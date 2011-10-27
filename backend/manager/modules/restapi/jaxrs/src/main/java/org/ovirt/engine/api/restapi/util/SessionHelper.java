package org.ovirt.engine.api.restapi.util;

import java.util.UUID;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.invocation.Current;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class SessionHelper {

    private Current current;
    private static ThreadLocal<String> sessionIdHolder = new ThreadLocal<String>();

    public void setCurrent(Current current) {
        this.current = current;
    }

    public Current getCurrent() {
        return current;
    }

    public <P extends VdcQueryParametersBase> P sessionize(P parameters) {
        Principal principal = current.get(Principal.class);
        if (principal != null) {
            parameters.setSessionId(getSessionId(principal));
        }
        return parameters;
    }

    public <P extends VdcActionParametersBase> P sessionize(P parameters) {
        Principal principal = current.get(Principal.class);
        return sessionize(parameters, principal);
    }

    public <P extends VdcActionParametersBase> P sessionize(P parameters, Principal principal) {
        if (principal != null) {
            parameters.setSessionId(getSessionId(principal));
        }
        return parameters;
    }

    /**
     * Fabricate a session ID.
     *
     * @param principal
     *            the current principal
     * @return a session ID to use for the short-lived login session
     */
    public static synchronized String getSessionId(Principal principal) {
        if (sessionIdHolder.get() == null) {
            String sessionId =  UUID.randomUUID() + "_"
            + principal.getUser() + "\\"
            + principal.getDomain();

            sessionIdHolder.set(sessionId);
        }
        return sessionIdHolder.get();
    }

    /**
     * Clean sessionId on Logout
     */
    public void clean() {
        sessionIdHolder.remove();
    }
}
