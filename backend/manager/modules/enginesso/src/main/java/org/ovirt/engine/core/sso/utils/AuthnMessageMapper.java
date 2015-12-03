package org.ovirt.engine.core.sso.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;

public class AuthnMessageMapper {
    private static final Map<Integer, String> messagesMap = new HashMap<>();

    private static final String USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED = "Cannot Login. User Password has expired. " +
            "Use the following URL to change the password: <a href='%s'> Link </a>";
    private static final String USER_FAILED_TO_AUTHENTICATE = "Login failed. Please verify your login information or " +
            "contact the system administrator.";
    private static final String USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWD = "The user name or password " +
            "is incorrect.";
    private static final String USER_ACCOUNT_DISABLED = "Cannot Login. User Account is Disabled or Locked, " +
            "Please contact your system administrator.";
    private static final String USER_ACCOUNT_EXPIRED = "Cannot Login. User Account has expired, " +
            "Please contact your system administrator.";
    public static final String USER_PASSWORD_EXPIRED = "Cannot Login. User Password has expired, " +
            "Please change your password.";
    private static final String USER_FAILED_TO_AUTHENTICATE_TIMED_OUT = "Login failed. A timeout has occurred to one " +
            "or more of the servers that participate in the login process.";

    static {
        messagesMap.put(Authn.AuthResult.GENERAL_ERROR, USER_FAILED_TO_AUTHENTICATE);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_INVALID, USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWD);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_INCORRECT, USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWD);
        messagesMap.put(Authn.AuthResult.ACCOUNT_LOCKED, USER_ACCOUNT_DISABLED);
        messagesMap.put(Authn.AuthResult.ACCOUNT_DISABLED, USER_ACCOUNT_DISABLED);
        messagesMap.put(Authn.AuthResult.ACCOUNT_EXPIRED, USER_ACCOUNT_EXPIRED);
        messagesMap.put(Authn.AuthResult.TIMED_OUT, USER_FAILED_TO_AUTHENTICATE_TIMED_OUT);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_EXPIRED, USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED);
    }

    public static final String mapMessageErrorCode(HttpServletRequest request, String profile, ExtMap outputMap) {
        int authResult = outputMap.<Integer>get(Authn.InvokeKeys.RESULT);
        String msg = messagesMap.containsKey(authResult) ? messagesMap.get(authResult) :USER_FAILED_TO_AUTHENTICATE;
        if (authResult == Authn.AuthResult.CREDENTIALS_EXPIRED) {
            if (outputMap.<String> get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL) != null ||
                    SSOUtils.getSsoContext(request).getSsoProfilesSupportingPasswdChange().contains(profile)) {
                msg = String.format(USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED,
                        request.getContextPath() + SSOConstants.INTERACTIVE_CHANGE_PASSWD_FORM_URI);
            } else {
                msg = USER_PASSWORD_EXPIRED;
            }
        }
        return msg;
    }
}
