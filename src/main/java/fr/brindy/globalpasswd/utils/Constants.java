package fr.brindy.globalpasswd.utils;

import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class Constants {
    public static final Component DIALOG_TITLE = Component.text("This server is protected by Global Passwd.");
    public static final Component PASSWORD_INPUT_TEXT = Component.text("Please enter your password.");
    public static final String PASSWORD_INPUT_KEY = "password";
    public static final Component CONFIRM_BUTTON_TEXT = Component.text("Confirm");
    public static final Component CONFIRM_BUTTON_TOOLTIP = Component.text("Confirms your input and check wether the entered password is correct. If the password is correct, you will be connected to the server.");
    public static final Component CANCEL_BUTTON_TEXT = Component.text("Cancel");
    public static final Component CANCEL_BUTTON_TOOLTIP = Component.text("Disconnects you from the server.");
    public static final String PLUGIN_KEY = "global-passwd";

    public static final long TIMEOUT_TIME = 2;
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
    public static final Component TIMEOUT_DISCONNECTION_MESSAGE = Component.text("You did not enter any password within " + TIMEOUT_TIME + " minute(s), you got disconnected.");
    public static final Component WRONG_PASSWORD_DISCONNECTION_MESSAGE = Component.text("You entered the wrong password. Please try again.");

}
