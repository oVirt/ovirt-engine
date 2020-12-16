package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveNegotiateAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 3918706828180944312L;
    private static Logger log = LoggerFactory.getLogger(InteractiveNegotiateAuthServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (SsoService.getSsoContext(request).getNegotiateAuthUtils().doAuth(request, response).getStatus()) {
        case Authn.AuthResult.NEGOTIATION_UNAUTHORIZED:
            log.debug("External authentication failed redirecting to url: {}",
                    SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
            break;
        case Authn.AuthResult.SUCCESS:
            log.debug("External authentication succeeded redirecting to module");
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI);
            break;
        case Authn.AuthResult.NEGOTIATION_INCOMPLETE:
            log.debug("External authentication incomplete");
            break;
        }
    }
}
