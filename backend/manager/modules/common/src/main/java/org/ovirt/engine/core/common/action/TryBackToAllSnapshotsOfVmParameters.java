package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = 1862924807826485840L;
    private Guid dstSnapshotId;
    private boolean restoreMemory;
    private List<DiskImage> disks;

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

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory, List<DiskImage> disks) {
        this(vmId, dstSnapshotId, restoreMemory);
        this.disks = disks;
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

    public List<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskImage> disks) {
        this.disks = disks;
    }
}
