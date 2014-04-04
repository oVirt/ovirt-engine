package org.ovirt.engine.api.extensionsold;

public class AAAExtensionException extends RuntimeException {

    public enum AAAExtensionError {
        INCORRECT_CREDENTIALS,
        CREDENTIALS_EXPIRED,
        INVALID_CONFIGURATION,
        SERVER_IS_NOT_AVAILABLE,
        TIMED_OUT,
        LOCKED_OR_DISABLED_ACCOUNT,
        GENERAL_ERROR
    };

    private final AAAExtensionError error;


    public AAAExtensionException(AAAExtensionError error, String message) {
        super(message);
        this.error = error;
    }

    public AAAExtensionException(AAAExtensionError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public AAAExtensionError getError() {
        return error;
    }


}
