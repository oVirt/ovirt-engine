package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Server ID and Cluster ID as parameters, Used by the "Gluster Volumes List" command.
 */
public class GlusterVolumesListVDSParameters extends VdsIdVDSCommandParametersBase {

    private Guid clusterId;

    public GlusterVolumesListVDSParameters(Guid serverId, Guid clusterId) {
        super(serverId);
        this.clusterId = clusterId;
    }

    public GlusterVolumesListVDSParameters() {
    }

    public Guid getClusterId() {
        return clusterId;
    }
}
