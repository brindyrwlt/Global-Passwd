package fr.brindy.globalpasswd.events;

import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.Keys;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerConnectionEvent implements Listener {
    /**
     * Stores the UUIDs of the players trying to connect. Used to kick them when time to enter password is out.
     */
    private final Map<UUID, CompletableFuture<Boolean>> connecting = new HashMap<>();
    private final AuthService authService;
    private final ComponentLogger logger;


    public PlayerConnectionEvent(AuthService authService, ComponentLogger logger) {
        this.authService = authService;
        this.logger = logger;
    }

    @EventHandler
    public void onConnection(AsyncPlayerConnectionConfigureEvent event) {
        PlayerConfigurationConnection connection = event.getConnection();
        UUID uuid = connection.getProfile().getId();
        if(uuid == null) {
            return;
        }

        Dialog dialog = Dialog.create(
                builder -> builder.empty().base(
                        DialogBase.builder(Constants.DIALOG_TITLE)
                                .canCloseWithEscape(false)
                                .inputs(List.of(
                                    DialogInput.text(Constants.PASSWORD_INPUT_KEY, Constants.PASSWORD_INPUT_TEXT)
                                            .width(500)
                                            .build()
                                ))
                                .build()
                ).type(
                        DialogType.multiAction(List.of(
                                ActionButton.create(
                                    Constants.CONFIRM_BUTTON_TEXT,
                                    Constants.CONFIRM_BUTTON_TOOLTIP,
                                    80,
                                    DialogAction.customClick(Keys.CONFIRM.toKey(), null)
                                ),
                                ActionButton.create(
                                    Constants.CANCEL_BUTTON_TEXT,
                                    Constants.CANCEL_BUTTON_TOOLTIP,
                                    80,
                                    DialogAction.customClick(Keys.CANCEL.toKey(), null)
                                )
                        ))
                        .build()
                )
        );

        Audience audience = connection.getAudience();
        audience.showDialog(dialog);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeOnTimeout(false, Constants.TIMEOUT_TIME, Constants.TIMEOUT_UNIT);

        connecting.put(uuid, future);

        if(!future.join()) {
            audience.closeDialog();
            disconnectPlayer(connection, Constants.TIMEOUT_DISCONNECTION_MESSAGE);
        }

        connecting.remove(uuid);
    }

    @EventHandler
    public void onButtonClick(PlayerCustomClickEvent event) {
        if(event.getCommonConnection() instanceof PlayerConfigurationConnection connection) {
            UUID uuid = connection.getProfile().getId();
            if (uuid == null) {
                return;
            }

            logger.info("ça passe ici");

            String action = event.getIdentifier().toString();
            if(Objects.equals(action, Keys.CANCEL.toString()) || Objects.equals(action, Keys.CONFIRM.toString())) {
                logger.info("boutons reconnus");

                DialogResponseView dialog = event.getDialogResponseView();
                if(dialog == null) {
                    return;
                }

                String passwordEntered = dialog.getText(Constants.PASSWORD_INPUT_KEY);

                try {
                    if(authService.compare(passwordEntered)) {
                        logger.info("bon mot de passe");
                        connecting.get(uuid).complete(true);
                        return;
                    }
                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                }

                logger.info(passwordEntered);

                connecting.remove(uuid);
                disconnectPlayer(connection, Constants.WRONG_PASSWORD_DISCONNECTION_MESSAGE);
            }
        }
    }

    private void disconnectPlayer(PlayerConfigurationConnection connection, Component reason) {
        connection.disconnect(reason);
    }
}
