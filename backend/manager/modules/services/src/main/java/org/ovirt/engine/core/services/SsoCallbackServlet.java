package org.ovirt.engine.core.services;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TerminateSessionsForTokenParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.crypto.EnvelopePBE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = 6329289042799650200L;

    private static Logger log = LoggerFactory.getLogger(SsoCallbackServlet.class);

    @Inject
    private BackendInternal backend;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Calling SsoCallbackServlet with queryString '{}'", request.getQueryString());

        String event = request.getParameter("event");
        String accessToken = request.getParameter("token");

        switch(event) {
            case "auditLog":
                handleAuditLog(request, response);
                break;
            case "logout":
                handleLogout(accessToken, response);
                break;
            default:
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                log.error("Unsupported event '{}'", event);
                break;
        }
    }

    private void handleAuditLog(HttpServletRequest request, HttpServletResponse response) {
        try {
            String clientSecret = request.getParameter("clientSecret");
            String engineClientSecret = EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_CLIENT_SECRET");
            // Check if the client secret passed by Sso matches the client secret in config
            if (EnvelopePBE.check(clientSecret, engineClientSecret)) {
                String loginErrMsg = request.getParameter("loginErrMsg");
                String userName = request.getParameter("userName");
                String sourceIp = request.getParameter("sourceIp");
                AuditLogable event = new AuditLogableImpl();
                event.addCustomValue("LoginErrMsg", String.format(" : '%s'", loginErrMsg));
                event.addCustomValue("SourceIP", sourceIp);
                event.setUserName(userName);
                auditLogDirector.log(event, AuditLogType.USER_VDC_LOGIN_FAILED);
            }
        } catch (Exception ex) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            response.setStatus(HttpURLConnection.HTTP_OK);
        }
    }

    private void handleLogout(String accessToken, HttpServletResponse response) {
        ActionReturnValue returnValue = backend.runInternalAction(ActionType.TerminateSessionsForToken,
                new TerminateSessionsForTokenParameters(accessToken));

        if (returnValue.getSucceeded()) {
            response.setStatus(HttpURLConnection.HTTP_OK);
            log.debug("Invalidate Sessions for access token '{}' succeeded", accessToken);
        } else {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            log.debug("Invalidate Sessions for access token '{}' failed", accessToken);
        }
    }
}
