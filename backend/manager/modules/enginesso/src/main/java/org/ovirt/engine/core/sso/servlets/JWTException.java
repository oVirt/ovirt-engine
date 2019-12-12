package org.ovirt.engine.core.sso.servlets;

public class JWTException extends RuntimeException {
    public JWTException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    enum ErrorCode{
        CANNOT_SERIALIZE_PLAIN_JWT
    }

    private final ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
