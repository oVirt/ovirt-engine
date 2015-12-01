package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOContext;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.ovirt.engine.core.sso.utils.TokenCleanupUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthRevokeServlet extends HttpServlet {
    private static final long serialVersionUID = -473606118937052463L;
    private static Logger log = LoggerFactory.getLogger(OAuthRevokeServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered OAuthRevokeServlet QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SSOUtils.getRequestParameters(request));

        try {
            String token = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_TOKEN);
            String scope = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_SCOPE, "");
            SSOUtils.validateClientAcceptHeader(request);
            String[] clientIdAndSecret = SSOUtils.getClientIdClientSecret(request);
            SSOUtils.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], scope, null);

            SSOContext ssoContext = SSOUtils.getSsoContext(request);
            SSOSession ssoSession = ssoContext.getSsoSession(token);
            if (ssoSession != null) {
                Set<String> associatedClientIds = new TreeSet<>(ssoSession.getAssociatedClientIds());
                boolean revokeAllScope = SSOUtils.scopeAsList(scope).contains("ovirt-ext=revoke:revoke-all");
                if (revokeAllScope) {
                    SSOUtils.validateRequestScope(request, token, scope);
                } else {
                    ssoSession.getAssociatedClientIds().remove(clientIdAndSecret[0]);
                }
                if (revokeAllScope || ssoSession.getAssociatedClientIds().isEmpty()) {
                    log.info("User {}@{} successfully logged out",
                            SSOUtils.getUserId(ssoSession.getPrincipalRecord()),
                            ssoSession.getProfile());
                    TokenCleanupUtility.cleanupSsoSession(ssoContext, ssoSession, associatedClientIds);
                }
            }
            SSOUtils.sendJsonData(response, new HashMap<>());
        } catch (OAuthException ex) {
            SSOUtils.sendJsonDataWithMessage(response, ex);
        } catch (Exception ex) {
            SSOUtils.sendJsonDataWithMessage(response, SSOConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }
}
