package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Locale;
import java.util.Stack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.api.InteractiveAuth;
import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.SsoService;

public class InteractiveNextAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1188460579367588817L;

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Stack<InteractiveAuth> authStack = SsoService.getSsoSession(request).getAuthStack();

        if (authStack == null || authStack.isEmpty()) {
            SsoService.redirectToErrorPage(request,
                    response,
                    new OAuthException(
                            SsoService.getSsoSession(request).isOpenIdScope()
                                    ? SsoConstants.ERR_CODE_OPENID_LOGIN_REQUIRED
                                    : SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT,
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_ERROR_AUTHENTICATION_REQUIRED,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE))));
        } else {
            SsoService.getSsoSession(request).setStatus(SsoSession.Status.inprogress);
            response.sendRedirect(authStack.pop().getAuthUrl(request, response));
        }
    }
}
