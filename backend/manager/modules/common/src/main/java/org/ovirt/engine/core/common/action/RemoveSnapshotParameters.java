package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -2684524270498397962L;

    private Guid snapshotId;

    public RemoveSnapshotParameters(Guid snapshotId, Guid vmGuid) {
        super(vmGuid);
        this.snapshotId = snapshotId;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid value) {
        snapshotId = value;
    }

    public RemoveSnapshotParameters() {
        snapshotId = Guid.Empty;
    }
}
