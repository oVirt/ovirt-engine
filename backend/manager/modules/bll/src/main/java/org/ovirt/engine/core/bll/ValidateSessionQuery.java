package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/*
 * This query validates the session, returning the VdcUser which is logged in this session.
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
            VdcUser vdcUser = (VdcUser) getSessionUser(sessionID);
            if (vdcUser != null) {
                log.debug("Found session user");
                getQueryReturnValue().setReturnValue(vdcUser);
                getQueryReturnValue().setSucceeded(true);
            } else {
                log.debug("Didn't find session user");
            }
        }
    }

    protected Object getSessionUser(String sessionID) {
        return SessionDataContainer.getInstance().getUser(sessionID);
    }

    private final static Log log = LogFactory.getLog(ValidateSessionQuery.class);
}
