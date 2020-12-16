package org.ovirt.engine.core.sso.api;

public class OAuthBadRequestException extends OAuthException {
    private static final long serialVersionUID = -1473602520785131331L;

    public OAuthBadRequestException(String code, String errorMsg) {
        super(code, errorMsg);
    }
}
