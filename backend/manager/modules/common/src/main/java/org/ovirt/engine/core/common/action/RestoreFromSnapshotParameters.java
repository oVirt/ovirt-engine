package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RestoreFromSnapshotParameters extends ImagesContainterParametersBase {

    private static final long serialVersionUID = 7601780627830362597L;
    private boolean removeParent;
    private Snapshot snapshot;
    private CinderDisk cinderDiskToBeRemoved;

    private Guid removedSnapshotId;

    public RestoreFromSnapshotParameters() {
    }

    public RestoreFromSnapshotParameters(Guid imageId,
            Guid containerId,
            Snapshot snapshot,
            Guid removedSnapshotId) {
        super(imageId, containerId);

        this.snapshot = snapshot;
        this.removedSnapshotId = removedSnapshotId;
    }

    public CinderDisk getCinderDiskToBeRemoved() {
        return cinderDiskToBeRemoved;
    }

    public void setCinderDiskToBeRemoved(CinderDisk cinderDiskToBeRemoved) {
        this.cinderDiskToBeRemoved = cinderDiskToBeRemoved;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public Guid getRemovedSnapshotId() {
        return removedSnapshotId;
    }

    public boolean isRemoveParent() {
        return removeParent;
    }

    public void setRemoveParent(boolean removeParent) {
        this.removeParent = removeParent;
    }
}
