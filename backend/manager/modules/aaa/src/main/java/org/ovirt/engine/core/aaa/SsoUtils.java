package org.ovirt.engine.core.aaa;

import java.util.Collection;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoUtils {
    private static final Logger log = LoggerFactory.getLogger(SsoUtils.class);

    public static String createUserSession(HttpServletRequest req, Map<String, Object> jsonResponse, boolean loginAsAdmin) {
        String engineSessionId = null;
        if (!FiltersHelper.isStatusOk(jsonResponse)) {
            throw new RuntimeException((String) jsonResponse.get("MESSAGE"));
        }
        InitialContext ctx = null;
        Map<String, Object> payload = (Map<String, Object>) jsonResponse.get("ovirt");
        String username = (String) jsonResponse.get("user_id");
        String profile = null;
        int index = username.lastIndexOf("@");
        if (index != -1) {
            profile = username.substring(index+1);
            username = username.substring(0, index);
        }
        try {
            ctx = new InitialContext();
            VdcReturnValueBase queryRetVal = FiltersHelper.getBackend(ctx).runAction(VdcActionType.CreateUserSession,
                    new CreateUserSessionParameters(
                            (String) jsonResponse.get(SessionConstants.SSO_TOKEN_KEY),
                            profile,
                            username,
                            (String) payload.get("principal_id"),
                            (String) payload.get("email"),
                            req == null ? "" : req.getRemoteAddr(),
                            (Collection<ExtMap>) payload.get("group_ids"),
                            loginAsAdmin));
            if (!queryRetVal.getSucceeded()) {
                throw new RuntimeException(String.format("The user %s is not authorized to perform login", username));
            }
            engineSessionId = queryRetVal.getActionReturnValue();
            if (req != null) {
                req.getSession(true).setAttribute(
                        SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                        engineSessionId);
                req.getSession(true).setAttribute(
                        FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                        true);
            }
        } catch (Exception ex) {
            log.error("User '{}@{}' login failed: {}", username, profile, ex.getMessage());
            log.debug("User '{}@{}' login failed", username, profile, ex);
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

    public static String createUniqueStateInSessionIfNotExists(HttpServletRequest request) {
        String state = (String) request.getSession(true).getAttribute("state");
        try {
            if (StringUtils.isEmpty(state)) {
                state = FiltersHelper.generateState();
                request.getSession(true).setAttribute("state", state);
            }
        } catch (Exception ex) {
            log.error("Unable to generate unique state: {}", ex.getMessage());
            log.debug("Unable to generate unique state", ex);
        }
        return state;
    }
}
