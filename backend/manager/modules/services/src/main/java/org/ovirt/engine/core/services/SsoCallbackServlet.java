package org.ovirt.engine.core.services;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.TerminateSessionsForTokenParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = 6329289042799650200L;

    private static Logger log = LoggerFactory.getLogger(SsoCallbackServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Calling SsoCallbackServlet with queryString '{}'", request.getQueryString());

        String event = request.getParameter("event");
        String accessToken = request.getParameter("token");

        switch(event) {
            case "logout":
                handleLogout(accessToken, response);
                break;
            default:
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                log.error("Unsupported event '{}'", event);
                break;
        }
    }

    private void handleLogout(String accessToken, HttpServletResponse response) {
        BackendInternal backend = EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
        VdcReturnValueBase returnValue = backend.runInternalAction(VdcActionType.TerminateSessionsForToken,
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
