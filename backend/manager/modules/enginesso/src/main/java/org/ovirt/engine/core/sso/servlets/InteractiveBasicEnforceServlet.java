package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.api.Credentials;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveBasicEnforceServlet extends HttpServlet {
    private static final long serialVersionUID = -2049151874771762209L;
    private String realm = "engine sso";

    private static Logger log = LoggerFactory.getLogger(InteractiveBasicEnforceServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Credentials credentials = SsoService.getUserCredentialsFromHeader(request);
        if (credentials != null) {
            log.debug("Credentials Obtained redirecting to url: {}", SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
            SsoService.getSsoSession(request).setTempCredentials(credentials);
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
        } else {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
