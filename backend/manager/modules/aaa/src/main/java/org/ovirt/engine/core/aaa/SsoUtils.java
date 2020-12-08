package org.ovirt.engine.core.aaa;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.crypto.EnvelopeEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoUtils {
    private static final Logger log = LoggerFactory.getLogger(SsoUtils.class);

    public static String createUserSession(HttpServletRequest req,
            Map<String, Object> jsonResponse,
            boolean loginAsAdmin) throws Exception {
        String engineSessionId;
        if (!FiltersHelper.isStatusOk(jsonResponse)) {
            throw new RuntimeException((String) jsonResponse.get("MESSAGE"));
        }
        InitialContext ctx = null;
        Map<String, Object> payload = (Map<String, Object>) jsonResponse.get("ovirt");
        String username = (String) jsonResponse.get("user_id");
        String profile = null;
        int index = username.lastIndexOf("@");
        if (index != -1) {
            profile = username.substring(index + 1);
            username = username.substring(0, index);
        }
        String authzName = (String) jsonResponse.get("user_authz");
        try {
            ctx = new InitialContext();
            ActionReturnValue queryRetVal = FiltersHelper.getBackend(ctx).runAction(ActionType.CreateUserSession,
                    new CreateUserSessionParameters(
                            (String) jsonResponse.get(SessionConstants.SSO_TOKEN_KEY),
                            (String) jsonResponse.get(SessionConstants.SSO_SCOPE_KEY),
                            (String) jsonResponse.get(SessionConstants.SSO_SCOPE_KEY),
                            profile,
                            username,
                            (String) payload.get("principal_id"),
                            (String) payload.get("email"),
                            (String) payload.get("first_name"),
                            (String) payload.get("last_name"),
                            (String) payload.get("namespace"),
                            req == null ? "" : req.getRemoteAddr(),
                            (Collection<ExtMap>) payload.get("group_ids"),
                            loginAsAdmin));
            if (!queryRetVal.getSucceeded()) {
                if (queryRetVal.getActionReturnValue() == CreateUserSessionsError.NUM_OF_SESSIONS_EXCEEDED) {
                    throw new RuntimeException(String.format(
                            "Unable to login user %s@%s with profile [%s] " +
                                    "because the maximum number of allowed sessions %s is exceeded",
                            username,
                            authzName,
                            profile,
                            EngineLocalConfig.getInstance().getInteger("ENGINE_MAX_USER_SESSIONS")));
                }
                throw new RuntimeException(String.format(
                        "The user %s@%s with profile [%s] is not authorized to perform login",
                        username,
                        authzName,
                        profile));
            }
            engineSessionId = queryRetVal.getActionReturnValue();
            if (req != null) {
                req.setAttribute(
                        SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                        engineSessionId);
                req.setAttribute(
                        FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                        true);
            }
        } catch (Exception ex) {
            log.error("User '{}@{}' with profile [{}] login failed: {}", username, authzName, profile, ex.getMessage());
            log.debug("User '{}@{}' with profile [{}] login failed", username, authzName, profile, ex);
            throw ex;
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException ex) {
                log.error("Unable to close context", ex);
            }
        }
        return engineSessionId;
    }

    public static String getPassword(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("Sso access token is null.");
        }
        final Map<String, Object> response = SsoOAuthServiceUtils.getTokenInfo(
                token,
                "ovirt-ext=token:password-access");
        FiltersHelper.isStatusOk(response);
        final Map<String, Object> ovirt = (HashMap<String, Object>) response.get("ovirt");
        String password = (String) ovirt.get("password");
        try {
            password = password == null ? null : new String(
                    EnvelopeEncryptDecrypt.decrypt(EngineEncryptionUtils.getPrivateKeyEntry(), password),
                    StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Unable to decrypt user password for session {}.", ex.getMessage());
            log.debug("Exception", ex);
            password = null;
        }
        return password;
    }

    public static boolean isDomainValid(String appUrl) {
        if (StringUtils.isBlank(appUrl)) {
            return true;
        }
        Set<String> allowedDomains = new HashSet<>();
        String engineUrl = EngineLocalConfig.getInstance().getProperty("SSO_ENGINE_URL");
        allowedDomains.add(parseHostFromUrl(engineUrl, "SSO_ENGINE_URL"));

        var alternateFqdnString = EngineLocalConfig.getInstance().getProperty("SSO_ALTERNATE_ENGINE_FQDNS", true);
        if (StringUtils.isNotBlank(alternateFqdnString)) {
            Arrays.stream(alternateFqdnString.trim().split("\\s *"))
                    .filter(StringUtils::isNotBlank)
                    .map(String::toLowerCase)
                    .forEach(allowedDomains::add);
        }

        return allowedDomains.contains(parseHostFromUrl(appUrl, "appUrl"));
    }

    private static String parseHostFromUrl(String url, String urlPropertyName) {
        try {
            return new URI(url).getHost().toLowerCase();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(urlPropertyName + " not a valid URI: " + url);
        }
    }

}
