package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;

public class CreateGlusterVolumeSnapshotParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = 2015321730118872975L;

    private GlusterVolumeSnapshotEntity snapshot;

    private boolean force;

    public CreateGlusterVolumeSnapshotParameters() {
    }

    public CreateGlusterVolumeSnapshotParameters(GlusterVolumeSnapshotEntity snapshot) {
        super(snapshot.getVolumeId());
        this.snapshot = snapshot;
    }

    public CreateGlusterVolumeSnapshotParameters(GlusterVolumeSnapshotEntity snapshot,
            boolean force) {
        super(snapshot.getVolumeId());
        this.snapshot = snapshot;
        this.force = force;
    }

    public GlusterVolumeSnapshotEntity getSnapshot() {
        return this.snapshot;
    }

    public void setSnapshot(GlusterVolumeSnapshotEntity snapshot) {
        this.snapshot = snapshot;
    }

    public boolean getForce() {
        return this.force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
