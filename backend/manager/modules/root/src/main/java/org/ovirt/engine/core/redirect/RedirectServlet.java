package org.ovirt.engine.core.redirect;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

public class RedirectServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(RedirectServlet.class);
    private static String pagePrefix = "RedirectServlet";
    private static String pageSuffix = "Page";
    private static String errorSuffix = "PageError";

    // ***************************************************************************** //
    //
    //                                 IMPORTANT!!!
    //
    //  We must use page and error prefix and suffix, if not user can fetch any
    //  configuration value from the vdc_option table!
    //
    // ***************************************************************************** //

    private String getConfigValue(ConfigurationValues conf) {
        String retVal = null;
        BackendInternal backend = null;
        GetConfigurationValueParameters params = null;
        VdcQueryReturnValue v = null;

        try {
            backend = (BackendInternal) EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);

            params = new GetConfigurationValueParameters(conf, ConfigCommon.defaultConfigurationVersion);

            v = backend.runInternalQuery(VdcQueryType.GetConfigurationValue, params);
            if (v != null) {
                retVal = (v.getSucceeded() && StringUtils.isNotEmpty((String) v.getReturnValue()))
                    ? v.getReturnValue().toString() : null;
            } else {
                log.error("Redirect Servlet: Got NULL from backend.RunQuery!");
            }
        } catch (Throwable t) {
            log.error("Redirect Servlet: Caught exception while trying to run query: ", t);
        }

        return retVal;
    }

    protected void addAlert(PrintWriter out, String message) {
        out.print("<html><body><script>alert('" + message.replace('\'', '"') + "');window.history.back()</script></body></html>");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            String page = pagePrefix + request.getParameter("Page") + pageSuffix;
            if (request.getParameter("Page") == null) {
                addAlert(out, "Page parameter is mandatory");
            }
            else {
                String pageUrl = getConfigValue(ConfigurationValues.valueOf(page));
                if (pageUrl == null || pageUrl.trim().equals("")) {
                    String pageError = getConfigValue(ConfigurationValues.valueOf(pagePrefix + request.getParameter("Page") + errorSuffix));
                    if (pageError == null) {
                        addAlert(out, "Cannot find page: " + request.getParameter("Page"));
                    }
                    else {
                        addAlert(out, pageError);
                    }
                }
                else {
                    response.sendRedirect(pageUrl);
                }
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(400);
            addAlert(out, "Page: " + request.getParameter("Page") + " is not legal.");
        } catch (Exception e1) {
            response.setStatus(500);
            log.error("Redirect Servlet: Error", e1);
        } finally {
            out.close();
        }
        log.debug("Health Status servlet: close");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            doGet(request, response);
    }
}
