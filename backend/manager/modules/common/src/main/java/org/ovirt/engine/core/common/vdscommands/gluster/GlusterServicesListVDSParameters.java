package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Server ID and service names as parameters, Used by the "Gluster Services List" command.
 */
public class GlusterServicesListVDSParameters extends VdsIdVDSCommandParametersBase {
    private Set<String> serviceNames;

    public GlusterServicesListVDSParameters(Guid serverId, Set<String> serviceNames) {
        super(serverId);
        this.serviceNames = serviceNames;
    }

    public GlusterServicesListVDSParameters() {
    }

    public Set<String> getServiceNames() {
        return serviceNames;
    }
}
