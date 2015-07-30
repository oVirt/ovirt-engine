package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeInfoVDSParameters extends GlusterVolumeVDSParameters {
    private Guid clusterId;

    public GlusterVolumeInfoVDSParameters(Guid upServerId, Guid clusterId, String volumeName) {
        super(upServerId, volumeName);
        this.clusterId = clusterId;
    }

    public GlusterVolumeInfoVDSParameters() {
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid id) {
        this.clusterId = id;
    }
}
