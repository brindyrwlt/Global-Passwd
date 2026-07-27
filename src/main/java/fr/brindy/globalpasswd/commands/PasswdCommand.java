package fr.brindy.globalpasswd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.services.ConfigService;
import fr.brindy.globalpasswd.services.SessionService;
import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.exceptions.PasswordChangeException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
                            Commands.literal("reset")
                                .then(simpleArgument(
                                    "all",
                                    this::resetAllSessions,
                                    Constants.PASSWD_SESSIONS_RESET_ALL_PERMISSION
                                ))
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

    private int resetAllSessions(CommandContext<CommandSourceStack> context) {
        if(sessionService.isEnabled()) {
            sessionService.deleteAllSessions();
            broadcast(Constants.SESSIONS_RESET_ALL_MESSAGE);
        } else {
            broadcast(Constants.SESSIONS_DISABLED_ERROR_MESSAGE);
        }

        return Command.SINGLE_SUCCESS;
    }

    private void broadcast(Component message) {
        server.broadcast(message);
    }

    private void messageUser(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }
}
