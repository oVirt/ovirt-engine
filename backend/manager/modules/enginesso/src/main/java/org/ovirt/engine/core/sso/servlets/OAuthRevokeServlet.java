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

import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.SsoService;
import org.ovirt.engine.core.sso.service.TokenCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthRevokeServlet extends HttpServlet {
    private static final long serialVersionUID = -473606118937052463L;
    private static Logger log = LoggerFactory.getLogger(OAuthRevokeServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered OAuthRevokeServlet QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SsoService.getRequestParameters(request));

        try {
            String token = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_TOKEN);
            String scope = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_SCOPE, "");
            SsoService.validateClientAcceptHeader(request);
            String[] clientIdAndSecret = SsoService.getClientIdClientSecret(request);
            SsoService.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], scope, null);

            SsoSession ssoSession = ssoContext.getSsoSession(token);
            if (ssoSession != null) {
                Set<String> associatedClientIds = new TreeSet<>(ssoSession.getAssociatedClientIds());
                boolean revokeAllScope = SsoService.scopeAsList(scope).contains("ovirt-ext=revoke:revoke-all");
                if (revokeAllScope) {
                    SsoService.validateRequestScope(request, token, scope);
                } else {
                    ssoSession.getAssociatedClientIds().remove(clientIdAndSecret[0]);
                }
                if (revokeAllScope || ssoSession.getAssociatedClientIds().isEmpty()) {
                    log.info("User {}@{} with profile [{}] successfully logged out",
                            SsoService.getUserId(ssoSession.getPrincipalRecord()),
                            ssoContext.getUserAuthzName(ssoSession),
                            ssoSession.getProfile());
                    TokenCleanupService.cleanupSsoSession(ssoContext, ssoSession, associatedClientIds);
                }
            }
            SsoService.sendJsonData(response, new HashMap<>());
        } catch (OAuthException ex) {
            SsoService.sendJsonDataWithMessage(request, response, ex);
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }
}
