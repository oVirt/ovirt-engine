package org.ovirt.engine.core.bll.provider.storage;

public class OpenStackImageException extends RuntimeException {

    private static final long serialVersionUID = 8324838739774296612L;

    public static enum ErrorType {
        UNSUPPORTED_CONTAINER_FORMAT,
        UNSUPPORTED_DISK_FORMAT,
        UNABLE_TO_DOWNLOAD_IMAGE,
        UNRECOGNIZED_IMAGE_FORMAT,
        IMAGE_NOT_FOUND
    }

    private ErrorType errorType;

    public OpenStackImageException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

}
