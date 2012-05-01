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

        return new ValidationResult();
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
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_IS_BROKEN) : new ValidationResult();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }
}
