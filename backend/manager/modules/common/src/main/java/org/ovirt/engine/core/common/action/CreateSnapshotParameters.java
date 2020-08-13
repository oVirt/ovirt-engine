package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateSnapshotParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = 2765338197999154172L;

    private boolean isLiveSnapshot;

    public CreateSnapshotParameters() {
    }

    public CreateSnapshotParameters(Guid imageId) {
        super(imageId);
    }

    public CreateSnapshotParameters(ImagesActionsParametersBase other) {
        super(other);
    }

    public boolean isLiveSnapshot() {
        return isLiveSnapshot;
    }

    public void setLiveSnapshot(boolean isLiveSnapshot) {
        this.isLiveSnapshot = isLiveSnapshot;
    }
}
