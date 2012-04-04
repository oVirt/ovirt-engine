package org.ovirt.engine.core.validate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/*
 * This servlet gets a session ID, validates it, and returns the user name which is logged in this session.
 */
public class ValidateSession extends HttpServlet {
    private static final long serialVersionUID = -6984391651645165467L;

    private String SESSION_ID_PARAMETER = "sessionID";
    private int SUCCESS_CODE = HttpURLConnection.HTTP_OK;
    private int FAILED_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;

    private static Log log = LogFactory.getLog(ValidateSession.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcessRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcessRequest(request, response);
    }

    private void doProcessRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sessionID = request.getParameter(SESSION_ID_PARAMETER);
        if (runQuery(request, response, sessionID)) {
            response.setStatus(SUCCESS_CODE);
            log.debug("Validate Session " + sessionID + " succeeded");
        } else {
            response.setStatus(FAILED_CODE);
            log.debug("Validate Session " + sessionID + " failed");
        }
    }

    private boolean runQuery(HttpServletRequest request, HttpServletResponse response, String sessionID) {
        BackendInternal backend = null;
        VdcQueryParametersBase params = null;
        VdcQueryReturnValue queryReturnValue = null;
        boolean returnValue = false;

        backend = (BackendInternal) EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
        log.debug("Calling ValidateSession query");

        params = new VdcQueryParametersBase();
        params.setHttpSessionId(sessionID);

        queryReturnValue = backend.runInternalQuery(VdcQueryType.ValidateSession, params);

        if (queryReturnValue != null) {
            returnValue = queryReturnValue.getSucceeded();
        } else {
            log.error("Got NULL from backend.RunQuery");
        }

        // We get the user name only in case the validation succeeded
        if (returnValue) {
            log.debug("Getting user name");
            printResult(queryReturnValue, response);
        }

        return returnValue;
    }

    private void printResult(VdcQueryReturnValue queryReturnValue, HttpServletResponse response) {
        VdcUser vdcUser = (VdcUser) queryReturnValue.getReturnValue();
        PrintWriter out;
        try {
            out = response.getWriter();
            out.print(vdcUser.getUserName());
        } catch (IOException e) {
            log.error("IO Exception while writing user name: ", e);
        }

    }
}
