package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This query validates the session, returning the user which is logged in this session.
 */
public class ValidateSessionQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    private static final Logger log = LoggerFactory.getLogger(ValidateSessionQuery.class);

    public ValidateSessionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        log.debug("Calling ValidateSession");
        validateSession(getParameters().getSessionId());
        log.debug("ValidateSession ended");
    }

    private void validateSession(String sessionID) {
        getQueryReturnValue().setSucceeded(false);
        if (sessionID != null) {
            log.debug("Input session ID is '{}'", sessionID);
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
        return getSessionDataContainer().getUser(sessionID, false);
    }
}
