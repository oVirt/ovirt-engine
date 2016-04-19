package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.InteractiveAuth;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;

public class InteractiveNextAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1188460579367588817L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Stack<InteractiveAuth> authStack = SsoUtils.getSsoSession(request).getAuthStack();

        if (authStack == null || authStack.isEmpty()) {
            SsoUtils.redirectToErrorPage(request, response,
                    new OAuthException(SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT, "Authentication required."));
        } else {
            SsoUtils.getSsoSession(request).setStatus(SsoSession.Status.inprogress);
            response.sendRedirect(authStack.pop().getAuthUrl(request, response));
        }
    }
}
