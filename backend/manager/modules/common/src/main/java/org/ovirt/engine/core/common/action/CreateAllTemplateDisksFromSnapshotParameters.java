package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateAllTemplateDisksFromSnapshotParameters extends CreateAllTemplateDisksParameters {

    private static final long serialVersionUID = 8279629302458644328L;

    private Guid snapshotId;

    public CreateAllTemplateDisksFromSnapshotParameters() {
    }

    public CreateAllTemplateDisksFromSnapshotParameters(Guid vmId) {
        super(vmId);
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }
}
