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
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
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
    private Snapshot snapshot;

    @Before
    public void setUp() {
        vmId = Guid.newGuid();
        snapshot = new Snapshot();
        snapshot.setId(Guid.newGuid());
        snapshot.setType(Snapshot.SnapshotType.REGULAR);
        doReturn(snapshotDao).when(validator).getSnapshotDao();
    }

    @Test
    public void vmNotDuringSnapshotReturnsInvalidResultWhenInSnapshot() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(true);
        validateInvalidResult(validator.vmNotDuringSnapshot(vmId),
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
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
                EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void vmNotInPreviewReturnsValidForNoSnapshotInProgress() throws Exception {
        when(snapshotDao.exists(vmId, SnapshotStatus.IN_PREVIEW)).thenReturn(false);
        validateValidResult(validator.vmNotInPreview(vmId));
    }

    @Test
    public void snapshotExistsByGuidReturnsInvalidResultWhenNoSnapshot() throws Exception {
        when(snapshotDao.exists(vmId, snapshot.getId())).thenReturn(false);
        validateInvalidResult(validator.snapshotExists(vmId, snapshot.getId()),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsByGuidReturnsValidResultWhenSnapshotExists() throws Exception {
        when(snapshotDao.exists(vmId, snapshot.getId())).thenReturn(true);
        validateValidResult(validator.snapshotExists(vmId, snapshot.getId()));
    }

    @Test
    public void snapshotTypeSupported() throws Exception {
        snapshot.setType(SnapshotType.REGULAR);
        validateValidResult(validator.isRegularSnapshot(snapshot));
    }

    @Test
    public void snapshotTypeNotSupported() throws Exception {
        snapshot.setType(SnapshotType.ACTIVE);
        validateInvalidResult(validator.isRegularSnapshot(snapshot),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_TYPE_NOT_REGULAR);
    }

    @Test
    public void snapshotExistsBySnapshotReturnsInvalidResultWhenNoSnapshot() throws Exception {
        validateInvalidResult(validator.snapshotExists(null),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsBySnapshotReturnsValidResultWhenSnapshotExists() throws Exception {
        validateValidResult(validator.snapshotExists(snapshot));
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
    private static void validateInvalidResult(ValidationResult validationResult, EngineMessage message) {
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
    private static void validateResult(ValidationResult validationResult, boolean isValid, EngineMessage message) {
        assertEquals(isValid, validationResult.isValid());
        assertEquals(message, validationResult.getMessage());
    }
}
