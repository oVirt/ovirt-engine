package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.Credentials;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveBasicEnforceServlet extends HttpServlet {
    private static final long serialVersionUID = -2049151874771762209L;
    private String realm = "engine sso";

    private static Logger log = LoggerFactory.getLogger(InteractiveBasicEnforceServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Credentials credentials = SSOUtils.getUserCredentialsFromHeader(request);
        if (credentials != null) {
            log.debug("Credentials Obtained redirecting to url: {}", SSOConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
            SSOUtils.getSsoSession(request).setTempCredentials(credentials);
            response.sendRedirect(request.getContextPath() + SSOConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
        } else {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
