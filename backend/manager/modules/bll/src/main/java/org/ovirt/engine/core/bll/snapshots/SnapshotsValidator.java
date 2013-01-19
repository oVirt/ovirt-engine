package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Validator that is used to test if there are snapshots in progress, etc.
 */
public class SnapshotsValidator {

    /**
     * Return if the VM is during a snapshot operation (running currently on the VM).
     *
     * @param vmId
     *            The VM to check for.
     * @return Is the VM during a snapshot operation or not.
     */
    public ValidationResult vmNotDuringSnapshot(Guid vmId) {
        if (getSnapshotDao().exists(vmId, SnapshotStatus.LOCKED)) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
        }

        return ValidationResult.VALID;
    }

    /**
     * Check that the given snapshot is not broken, and if it is then return the corresponding error.
     *
     * @param snapshot
     *            The snapshot to check.
     * @return Is snapshot broken or not.
     */
    public ValidationResult snapshotNotBroken(Snapshot snapshot) {
        return SnapshotStatus.BROKEN == snapshot.getStatus() ?
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_IS_BROKEN) : ValidationResult.VALID;
    }

    /**
     * Check if the given snapshot id exists for the given VM in the DB.
     *
     * @param vmId
     *            ID of VM to check for.
     * @param snapshotId
     *            Snapshot ID to check.
     * @return Snapshot exists or not.
     */
    public ValidationResult snapshotExists(Guid vmId, Guid snapshotId) {
        return createSnapshotExistsResult(getSnapshotDao().exists(vmId, snapshotId));
    }

    /**
     * Check if the given snapshot is null, then it means it doesn't exist.
     *
     * @param snapshot
     *            Snapshot to check.
     * @return Snapshot exists (not null) or not.
     */
    public ValidationResult snapshotExists(Snapshot snapshot) {
        return createSnapshotExistsResult(snapshot != null);
    }

    /**
     * Create result that indicates if snapshot exists or not.
     *
     * @param snapshotExists
     *            Does the snapshot exist?
     * @return Result that either contains the suitable error or not.
     */
    private static ValidationResult createSnapshotExistsResult(boolean snapshotExists) {
        return snapshotExists
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }
}
