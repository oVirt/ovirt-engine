package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.aaa.AcctUtils;
import org.ovirt.engine.core.aaa.AuthType;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;

public class BasicAuthenticationFilter implements Filter {

    private static enum UserNameFormat {
        UPN,
        RESTAPI_SPECIFIC
    };

    private static class UserProfile {

        private String userName;
        private AuthenticationProfile profile;

        public UserProfile(String user, AuthenticationProfile profile) {
            this.userName = user;
            this.profile = profile;
        }
    }

    private static final Map<Integer, String> authResultMap;
    static {
        try {
            authResultMap = new HashMap<Integer, String>();
            for (Field field : Authn.AuthResult.class.getFields()) {
                authResultMap.put((Integer)field.get(null), field.getName());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BasicAuthenticationFilter.class);
    private UserNameFormat userNameFormat = UserNameFormat.UPN;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            userNameFormat = UserNameFormat.valueOf(filterConfig.getInitParameter("user-name-format"));
        } catch (Exception ex) {
            log.error("The value {} is not a valid UserNameFormat. setting UPN as default", filterConfig.getInitParameter("user-name-format"));
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!FiltersHelper.isAuthenticated(req)) {
            String headerValue = req.getHeader(FiltersHelper.Constants.HEADER_AUTHORIZATION);
            if (headerValue != null && headerValue.startsWith("Basic ")) {
                String[] creds = new String(
                        Base64.decodeBase64(headerValue.substring("Basic".length())),
                        Charset.forName("UTF-8")
                    ).split(":", 2);
                if (creds != null && creds.length == 2) {
                    handleCredentials(req, creds[0], creds[1]);
                } else {
                    log.error("Error in parsing basic authorization information");
                }
            }
        }
        chain.doFilter(request, response);
    }


    private UserProfile translateUser(String translateFrom) {
        UserProfile result = translateUserProfileUpn(translateFrom);
        if (userNameFormat == UserNameFormat.RESTAPI_SPECIFIC && result == null) {
            result = translateUserRestApiSpecific(translateFrom);
        }
        if (result == null) {
            result = new UserProfile(translateFrom, null);
        }
        return result;
    }

    private UserProfile translateUserProfileUpn(String translateFrom) {
        UserProfile result = null;
        int separator = translateFrom.lastIndexOf("@");
        if (separator != -1) {
            String profileName = translateFrom.substring(separator + 1);
            AuthenticationProfile profile = AuthenticationProfileRepository.getInstance().getProfile(profileName);
            result = profile != null ? new UserProfile(translateFrom.substring(0, separator), profile) : null;
        }
        return result;
    }

    private UserProfile translateUserRestApiSpecific(String translateFrom) {
        UserProfile result = null;
        int separator = translateFrom.indexOf("\\");
        if (separator != -1) {

            String profileName = translateFrom.substring(0, separator);
            AuthenticationProfile profile = AuthenticationProfileRepository.getInstance().getProfile(profileName);
            result = profile != null ? new UserProfile(translateFrom.substring(separator + 1), profile) : null;
        }
        return result;
    }


    private void handleCredentials(HttpServletRequest request, String user, String password) {
        UserProfile userProfile = translateUser(user);
        if (userProfile == null || userProfile.profile == null) {
            log.error("Cannot obtain profile for user {}", user);
        } else {
            ExtMap outputMap = userProfile.profile.getAuthn().invoke(new ExtMap().mput(
                    Base.InvokeKeys.COMMAND,
                    Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS
                    ).mput(
                            Authn.InvokeKeys.USER,
                            userProfile.userName
                    ).mput(
                           Authn.InvokeKeys.CREDENTIALS,
                           password
                    )
            );
            if (outputMap.<Integer> get(Base.InvokeKeys.RESULT) == Base.InvokeResult.SUCCESS &&
                    outputMap.<Integer> get(Authn.InvokeKeys.RESULT) == Authn.AuthResult.SUCCESS) {
                request.setAttribute(FiltersHelper.Constants.REQUEST_AUTH_RECORD_KEY,
                    outputMap.<ExtMap> get(Authn.InvokeKeys.AUTH_RECORD));
                request.setAttribute(FiltersHelper.Constants.REQUEST_AUTH_TYPE_KEY, AuthType.CREDENTIALS);
                request.setAttribute(FiltersHelper.Constants.REQUEST_PROFILE_KEY, userProfile.profile.getName());
                request.setAttribute(FiltersHelper.Constants.REQUEST_PASSWORD_KEY, password);
            } else {
                int authResultCode = outputMap.<Integer> get(Authn.InvokeKeys.RESULT, Authn.AuthResult.GENERAL_ERROR);
                String authnResult = authResultMap.get(authResultCode);
                if (authnResult == null) {
                    authnResult = Integer.toString(authResultCode);
                }
                AcctUtils.reportRecords(
                        Acct.ReportReason.PRINCIPAL_LOGIN_FAILED,
                        userProfile.profile.getAuthzName(),
                        userProfile.userName,
                        null,
                        null,
                        "Basic authentication failed for User %1$s (%2$s).",
                        userProfile.userName,
                        authnResult
                        );
                log.error("User {} authentication failed. profile is {}. Invocation Result code is {}. Authn result code is {}",
                        userProfile.userName,
                        userProfile.profile.getName(),
                        outputMap.<Integer> get(Base.InvokeKeys.RESULT),
                        authnResult
                        );
            }
        }
    }


    @Override
    public void destroy() {
    }

}
