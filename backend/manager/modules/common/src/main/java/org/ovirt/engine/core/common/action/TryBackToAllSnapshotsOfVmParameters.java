package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -6915708674977777690L;

    private Guid dstSnapshotId;
    private boolean restoreMemory;
    private Set<Guid> imageIds;
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
