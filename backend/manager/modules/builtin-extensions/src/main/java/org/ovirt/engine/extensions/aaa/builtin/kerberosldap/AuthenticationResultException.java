package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;

public class AuthenticationResultException extends DirectoryServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 2854926242091038897L;
    private AuthenticationResult result;

    public AuthenticationResultException(AuthenticationResult result) {
        super(result.getDetailedMessage());
        this.result = result;
    }

    public AuthenticationResult getResult() {
        return result;
    }

    public AuthenticationResultException(AuthenticationResult result, String message) {
        super(message);
        this.result = result;
    }

    public AuthenticationResultException(AuthenticationResult result, Throwable cause) {
        super(result.getDetailedMessage(), cause);
        this.result = result;
    }

    public AuthenticationResultException(AuthenticationResult result, String message, Throwable cause) {
        super(message, cause);
        this.result = result;
    }
}
