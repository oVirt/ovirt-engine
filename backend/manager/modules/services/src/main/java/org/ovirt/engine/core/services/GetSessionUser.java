package org.ovirt.engine.core.services;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This servlet gets a session ID, validates it, checks if the logged in user is administrator, and if so returns its user name
 */
public class GetSessionUser extends HttpServlet {
    private static final long serialVersionUID = -6984391651645165467L;

    private static final String SESSION_ID_PARAMETER = "sessionID";
    private static final int SUCCESS_CODE = HttpURLConnection.HTTP_OK;
    private static final int FAILED_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;

    private static final Logger log = LoggerFactory.getLogger(GetSessionUser.class);

    @Inject
    private BackendInternal backend;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcessRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcessRequest(request, response);
    }

    private void doProcessRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sessionID = request.getParameter(SESSION_ID_PARAMETER);
        if (runQuery(response, sessionID)) {
            response.setStatus(SUCCESS_CODE);
            log.debug("Validate Session '{}' succeeded", sessionID);
        } else {
            response.setStatus(FAILED_CODE);
            log.debug("Validate Session '{}' failed", sessionID);
        }
    }

    private boolean runQuery(HttpServletResponse response, String sessionID) {
        boolean returnValue = false;

        log.debug("Calling ValidateSession query");

        QueryReturnValue queryReturnValue = backend.runInternalQuery(QueryType.ValidateSession,
                new QueryParametersBase(sessionID));

        if (queryReturnValue != null) {
            returnValue = queryReturnValue.getSucceeded();

            if (returnValue) {
                DbUser user = queryReturnValue.getReturnValue();

                // We get the user name only in case the validation succeeded, and the user is an administrator
                if (user.isAdmin()) {
                    log.debug("Getting user name");
                    printUPNToResponse(response, getUPN(user));
                } else {
                    log.error("User '{}' is not authorized to perform operation", user.getLoginName());
                    returnValue = false;
                }
            }
        } else {
            log.error("Got NULL from backend.RunQuery");
        }

        return returnValue;
    }

    private void printUPNToResponse(HttpServletResponse response, String upn) {
        try {
            response.getWriter().print(upn);
        } catch (IOException e) {
            log.error("Exception while writing user name: {}", e.getMessage());
            log.debug("Exception", e);
        }

    }

    private String getUPN(DbUser user) {
        String retVal = user.getLoginName();
        if (!retVal.contains("@")) {
            retVal = retVal + "@" + user.getDomain();
        }
        return retVal;
    }
}
