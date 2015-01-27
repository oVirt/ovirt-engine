package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotVDSParameters extends GlusterVolumeVDSParameters {
    protected Guid clusterId;

    public GlusterVolumeSnapshotVDSParameters() {
    }

    public GlusterVolumeSnapshotVDSParameters(Guid serverId, Guid clusterId, String volumeName) {
        super(serverId, volumeName);
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }
}
