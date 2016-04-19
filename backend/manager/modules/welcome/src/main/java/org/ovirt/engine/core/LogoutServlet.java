package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 5429007478757020118L;

    private static final Logger log = LoggerFactory.getLogger(LogoutServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String error = null;
        String error_code = null;
        try {
            Map<String, Object> revokeResponse =  SsoOAuthServiceUtils.revoke(
                    (String) request.getSession(true).getAttribute(WelcomeUtils.TOKEN));
            error = (String) revokeResponse.get(WelcomeUtils.ERROR);
            error_code = (String) revokeResponse.get(WelcomeUtils.ERROR_CODE);
            HttpSession session = request.getSession();
            if (session != null) {
                session.invalidate();
            }
        } catch (Exception ex) {
            log.error("Unable to logout user: {}", ex.getMessage());
        }
        response.sendRedirect(new URLBuilder(WelcomeUtils.getOauth2CallbackUrl(request))
                .addParameter(WelcomeUtils.ERROR, StringUtils.defaultIfEmpty(error, ""))
                .addParameter(WelcomeUtils.ERROR_CODE, StringUtils.defaultIfEmpty(error_code, "")).build());
    }
}
