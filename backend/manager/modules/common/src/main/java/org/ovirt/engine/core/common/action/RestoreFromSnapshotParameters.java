package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.compat.Guid;

public class RestoreFromSnapshotParameters extends ImagesContainterParametersBase {

    private static final long serialVersionUID = 7601780627830362597L;
    private Snapshot snapshot;

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

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public Guid getRemovedSnapshotId() {
        return removedSnapshotId;
    }
}
