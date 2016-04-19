package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveRedirectToModuleServlet extends HttpServlet {
    private static final long serialVersionUID = -4283642288798438953L;
    private static Logger log = LoggerFactory.getLogger(InteractiveRedirectToModuleServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SsoUtils.isUserAuthenticated(request)) {
            log.debug("User is authenticated redirecting to module");
            SsoUtils.redirectToModule(
                    request,
                    response);
        } else {
            SsoUtils.getSsoSession(request).setReauthenticate(false);
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_FORM_URI);
        }
    }
}
