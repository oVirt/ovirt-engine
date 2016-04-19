package org.ovirt.engine.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchUserServlet extends HttpServlet {

    private static final long serialVersionUID = 9210030009170727847L;
    private static final Logger log = LoggerFactory.getLogger(SwitchUserServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            SsoOAuthServiceUtils.revoke((String) request.getSession(true).getAttribute(WelcomeUtils.TOKEN));
            HttpSession session = request.getSession();
            if (session != null) {
                session.invalidate();
            }
        } catch (Exception ex) {
            log.error("Unable to logout user: {}", ex.getMessage());
        }

        request.getRequestDispatcher(WelcomeUtils.LOGIN_URI).forward(request, response);
    }

}
