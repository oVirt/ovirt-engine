package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllManagedBlockSnapshotsParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -6969382030160997502L;

    private SnapshotActionEnum snapshotAction;
    private List<ManagedBlockStorageDisk> managedBlockStorageDisks;
    private List<Guid> snapshotsToRemove = new ArrayList<>();

    public RestoreAllManagedBlockSnapshotsParameters() {
    }

    public RestoreAllManagedBlockSnapshotsParameters(SnapshotActionEnum snapshotAction,
            List<ManagedBlockStorageDisk> managedBlockStorageDisks) {
        this.snapshotAction = snapshotAction;
        this.managedBlockStorageDisks = managedBlockStorageDisks;
    }

    public RestoreAllManagedBlockSnapshotsParameters(Guid vmId,
            SnapshotActionEnum snapshotAction,
            List<ManagedBlockStorageDisk> managedBlockStorageDisks) {
        super(vmId);
        this.snapshotAction = snapshotAction;
        this.managedBlockStorageDisks = managedBlockStorageDisks;
    }

    public SnapshotActionEnum getSnapshotAction() {
        return snapshotAction;
    }

    public void setSnapshotAction(SnapshotActionEnum snapshotAction) {
        this.snapshotAction = snapshotAction;
    }

    public List<ManagedBlockStorageDisk> getManagedBlockStorageDisks() {
        return managedBlockStorageDisks;
    }

    public void setManagedBlockStorageDisks(List<ManagedBlockStorageDisk> managedBlockStorageDisks) {
        this.managedBlockStorageDisks = managedBlockStorageDisks;
    }

    public List<Guid> getSnapshotsToRemove() {
        return snapshotsToRemove;
    }

    public void setSnapshotsToRemove(List<Guid> snapshotsToRemove) {
        this.snapshotsToRemove = snapshotsToRemove;
    }
}
