package org.ovirt.engine.core.bll.snapshots;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.OvfUtils;

/**
 * Validator that is used to test if there are snapshots in progress, etc.
 */
@Singleton
public class SnapshotsValidator {

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private DiskImageDao diskImageDao;

    /**
     * Return whether the VM is during a snapshot operation (running currently on the VM).
     *
     * @param vmId
     *            The VM to check for.
     * @return Is the VM during a snapshot operation or not.
     */
    public ValidationResult vmNotDuringSnapshot(Guid vmId) {
        return vmNotInStatus(vmId, SnapshotStatus.LOCKED, EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    /**
     * Verify that there is no merge running on same disk(s) of the provided snapshot.
     *
     * @param vmId
     *            The VM to check for.
     * @param mergedSnapshotId
     *            The snapshot to check
     * @return Is the VM during a snapshot on same disk(s).
     */
    public ValidationResult vmSnapshotDisksNotDuringMerge(Guid vmId, Guid mergedSnapshotId) {
        Set<Guid> mergedSnapshotDisksIds = getSnapshotDiskIds(mergedSnapshotId).collect(Collectors.toSet());
        boolean isVmDuringSnapshot =
                !mergedSnapshotDisksIds.isEmpty() &&
                getAllVmLockedSnapshotIds(vmId).flatMap(this::getSnapshotDiskIds).anyMatch(mergedSnapshotDisksIds::contains);

        return isVmDuringSnapshot ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT) :
                ValidationResult.VALID;
    }

    /**
     * Return whether the VM is in preview (running currently on a snapshot other than the active one).
     *
     * @param vmId
     *            The VM to check for.
     * @return Is the VM is preview or not.
     */
    public ValidationResult vmNotInPreview(Guid vmId) {
        return vmNotInStatus(vmId, SnapshotStatus.IN_PREVIEW, EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    /**
     * Return whether the VM has a snapshot in the given status.
     *
     * @param vmId
     *            The VM to check for.
     * @param status
     *            The status of a snapshot to look for
     * @param msg
     *            The validation error to return if the snapshot exists
     *
     * @return <code>true</code> if the VM dons't habe a snapshot in the given status.
     */
    private ValidationResult vmNotInStatus(Guid vmId, SnapshotStatus status, EngineMessage msg) {
        if (snapshotDao.exists(vmId, status)) {
            return new ValidationResult(msg);
        }

        return ValidationResult.VALID;
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
        return createSnapshotExistsResult(snapshotDao.exists(vmId, snapshotId));
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
     * Checks if the given snapshot's type is regular.
     */
    public ValidationResult isRegularSnapshot(Snapshot snapshot) {
        if (SnapshotType.REGULAR != snapshot.getType()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_TYPE_NOT_REGULAR);
        }

        return ValidationResult.VALID;
    }

    /**
     * Checks if the given snapshot's status is OK.
     *
     * @param snapshotId
     *            Snapshot ID to check.
     * @return Snapshot status OK or not
     */
    public ValidationResult isSnapshotStatusOK(Guid snapshotId) {
        return isSnapshotInStatus(snapshotId,
                SnapshotStatus.OK,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_SNAPSHOT_STATUS);
    }


    /**
     * Return whether the Snapshot is in the given status.
     *
     * @param snapshotId
     *            The snapshot ID to check for.
     * @param status
     *            The status of the snapshot to look for
     * @param msg
     *            The validation error to return if the snapshot not in the required status
     *
     * @return Snapshot is in the required status or not
     */
    private ValidationResult isSnapshotInStatus(Guid snapshotId, SnapshotStatus status,  EngineMessage msg) {
        Snapshot snap = snapshotDao.get(snapshotId);
        if (snap != null && snap.getStatus() != status) {
            return new ValidationResult(msg, String.format("$SnapshotStatus %1$s", snap.getStatus()));
        }

        return ValidationResult.VALID;
    }

    /**
     * Checks if the given snapshot's VM configuration in broken.
     */
    public ValidationResult snapshotVmConfigurationBroken(Snapshot snapshot, String vmName) {
        return !snapshot.isVmConfigurationBroken()
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION,
                String.format("$VmName %1$s", vmName),
                String.format("$SnapshotName %1$s", snapshot.getDescription()));
    }

    /**
     * Checks if that the destination lease domain ID belongs to one of the VM's snapshots.
     */
    public ValidationResult isLeaseDomainIdBelongsToSnapshot(Guid vmId, Guid dstLeaseDomainId) {
        List<Snapshot> allVmSnapshots = snapshotDao.getAllWithConfiguration(vmId);

        boolean leaseStorageDomainValid = allVmSnapshots.stream()
                .filter(snapshot -> snapshot.getVmConfiguration() != null)
                .anyMatch(snapshot -> dstLeaseDomainId.equals(OvfUtils.fetchLeaseDomainId(snapshot.getVmConfiguration())));

        return leaseStorageDomainValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_LEASE_DOMAIN_ID_IS_NOT_VALID);
    }

    /**
     * Create result that indicates if snapshot exists or not.
     *
     * @param snapshotExists
     *            Does the snapshot exist?
     * @return Result that either contains the suitable error or not.
     */
    private ValidationResult createSnapshotExistsResult(boolean snapshotExists) {
        return snapshotExists
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    /**
     * Return disk IDs (image groups) of the provided snapshot.
     *
     * @param snapshotId
     *          Snapshot IDs
     *
     * @return Stream of disks Id
     */
    private Stream<Guid> getSnapshotDiskIds(Guid snapshotId) {
        return diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId).stream().map(DiskImage::getId);
    }

    /**
     * Get all locked snapshot IDs of the provided Vm.
     *
     * @param vmId
     *          Vm Id
     *
     * @return Stream of locked snapshot IDs
     */
    private Stream<Guid> getAllVmLockedSnapshotIds(Guid vmId) {
        return snapshotDao.getAll(vmId)
                .stream()
                .filter(snapshot -> snapshot.getStatus() == SnapshotStatus.LOCKED)
                .map(Snapshot::getId);
    }
}
