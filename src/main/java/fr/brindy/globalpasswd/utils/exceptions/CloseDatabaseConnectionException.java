package fr.brindy.globalpasswd.utils.exceptions;

public class CloseDatabaseConnectionException extends RuntimeException {
    public CloseDatabaseConnectionException(String errorMessage) {
        super(errorMessage, new Throwable("Failed to close connection to the session database."));
    }
}
