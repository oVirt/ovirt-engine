package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class TerminateSessionParameters extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -8545136602971701926L;

    /**
     * Database id of a session of a user to logout
     */
    private long terminatedSessionDbId;

    private TerminateSessionParameters() {
        this(-1);
    }

    public TerminateSessionParameters(long sessionDbId) {
        this.terminatedSessionDbId = sessionDbId;
    }

    public long getTerminatedSessionDbId() {
        return terminatedSessionDbId;
    }
}
