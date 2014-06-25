package org.ovirt.engine.core.bll.provider.storage;

public class OpenStackImageException extends RuntimeException {

    public static enum ErrorType {
        UNSUPPORTED_CONTAINER_FORMAT,
        UNSUPPORTED_DISK_FORMAT,
        UNABLE_TO_DOWNLOAD_IMAGE,
        UNRECOGNIZED_IMAGE_FORMAT
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
