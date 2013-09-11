package org.ovirt.engine.core.redirect;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * The purpose of this servlet is to redirect requests to the home page of the
 * reports application if it is installed or to show an error message to the
 * user if it isn't. The location of the reports application is taken from the
 * configuration option <code>RedirectServletPageReports</code>, if it is empty
 * the servlet assumes that the reports application isn't installed.
 */
@SuppressWarnings("serial")
public class ReportsRedirectServlet extends HttpServlet {

    private void addAlert(PrintWriter out, String message) {
        out.print("<html><body><script>alert(\"" + message + "\");window.history.back()</script></body></html>");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String reportsUrl = Config.<String> GetValue(ConfigValues.RedirectServletReportsPage);
        if (StringUtils.isEmpty(reportsUrl)) {
            addAlert(out, "The reports application isn't installed.");
        }
        else {
            response.sendRedirect(reportsUrl);
        }
    }

}
