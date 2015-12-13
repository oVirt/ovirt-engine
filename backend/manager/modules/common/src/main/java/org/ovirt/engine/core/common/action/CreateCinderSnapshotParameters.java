package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;

public class CreateCinderSnapshotParameters extends ImagesContainterParametersBase{
    private SnapshotType snapshotType;

    public CreateCinderSnapshotParameters(Guid imageId) {
        super(imageId);
    }

    public CreateCinderSnapshotParameters() {
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }
}
