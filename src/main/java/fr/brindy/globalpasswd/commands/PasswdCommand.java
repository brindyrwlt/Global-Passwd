package fr.brindy.globalpasswd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.services.ConfigService;
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
    private final Server server;

    public PasswdCommand(AuthService authService, ConfigService configService) {
        this.authService = authService;
        this.configService = configService;
        this.server = Bukkit.getServer();
    }

    public LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("passwd")
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
                .then(togglePluginArgument("enable", true))
                .then(togglePluginArgument("disable", false))
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

    private LiteralArgumentBuilder<CommandSourceStack> togglePluginArgument(String argumentName, boolean isEnabled) {
        return Commands.literal(argumentName)
                .requires(Commands.restricted(
                        source -> source.getSender().hasPermission(Constants.PASSWD_TOGGLE_PERMISSION)
                ))
                .executes(context -> togglePlugin(context, isEnabled));
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

    private void broadcast(Component message) {
        server.broadcast(message);
    }

    private void messageUser(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }
}
