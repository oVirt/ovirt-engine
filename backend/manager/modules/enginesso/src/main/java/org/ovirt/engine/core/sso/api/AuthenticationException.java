package org.ovirt.engine.core.sso.api;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 4276793672841624557L;

    public AuthenticationException(String errorMsg) {
        super(errorMsg);
    }

    public AuthenticationException(String errorMsg, Exception ex) {
        super(errorMsg, ex);
    }
}
