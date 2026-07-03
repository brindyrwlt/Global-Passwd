package fr.brindy.globalpasswd.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.TimeUnit;

public class Constants {
    // Technical informations
    public static final String PLUGIN_KEY = "global-passwd";
    public static final String KEY_FILE_NAME = "global.key";
    public static final String SESSIONS_FILE_NAME = "sessions.db";
    public static final String CONFIG_FILE_NAME = "config.yml";

    // Permissions
    public static final String PASSWD_CHANGE_PERMISSION = "globalpasswd.passwd.change";
    public static final String PASSWD_TOGGLE_PERMISSION = "globalpasswd.passwd.toggle";

    // Dialog
    public static final Component DIALOG_TITLE = Component.text("This server is protected by Global Passwd.");
    public static final Component PASSWORD_INPUT_TEXT = Component.text("Please enter the server password.");
    public static final String PASSWORD_INPUT_KEY = "password";
    public static final Component CONFIRM_BUTTON_TEXT = Component.text("Confirm");
    public static final Component CONFIRM_BUTTON_TOOLTIP = Component.text("Confirms your input and check whether the entered password is correct. If the password is correct, you will be connected to the server.");
    public static final Component CANCEL_BUTTON_TEXT = Component.text("Cancel");
    public static final Component CANCEL_BUTTON_TOOLTIP = Component.text("Disconnects you from the server.");
    public static final int INPUT_WIDTH = 300;
    public static final int BUTTON_WIDTH = 80;

    // Timeout
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
    public static Component getTimeoutDisconnectionMessage(long timeoutTime) {
        return Component.text("You did not enter any password within " + timeoutTime + " seconds, you got disconnected.");
    }
    public static final Component WRONG_PASSWORD_DISCONNECTION_MESSAGE = Component.text("You entered the wrong password. Please try again.");

    // Messages
    private static final Component PLUGIN_NAME = Component.text("Global Passwd").color(TextColor.color(0xB0DCCB));
    public static final Component PLUGIN_TAG = Component.text("[").append(Constants.PLUGIN_NAME).append(Component.text("] "));
    public static final Component PLUGIN_START_MESSAGE_1 = Component.text("The Global Passwd plugin is enabled. Your server is now protected!").color(TextColor.color(0xFFFFFF));
    public static final Component PLUGIN_START_MESSAGE_2 = Component.text("If you want to change your password, please enter the ").append(Component.text("'passwd change <new password>'").color(TextColor.color(0xFFFFFF)).append(Component.text(" command.")));

    private static Component buildMessage(String message) {
        return Constants.PLUGIN_TAG.append(Component.text(message));
    }

    public static final Component PASSWD_CHANGE_SUCCESS_MESSAGE = buildMessage("The server password has been changed.");
    public static final Component PASSWD_ENABLE_SUCCESS_MESSAGE = buildMessage("The server password has been enabled.");
    public static final Component PASSWD_DISABLE_SUCCESS_MESSAGE = buildMessage("The server password has been disabled.");
    public static final Component PASSWD_ALREADY_ENABLED_MESSAGE = buildMessage("The server password is already enabled.");
    public static final Component PASSWD_ALREADY_DISABLED_MESSAGE = buildMessage("The server password is already disabled.");
}
