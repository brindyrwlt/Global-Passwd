package fr.brindy.globalpasswd.events;

import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.services.ConfigService;
import fr.brindy.globalpasswd.services.SessionService;
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
    private final SessionService sessionService;
    private final ConfigService configService;

    private static final Dialog authDialog = createDialog();

    public PlayerConnectionEvent(AuthService authService, SessionService sessionService, ConfigService configService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.configService = configService;
    }

    @EventHandler
    public void onConnection(AsyncPlayerConnectionConfigureEvent event) {
        if(configService.getEnabled()) {
            PlayerConfigurationConnection connection = event.getConnection();
            UUID uuid = connection.getProfile().getId();
            if(uuid == null) {
                return;
            }

            if(sessionService != null && sessionService.isSessionValid(uuid.toString())) {
                return;
            }

            Audience audience = connection.getAudience();
            audience.showDialog(authDialog);

            long timeoutTime = configService.getTimeoutTime();
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeOnTimeout(false, timeoutTime, Constants.TIMEOUT_UNIT);

            connecting.put(uuid, future);

            if(!future.join()) {
                audience.closeDialog();
                disconnectPlayer(connection, Constants.getTimeoutDisconnectionMessage(timeoutTime));
            }

            connecting.remove(uuid);
        }
    }

    @EventHandler
    public void onButtonClick(PlayerCustomClickEvent event) {
        if(configService.getEnabled()) {
            if(event.getCommonConnection() instanceof PlayerConfigurationConnection connection) {
                UUID uuid = connection.getProfile().getId();
                if(uuid == null) {
                    return;
                }

                String action = event.getIdentifier().toString();
                if(Objects.equals(action, Keys.CONFIRM.toString())) {
                    DialogResponseView dialog = event.getDialogResponseView();
                    if(dialog == null) {
                        return;
                    }

                    String passwordEntered = dialog.getText(Constants.PASSWORD_INPUT_KEY);

                    try {
                        if(authService.compare(passwordEntered)) {
                            connecting.get(uuid).complete(true);

                            if(sessionService != null) {
                                sessionService.validateSession(uuid.toString());
                            }
                            return;
                        }
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                        throw new RuntimeException(e);
                    }

                    connecting.remove(uuid);
                    disconnectPlayer(connection, Constants.WRONG_PASSWORD_DISCONNECTION_MESSAGE);
                } else if(Objects.equals(action, Keys.CANCEL.toString())) {
                    disconnectPlayer(connection, Component.empty());
                }
            }
        }
    }

    private void disconnectPlayer(PlayerConfigurationConnection connection, Component reason) {
        connection.disconnect(reason);
    }

    private static Dialog createDialog() {
        return Dialog.create(
                builder -> builder.empty().base(
                        DialogBase.builder(Constants.DIALOG_TITLE)
                                .canCloseWithEscape(false)
                                .inputs(List.of(
                                        DialogInput.text(Constants.PASSWORD_INPUT_KEY, Constants.PASSWORD_INPUT_TEXT)
                                                .width(Constants.INPUT_WIDTH)
                                                .build()
                                ))
                                .build()
                ).type(
                        DialogType.multiAction(List.of(
                                        ActionButton.create(
                                                Constants.CONFIRM_BUTTON_TEXT,
                                                Constants.CONFIRM_BUTTON_TOOLTIP,
                                                Constants.BUTTON_WIDTH,
                                                DialogAction.customClick(Keys.CONFIRM.toKey(), null)
                                        ),
                                        ActionButton.create(
                                                Constants.CANCEL_BUTTON_TEXT,
                                                Constants.CANCEL_BUTTON_TOOLTIP,
                                                Constants.BUTTON_WIDTH,
                                                DialogAction.customClick(Keys.CANCEL.toKey(), null)
                                        )
                                ))
                                .build()
                )
        );
    }
}
