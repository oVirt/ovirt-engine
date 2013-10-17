package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Host name or Ip as parameter,
 * Used by the "Add Gluster Server" command.
 */
public class AddGlusterServerVDSParameters extends VdsIdVDSCommandParametersBase {

    private String hostnameOrIp;

    public AddGlusterServerVDSParameters(Guid serverId, String hostnameOrIp) {
        super(serverId);
        setHostnameOrIp(hostnameOrIp);
    }

    public AddGlusterServerVDSParameters() {
    }

    public String getHostnameOrIp() {
        return hostnameOrIp;
    }

    public void setHostnameOrIp(String hostnameOrIp) {
        this.hostnameOrIp = hostnameOrIp;
    }

}
