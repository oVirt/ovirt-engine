package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameter class with host name as parameter. <br>
 * This will be used directly by Gluster Host Remove command.
 */
public class GlusterHostRemoveVDSParameters extends VdsIdVDSCommandParametersBase {
    private final String hostName;
    private boolean forceAction;

    public GlusterHostRemoveVDSParameters(Guid serverId, String hostName, boolean forceAction) {
        super(serverId);
        this.hostName = hostName;
        setForceAction(forceAction);
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

}

