package org.ovirt.engine.api.restapi.util;

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

    /**
     * Setting the sessionId on the parameters
     */
    public <P extends VdcActionParametersBase> P sessionize(P parameters) {
        parameters.setSessionId(getSessionId());
        return parameters;
    }

    /**
     * Setting the sessionId on the parameters
     */
    public <P extends VdcQueryParametersBase> P sessionize(P parameters){
        parameters.setSessionId(getSessionId());
        return parameters;
    }

    /**
     * Clean sessionId on Logout
     */
    public void clean() {
        sessionIdHolder.remove();
    }

    /**
     * Get the sessionId
     */
    public String getSessionId() {
        return sessionIdHolder.get();
    }

    /**
     * Set the sessionId
     */
    public void setSessionId(String sessionId) {
        sessionIdHolder.set(sessionId);
    }
}
