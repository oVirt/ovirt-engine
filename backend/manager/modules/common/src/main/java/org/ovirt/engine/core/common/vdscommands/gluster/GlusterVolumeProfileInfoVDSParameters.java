package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameter class with serverId, clusterId and volume name as parameter. <br>
 * This will be used directly by Gluster Volume Profile Info Query.
 */
public class GlusterVolumeProfileInfoVDSParameters extends GlusterVolumeVDSParameters {
    private Guid clusterId;

    public GlusterVolumeProfileInfoVDSParameters(Guid clusterId, Guid serverId, String volumeName) {
        super(serverId, volumeName);
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

}
