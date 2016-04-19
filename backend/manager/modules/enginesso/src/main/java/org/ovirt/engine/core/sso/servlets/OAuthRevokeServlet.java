package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoContext;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.ovirt.engine.core.sso.utils.TokenCleanupUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthRevokeServlet extends HttpServlet {
    private static final long serialVersionUID = -473606118937052463L;
    private static Logger log = LoggerFactory.getLogger(OAuthRevokeServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoUtils.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered OAuthRevokeServlet QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SsoUtils.getRequestParameters(request));

        try {
            String token = SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_TOKEN);
            String scope = SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_SCOPE, "");
            SsoUtils.validateClientAcceptHeader(request);
            String[] clientIdAndSecret = SsoUtils.getClientIdClientSecret(request);
            SsoUtils.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], scope, null);

            SsoSession ssoSession = ssoContext.getSsoSession(token);
            if (ssoSession != null) {
                Set<String> associatedClientIds = new TreeSet<>(ssoSession.getAssociatedClientIds());
                boolean revokeAllScope = SsoUtils.scopeAsList(scope).contains("ovirt-ext=revoke:revoke-all");
                if (revokeAllScope) {
                    SsoUtils.validateRequestScope(request, token, scope);
                } else {
                    ssoSession.getAssociatedClientIds().remove(clientIdAndSecret[0]);
                }
                if (revokeAllScope || ssoSession.getAssociatedClientIds().isEmpty()) {
                    log.info("User {}@{} successfully logged out",
                            SsoUtils.getUserId(ssoSession.getPrincipalRecord()),
                            ssoSession.getProfile());
                    TokenCleanupUtility.cleanupSsoSession(ssoContext, ssoSession, associatedClientIds);
                }
            }
            SsoUtils.sendJsonData(response, new HashMap<>());
        } catch (OAuthException ex) {
            SsoUtils.sendJsonDataWithMessage(response, ex);
        } catch (Exception ex) {
            SsoUtils.sendJsonDataWithMessage(response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }
}
