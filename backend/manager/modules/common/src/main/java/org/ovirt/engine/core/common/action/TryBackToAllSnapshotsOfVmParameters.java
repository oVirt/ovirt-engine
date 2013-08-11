package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 1862924807826485840L;
    private Guid dstSnapshotId;
    private boolean restoreMemory;

    public TryBackToAllSnapshotsOfVmParameters() {
        dstSnapshotId = Guid.Empty;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId);
        this.dstSnapshotId = dstSnapshotId;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory) {
        this(vmId, dstSnapshotId);
        this.restoreMemory = restoreMemory;
    }

    public Guid getDstSnapshotId() {
        return dstSnapshotId;
    }

    public boolean isRestoreMemory() {
        return restoreMemory;
    }

    public void setRestoreMemory(boolean restoreMemory) {
        this.restoreMemory = restoreMemory;
    }
}
