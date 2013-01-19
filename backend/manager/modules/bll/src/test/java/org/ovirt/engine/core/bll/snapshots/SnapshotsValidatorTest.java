package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.SnapshotDao;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotsValidatorTest {

    /**
     * The class being tested.
     */
    @Spy
    private SnapshotsValidator validator = new SnapshotsValidator();

    @Mock
    private SnapshotDao snapshotDao;
    private Guid vmId;
    private Guid snapshotId;

    @Before
    public void setUp() {
        vmId = Guid.NewGuid();
        snapshotId = Guid.NewGuid();
        doReturn(snapshotDao).when(validator).getSnapshotDao();
    }

    @Test
    public void vmNotDuringSnapshotReturnsInvalidResultWhenInSnapshot() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(true);
        validateInvalidResult(validator.vmNotDuringSnapshot(vmId),
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    @Test
    public void vmNotDuringSnapshotReturnsValidForNoSnapshotInProgress() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(false);
        validateValidResult(validator.vmNotDuringSnapshot(vmId));
    }

    @Test
    public void vmNotInPreviewReturnsInvalidResultWhenInSnapshot() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.IN_PREVIEW)).thenReturn(true);
        validateInvalidResult(validator.vmNotInPreview(vmId),
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void vmNotInPreviewReturnsValidForNoSnapshotInProgress() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.IN_PREVIEW)).thenReturn(false);
        validateValidResult(validator.vmNotInPreview(vmId));
    }

    @Test
    public void snapshotExistsByGuidReturnsInvalidResultWhenNoSnapshot() throws Exception {
        when(snapshotDao.exists(vmId, snapshotId)).thenReturn(false);
        validateInvalidResult(validator.snapshotExists(vmId, snapshotId),
                VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsByGuidReturnsValidResultWhenSnapshotExists() throws Exception {
        when(snapshotDao.exists(vmId, snapshotId)).thenReturn(true);
        validateValidResult(validator.snapshotExists(vmId, snapshotId));
    }

    @Test
    public void snapshotExistsBySnapshotReturnsInvalidResultWhenNoSnapshot() throws Exception {
        validateInvalidResult(validator.snapshotExists(null),
                VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsBySnapshotReturnsValidResultWhenSnapshotExists() throws Exception {
        validateValidResult(validator.snapshotExists(new Snapshot()));
    }

    @Test
    public void snapshotNotBrokenReturnsInvalidResult() throws Exception {
        Snapshot snapshot = new Snapshot();
        snapshot.setStatus(SnapshotStatus.BROKEN);

        validateInvalidResult(validator.snapshotNotBroken(snapshot),
                VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_IS_BROKEN);
    }

    @Test
    public void snapshotNotBrokenReturnsValidResult() throws Exception {
        validateValidResult(validator.snapshotNotBroken((new Snapshot())));
    }

    /**
     * Validate that the given result is valid.
     *
     * @param validationResult
     *            The result.
     */
    private static void validateValidResult(ValidationResult validationResult) {
        validateResult(validationResult, true, null);
    }

    /**
     * Validate that the given result is invalid and contains the given message.
     *
     * @param validationResult
     *            The result.
     * @param message
     *            The error to expect.
     */
    private static void validateInvalidResult(ValidationResult validationResult, VdcBllMessages message) {
        validateResult(validationResult, false, message);
    }

    /**
     * Validate that the result is as expected.
     *
     * @param validationResult
     *            The result.
     * @param isValid
     *            Should be valid or not?
     * @param message
     *            The error to expect.
     */
    private static void validateResult(ValidationResult validationResult, boolean isValid, VdcBllMessages message) {
        assertEquals(isValid, validationResult.isValid());
        assertEquals(message, validationResult.getMessage());
    }
}
