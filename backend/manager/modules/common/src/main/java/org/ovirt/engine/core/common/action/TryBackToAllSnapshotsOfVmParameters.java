package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = 3617937608131413484L;

    private Guid dstSnapshotId;
    private boolean restoreMemory;
    private List<DiskImage> disks;
    private boolean restoreLease;
    private Guid dstLeaseDomainId;
    // keep the lease action in order to save calculation on endSuccessfully phase,
    // update vmStatic when the preview finished
    private LeaseAction leaseAction;

    public TryBackToAllSnapshotsOfVmParameters() {
        dstSnapshotId = Guid.Empty;
        restoreMemory = true;
        restoreLease = true;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId);
        this.dstSnapshotId = dstSnapshotId;
        restoreMemory = true;
        restoreLease = true;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory) {
        this(vmId, dstSnapshotId);
        this.restoreMemory = restoreMemory;
        restoreLease = true;
    }

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId, boolean restoreMemory, List<DiskImage> disks) {
        this(vmId, dstSnapshotId, restoreMemory);
        this.disks = disks;
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

    public List<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskImage> disks) {
        this.disks = disks;
    }

    public boolean isRestoreLease() {
        return restoreLease;
    }

    public void setRestoreLease(boolean restoreLease) {
        this.restoreLease = restoreLease;
    }

    public Guid getDstLeaseDomainId() {
        return dstLeaseDomainId;
    }

    public void setDstLeaseDomainId(Guid dstLeaseDomainId) {
        this.dstLeaseDomainId = dstLeaseDomainId;
    }

    public LeaseAction getLeaseAction() {
        return leaseAction;
    }

    public void setLeaseAction(LeaseAction leaseAction) {
        this.leaseAction = leaseAction;
    }

    public enum LeaseAction {
        CREATE_NEW_LEASE,
        UPDATE_LEASE_INFO,
        UPDATE_LEASE_INFO_AND_LEASE_DOMAIN_ID,
        DO_NOTHING
    }
}
