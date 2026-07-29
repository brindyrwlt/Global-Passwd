package fr.brindy.globalpasswd.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.services.ConfigService;
import fr.brindy.globalpasswd.services.SessionService;
import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.exceptions.PasswordChangeException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class PasswdCommand {
    private final AuthService authService;
    private final ConfigService configService;
    private final SessionService sessionService;
    private final Server server;

    public PasswdCommand(AuthService authService, ConfigService configService, SessionService sessionService) {
        this.authService = authService;
        this.configService = configService;
        this.sessionService = sessionService;
        this.server = Bukkit.getServer();
    }

    public LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("passwd")
                .then(simpleArgument(
                    "enable",
                    context -> togglePlugin(context, true),
                    Constants.PASSWD_TOGGLE_PERMISSION
                ))
                .then(simpleArgument(
                    "disable",
                    context -> togglePlugin(context, false),
                    Constants.PASSWD_TOGGLE_PERMISSION
                ))
                .then(simpleArgument(
                    "status",
                    this::showStatus,
                    Constants.PASSWD_STATUS_PERMISSION
                ))
                .then(
                    Commands.literal("change")
                        .requires(Commands.restricted(
                            source -> source.getSender().hasPermission(Constants.PASSWD_CHANGE_PERMISSION)
                        ))
                        .then(
                            Commands.argument("password", StringArgumentType.string())
                                .executes(this::changePassword)
                        )
                )
                .then(
                    Commands.literal("sessions")
                        .then(simpleArgument(
                            "enable",
                            context -> toggleSessions(context, true),
                            Constants.PASSWD_SESSIONS_TOGGLE_PERMISSION
                        ))
                        .then(simpleArgument(
                            "disable",
                            context -> toggleSessions(context, false),
                            Constants.PASSWD_SESSIONS_TOGGLE_PERMISSION
                        ))
                        .then(
                            Commands.literal("delete")
                                .then(simpleArgument(
                                    "all",
                                    this::deleteAllSessions,
                                    Constants.PASSWD_SESSIONS_DELETE_ALL_PERMISSION
                                ))
                                .then(
                                    Commands.argument("players", ArgumentTypes.playerProfiles())
                                        .requires(Commands.restricted(
                                            source -> source.getSender().hasPermission(Constants.PASSWD_SESSIONS_DELETE_PLAYER_PERMISSION)
                                        ))
                                        .executes(context -> managePlayerSession(context, sessionService::deletePlayerSession, Constants.SESSIONS_PLAYER_DELETED_MESSAGE))
                                )
                                // If there is no argument provided
                                .executes(this::deleteSenderSession)
                        )
                        .then(
                            Commands.literal("add")
                                .requires(Commands.restricted(
                                        source -> source.getSender().hasPermission(Constants.PASSWD_SESSIONS_ADD_PLAYER_PERMISSION)
                                ))
                                .then(
                                    Commands.argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> managePlayerSession(context, sessionService::validateSession, Constants.SESSIONS_PLAYER_ADDED_MESSAGE))
                                )
                        )
                )
                .build();
    }

    private int changePassword(CommandContext<CommandSourceStack> context) {
        try {
            authService.savePassword(context.getArgument("password", String.class));
            broadcast(Constants.PASSWD_CHANGE_SUCCESS_MESSAGE);
            return Command.SINGLE_SUCCESS;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PasswordChangeException(e.getMessage());
        }
    }

    private int managePlayerSession(CommandContext<CommandSourceStack> context, Consumer<String> func, Component message) {
        final Collection<PlayerProfile> players;
        try {
            players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        for(final PlayerProfile player : players) {
            UUID playerId = player.getId();

            if(playerId != null) {
                func.accept(playerId.toString());
            }
        }

        messageUser(context.getSource().getSender(), message);

        return Command.SINGLE_SUCCESS;
    }

    private int deleteSenderSession(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getExecutor();

        if(entity instanceof Player player) {
            sessionService.deletePlayerSession(player.getUniqueId().toString());
        }

        messageUser(source.getSender(), Constants.SESSIONS_SELF_DELETED_MESSAGE);

        return Command.SINGLE_SUCCESS;
    }

    private int showStatus(CommandContext<CommandSourceStack> context) {
        messageUser(context.getSource().getSender(), Constants.getStatusMessage(configService.getEnabled(), configService.getSessionsEnabled(), sessionService.getSessionCount()));
        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> simpleArgument(String argumentName, Command<CommandSourceStack> action, String permission) {
        return Commands.literal(argumentName)
                .requires(Commands.restricted(
                        source -> source.getSender().hasPermission(permission)
                ))
                .executes(action);
    }

    private int togglePlugin(CommandContext<CommandSourceStack> context, boolean isEnabled) {
        if(configService.getEnabled() == isEnabled) {
            messageUser(context.getSource().getSender(), isEnabled ? Constants.PASSWD_ALREADY_ENABLED_MESSAGE
                                                                     : Constants.PASSWD_ALREADY_DISABLED_MESSAGE);
        } else {
            configService.setEnabled(isEnabled);
            broadcast(isEnabled ? Constants.PASSWD_ENABLE_SUCCESS_MESSAGE
                                : Constants.PASSWD_DISABLE_SUCCESS_MESSAGE);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int toggleSessions(CommandContext<CommandSourceStack> context, boolean isEnabled) {
        if(configService.getSessionsEnabled() == isEnabled) {
            messageUser(context.getSource().getSender(), isEnabled ? Constants.PASSWD_SESSIONS_ALREADY_ENABLED_MESSAGE
                                                                   : Constants.PASSWD_SESSIONS_ALREADY_DISABLED_MESSAGE);
        } else {
            configService.setSessionsEnabled(isEnabled);
            broadcast(isEnabled ? Constants.PASSWD_ENABLE_SESSIONS_SUCCESS_MESSAGE
                                : Constants.PASSWD_DISABLE_SESSIONS_SUCCESS_MESSAGE);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int deleteAllSessions(CommandContext<CommandSourceStack> context) {
        sessionService.deleteAllSessions();
        broadcast(Constants.SESSIONS_DELETE_ALL_MESSAGE);

        return Command.SINGLE_SUCCESS;
    }

    private void broadcast(Component message) {
        server.broadcast(message);
    }

    private void messageUser(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }
}
