package org.ovirt.engine.core.sso.utils;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;

public class AuthnMessageMapper {
    public static final String USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED = "Cannot Login. User Password has expired. Use the following URL to change the password: %s";
    public static final String USER_PASSWORD_EXPIRED = "Cannot Login. User Password has expired, Please change your password.";
    public static final String USER_FAILED_TO_AUTHENTICATE = "Login failed. Please verify your login information or contact the system administrator.";

    public static final String mapMessageErrorCode(ExtMap outputMap) {
        String msg = USER_FAILED_TO_AUTHENTICATE;
        int authResult = outputMap.<Integer>get(Authn.InvokeKeys.RESULT);
        if (authResult == Authn.AuthResult.CREDENTIALS_EXPIRED) {
            boolean addedUserPasswordExpired = false;
            if (outputMap.<String> get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL) != null) {
                msg = String.format(USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED,
                        outputMap.<String>get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL));
                addedUserPasswordExpired = true;
            }
            if (!addedUserPasswordExpired) {
                msg = USER_PASSWORD_EXPIRED;
            }
        }
        return msg;
    }
}
