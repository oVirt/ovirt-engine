package org.ovirt.engine.core.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This class is a servlet implementation, aimed to report fundemental health of engine.
 * The servlet URL is: /OvirtEngineWeb/HealthStatus (as defined in web.xml), and these
 * are the possible HTTP return codes:
 * - 200: OK
 * - 500: Unable to connect to DB.
 * - 404/other: Depending on Tomcat/Jboss state, servlet may be unavailable or connection refused.
 * If the servlet is unable to contact the backend bean, it'll write an apropriate message to out.
 *
 */
public class HealthStatus extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HealthStatus.class);

    @Inject
    private BackendInternal backend;

    private boolean runQuery(PrintWriter out) {
        boolean fReturn = false;

        try {
            log.debug("Calling CheckDBConnection query");

            QueryParametersBase params = new QueryParametersBase();

            QueryReturnValue v = backend.runInternalQuery(QueryType.CheckDBConnection, params);
            if (v != null) {
                fReturn = v.getSucceeded();
                out.print(fReturn ? "DB Up!" : "DB Down!");
            } else {
                log.error("Got NULL from backend.RunQuery!");
            }
        } catch (Throwable t) {
            String msg = "Unable to contact Database!";
            if (backend == null) {
                msg = "Unable to contact Backend!";
            }
            out.print(msg);
            log.error(msg, t);
            fReturn = false;
        }

        return fReturn;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Health Status servlet: entry");
        response.setContentType("text/html");

        try (PrintWriter out = response.getWriter()) {
            if (runQuery(out)) {
                out.print("Welcome to Health Status!");
                log.debug("Succeeded to run Health Status.");
            } else {
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                log.error("Failed to run Health Status.");
            }
        } catch (Exception e) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            log.error("Error calling runQuery: ", e);
        }
        log.debug("Health Status servlet: close");
    }
}
