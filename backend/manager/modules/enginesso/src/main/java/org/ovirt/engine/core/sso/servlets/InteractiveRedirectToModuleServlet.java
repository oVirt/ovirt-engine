package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveRedirectToModuleServlet extends HttpServlet {
    private static final long serialVersionUID = -4283642288798438953L;
    private static Logger log = LoggerFactory.getLogger(InteractiveRedirectToModuleServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SSOUtils.isUserAuthenticated(request)) {
            log.debug("User is authenticated redirecting to module");
            SSOUtils.redirectToModule(
                    request,
                    response);
        } else {
            SSOUtils.getSsoSession(request).setReauthenticate(false);
            response.sendRedirect(request.getContextPath() + SSOConstants.INTERACTIVE_LOGIN_FORM_URI);
        }
    }
}
