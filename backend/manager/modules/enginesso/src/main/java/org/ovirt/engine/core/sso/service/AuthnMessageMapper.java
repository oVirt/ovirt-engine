package org.ovirt.engine.core.sso.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;

public class AuthnMessageMapper {
    private static final Map<Integer, String> messagesMap = new HashMap<>();

    static {
        messagesMap.put(Authn.AuthResult.GENERAL_ERROR, SsoConstants.APP_ERROR_USER_FAILED_TO_AUTHENTICATE);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_INVALID,
                SsoConstants.APP_ERROR_USER_FAILED_TO_AUTHENTICATE);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_INCORRECT,
                SsoConstants.APP_ERROR_USER_FAILED_TO_AUTHENTICATE);
        messagesMap.put(Authn.AuthResult.ACCOUNT_LOCKED, SsoConstants.APP_ERROR_USER_ACCOUNT_DISABLED);
        messagesMap.put(Authn.AuthResult.ACCOUNT_DISABLED, SsoConstants.APP_ERROR_USER_ACCOUNT_DISABLED);
        messagesMap.put(Authn.AuthResult.ACCOUNT_EXPIRED, SsoConstants.APP_ERROR_USER_ACCOUNT_EXPIRED);
        messagesMap.put(Authn.AuthResult.TIMED_OUT, SsoConstants.APP_ERROR_USER_FAILED_TO_AUTHENTICATE_TIMED_OUT);
        messagesMap.put(Authn.AuthResult.CREDENTIALS_EXPIRED,
                SsoConstants.APP_ERROR_USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED);
    }

    public static final String mapErrorCode(
            SsoContext ssoContext,
            HttpServletRequest request,
            String profile,
            ExtMap outputMap) {
        int authResult = outputMap.<Integer>get(Authn.InvokeKeys.RESULT);
        String errorCode = messagesMap.containsKey(authResult)
                ? messagesMap.get(authResult)
                : SsoConstants.APP_ERROR_USER_FAILED_TO_AUTHENTICATE;

        if (authResult == Authn.AuthResult.CREDENTIALS_EXPIRED) {
            if (outputMap.<String> get(Authn.InvokeKeys.CREDENTIALS_CHANGE_URL) != null ||
                    SsoService.getSsoContext(request).getSsoProfilesSupportingPasswdChange().contains(profile)) {
                errorCode = SsoConstants.APP_ERROR_USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED;
            } else {
                errorCode = SsoConstants.APP_ERROR_USER_PASSWORD_EXPIRED;
            }
        }

        return errorCode;
    }
}
