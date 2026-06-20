package fr.brindy.globalpasswd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.exceptions.PasswordChangeException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PasswdCommand {
    private final AuthService authService;

    public PasswdCommand(AuthService authService) {
        this.authService = authService;
    }

    public LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("passwd")
                .requires(Commands.restricted(
                        source -> source.getSender().hasPermission(Constants.PASSWD_CHANGE_PERMISSION)
                ))
                .then(
                    Commands.literal("change")
                        .then(
                            Commands.argument("password", StringArgumentType.string())
                                .executes(this::changePassword)
                        )
                )
                .build();
    }

    private int changePassword(CommandContext<CommandSourceStack> context) {
        try {
            authService.savePassword(context.getArgument("password", String.class));
            Bukkit.getServer().broadcast(Constants.PASSWD_CHANGE_SUCCESS_MESSAGE);
            return Command.SINGLE_SUCCESS;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PasswordChangeException(e.getMessage());
        }
    }
}
