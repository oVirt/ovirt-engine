package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private boolean newHost = false;

    public RemoveVdsVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public RemoveVdsVDSCommandParameters() {
    }

    public RemoveVdsVDSCommandParameters(Guid vdsId, boolean newHost) {
        super(vdsId);
        this.newHost = newHost;
    }

    public boolean isNewHost() {
        return newHost;
    }

    public void setNewHost(boolean errorIfHostDoesntExist) {
        this.newHost = errorIfHostDoesntExist;
    }

}
