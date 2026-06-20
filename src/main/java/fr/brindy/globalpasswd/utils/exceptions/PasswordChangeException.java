package fr.brindy.globalpasswd.utils.exceptions;

public class PasswordChangeException extends RuntimeException {
    public PasswordChangeException(String errorMessage) {
        super(errorMessage, new Throwable("An unknown problem has occured when trying to change the server password."));
    }
}
