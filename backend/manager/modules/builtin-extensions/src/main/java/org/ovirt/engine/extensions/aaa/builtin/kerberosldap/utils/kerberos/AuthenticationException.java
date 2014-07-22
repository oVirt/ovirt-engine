package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 8134567244545952978L;
    private final AuthenticationResult authResult;

    public AuthenticationException(AuthenticationResult authResult) {
        super(authResult.getDetailedMessage());
        this.authResult = authResult;
    }

    public AuthenticationResult getAuthResult() {
        return authResult;
    }
}
