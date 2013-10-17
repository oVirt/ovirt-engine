package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameter class with serverId, hostnameOrIp and forceAction as parameter. <br>
 * This will be used directly by Remove Gluster Server command.
 */
public class RemoveGlusterServerVDSParameters extends VdsIdVDSCommandParametersBase {
    private String hostnameOrIp;
    private boolean forceAction;

    public RemoveGlusterServerVDSParameters(Guid serverId, String hostnameOrIp, boolean forceAction) {
        super(serverId);
        this.hostnameOrIp = hostnameOrIp;
        setForceAction(forceAction);
    }

    public RemoveGlusterServerVDSParameters() {
    }

    public String getHostnameOrIp() {
        return hostnameOrIp;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

}

