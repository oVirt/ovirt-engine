package org.ovirt.engine.core.bll.snapshots;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

@ExtendWith(MockitoExtension.class)
public class SnapshotsValidatorTest {

    /**
     * The class being tested.
     */
    @InjectMocks
    private SnapshotsValidator validator = new SnapshotsValidator();

    @Mock
    private SnapshotDao snapshotDao;
    private Guid vmId;
    private Snapshot snapshot;

    @BeforeEach
    public void setUp() {
        vmId = Guid.newGuid();
        snapshot = new Snapshot();
        snapshot.setId(Guid.newGuid());
        snapshot.setType(Snapshot.SnapshotType.REGULAR);
    }

    @Test
    public void vmNotDuringSnapshotReturnsInvalidResultWhenInSnapshot() {
        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(true);
        validateInvalidResult(validator.vmNotDuringSnapshot(vmId),
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    @Test
    public void vmNotDuringSnapshotReturnsValidForNoSnapshotInProgress() {
        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(false);
        validateValidResult(validator.vmNotDuringSnapshot(vmId));
    }

    @Test
    public void vmNotInPreviewReturnsInvalidResultWhenInSnapshot() {
        when(snapshotDao.exists(vmId, SnapshotStatus.IN_PREVIEW)).thenReturn(true);
        validateInvalidResult(validator.vmNotInPreview(vmId),
                EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void vmNotInPreviewReturnsValidForNoSnapshotInProgress() {
        when(snapshotDao.exists(vmId, SnapshotStatus.IN_PREVIEW)).thenReturn(false);
        validateValidResult(validator.vmNotInPreview(vmId));
    }

    @Test
    public void snapshotExistsByGuidReturnsInvalidResultWhenNoSnapshot() {
        when(snapshotDao.exists(vmId, snapshot.getId())).thenReturn(false);
        validateInvalidResult(validator.snapshotExists(vmId, snapshot.getId()),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsByGuidReturnsValidResultWhenSnapshotExists() {
        when(snapshotDao.exists(vmId, snapshot.getId())).thenReturn(true);
        validateValidResult(validator.snapshotExists(vmId, snapshot.getId()));
    }

    @Test
    public void snapshotTypeSupported() {
        snapshot.setType(SnapshotType.REGULAR);
        validateValidResult(validator.isRegularSnapshot(snapshot));
    }

    @Test
    public void snapshotTypeNotSupported() {
        snapshot.setType(SnapshotType.ACTIVE);
        validateInvalidResult(validator.isRegularSnapshot(snapshot),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_TYPE_NOT_REGULAR);
    }

    @Test
    public void snapshotExistsBySnapshotReturnsInvalidResultWhenNoSnapshot() {
        validateInvalidResult(validator.snapshotExists(null),
                EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void snapshotExistsBySnapshotReturnsValidResultWhenSnapshotExists() {
        validateValidResult(validator.snapshotExists(snapshot));
    }

    @Test
    public void validateSnapshotStatusOK() {
        when(snapshotDao.get(snapshot.getId())).thenReturn(snapshot);
        snapshot.setStatus(SnapshotStatus.OK);
        validateValidResult(validator.isSnapshotStatusOK(snapshot.getId()));
    }

    @Test
    public void validateSnapshotStatusWhenSnapshotStatusLocked() {
        when(snapshotDao.get(snapshot.getId())).thenReturn(snapshot);
        snapshot.setStatus(SnapshotStatus.LOCKED);
        validateInvalidResult(validator.isSnapshotStatusOK(snapshot.getId()),
                EngineMessage.ACTION_TYPE_FAILED_INVALID_SNAPSHOT_STATUS);
    }

    @Test
    public void validateSnapshotStatusWhenSnapshotStatusInPreview() {
        when(snapshotDao.get(snapshot.getId())).thenReturn(snapshot);
        snapshot.setStatus(SnapshotStatus.IN_PREVIEW);
        validateInvalidResult(validator.isSnapshotStatusOK(snapshot.getId()),
                EngineMessage.ACTION_TYPE_FAILED_INVALID_SNAPSHOT_STATUS);
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
        if (!isValid) {
            assertThat(validationResult, failsWith(message));
        }
    }
}
