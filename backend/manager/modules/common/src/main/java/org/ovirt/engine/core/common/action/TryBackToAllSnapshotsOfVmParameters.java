package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -6915708674977777690L;

    private Guid dstSnapshotId;
    private boolean restoreMemory;
    private Set<Guid> imageIds;

    public TryBackToAllSnapshotsOfVmParameters() {
        dstSnapshotId = Guid.Empty;
        restoreMemory = true;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId);
        this.dstSnapshotId = dstSnapshotId;
        restoreMemory = true;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory) {
        this(vmId, dstSnapshotId);
        this.restoreMemory = restoreMemory;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory, Set<Guid> imageIds) {
        this(vmId, dstSnapshotId, restoreMemory);
        this.imageIds = imageIds;
    }

    public Guid getDstSnapshotId() {
        return dstSnapshotId;
    }

    public void setDstSnapshotId(Guid dstSnapshotId) {
        this.dstSnapshotId = dstSnapshotId;
    }

    public boolean isRestoreMemory() {
        return restoreMemory;
    }

    public void setRestoreMemory(boolean restoreMemory) {
        this.restoreMemory = restoreMemory;
    }

    public Set<Guid> getImageIds() {
        return imageIds;
    }

    public void setImageIds(Set<Guid> imageIds) {
        this.imageIds = imageIds;
    }
}
