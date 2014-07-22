package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/*
 * This query validates the session, returning the user which is logged in this session.
 */
public class ValidateSessionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public ValidateSessionQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        log.debug("Calling ValidateSession");
        validateSession(getParameters().getSessionId());
        log.debug("ValidateSession ended");
    }

    private void validateSession(String sessionID) {
        getQueryReturnValue().setSucceeded(false);
        if (sessionID != null) {
            log.debug("Input session ID is: " + sessionID);
            DbUser user = (DbUser) getSessionUser(sessionID);
            if (user != null) {
                log.debug("Found session user");
                getQueryReturnValue().setReturnValue(user);
                getQueryReturnValue().setSucceeded(true);
            } else {
                getQueryReturnValue().setExceptionString("Session does not exist.");
                log.debug("Didn't find session user");
            }
        }
    }

    protected Object getSessionUser(String sessionID) {
        return SessionDataContainer.getInstance().getUser(sessionID, false);
    }

    private final static Log log = LogFactory.getLog(ValidateSessionQuery.class);
}
