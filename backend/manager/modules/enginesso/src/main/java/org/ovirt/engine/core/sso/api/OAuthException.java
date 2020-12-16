package org.ovirt.engine.core.sso.api;

public class OAuthException extends RuntimeException {
    private static final long serialVersionUID = -1473602520785131331L;

    private String code;

    public OAuthException(String code, String errorMsg) {
        super(errorMsg);
        this.code = code;
    }

    public OAuthException(String code, String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
