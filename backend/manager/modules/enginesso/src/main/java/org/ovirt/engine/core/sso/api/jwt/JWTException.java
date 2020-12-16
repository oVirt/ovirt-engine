package org.ovirt.engine.core.sso.api.jwt;

public class JWTException extends RuntimeException {
    public JWTException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        CANNOT_SERIALIZE_PLAIN_JWT
    }

    private final ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
