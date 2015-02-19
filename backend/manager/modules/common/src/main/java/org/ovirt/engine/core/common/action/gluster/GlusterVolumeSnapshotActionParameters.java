package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotActionParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = -5148741622108406754L;

    private String snapshotName;
    private boolean force;

    public GlusterVolumeSnapshotActionParameters() {
    }

    public GlusterVolumeSnapshotActionParameters(Guid volumeId, String snapshotName, boolean force) {
        super(volumeId);
        this.snapshotName = snapshotName;
        this.force = force;
    }

    public String getSnapshotName() {
        return this.snapshotName;
    }

    public void setSnapshotName(String name) {
        this.snapshotName = name;
    }

    public boolean getForce() {
        return this.force;
    }

    public void setForce(boolean value) {
        this.force = value;
    }
}
