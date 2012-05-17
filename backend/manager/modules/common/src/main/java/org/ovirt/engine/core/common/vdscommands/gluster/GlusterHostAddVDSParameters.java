package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Host name as parameters,
 * Used by the "Add Gluster Host" command.
 */
public class GlusterHostAddVDSParameters extends VdsIdVDSCommandParametersBase {

    private String hostName;

    public GlusterHostAddVDSParameters(Guid serverId, String hostName) {
        super(serverId);
        setHostName(hostName);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

}
