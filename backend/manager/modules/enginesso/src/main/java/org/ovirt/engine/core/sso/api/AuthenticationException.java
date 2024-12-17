package org.ovirt.engine.core.sso.api;

import org.apache.commons.lang.builder.ToStringBuilder;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 4276793672841624557L;

    private String errorCode;

    public AuthenticationException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public AuthenticationException(String errorCode, String errorMsg, Exception ex) {
        super(errorMsg, ex);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("errorCode", getErrorCode()) //$NON-NLS-1$
                .append("message", getMessage()) //$NON-NLS-1$
                .toString();
    }
}
