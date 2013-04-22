package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Server ID and service names as parameters, Used by the "Gluster Services List" command.
 */
public class GlusterServicesListVDSParameters extends VdsIdVDSCommandParametersBase {
    private List<String> serviceNames;

    public GlusterServicesListVDSParameters(Guid serverId, List<String> serviceNames) {
        super(serverId);
        this.serviceNames = serviceNames;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }
}
