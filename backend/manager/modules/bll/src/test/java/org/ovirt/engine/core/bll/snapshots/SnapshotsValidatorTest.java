package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.SnapshotDao;

public class SnapshotsValidatorTest {

    /**
     * The class being tested.
     */
    @Spy
    private SnapshotsValidator validator = new SnapshotsValidator();

    @Mock
    private SnapshotDao snapshotDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        doReturn(snapshotDao).when(validator).getSnapshotDao();
    }

    @Test
    public void vmNotDuringSnapshotReturnsInvalidResultWhenInSnapshot() throws Exception {
        Guid vmId = new Guid();

        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(true);

        validateVmNotDuringSnapshot(vmId, false, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    @Test
    public void vmNotDuringSnapshotReturnsValidForNoSnapshotInProgress() throws Exception {
        Guid vmId = new Guid();

        when(snapshotDao.exists(vmId, SnapshotStatus.LOCKED)).thenReturn(false);

        validateVmNotDuringSnapshot(vmId, true, null);
    }

    /**
     * Run the method and validate that the result is as expected.
     *
     * @param vmId
     * @param isValid
     * @param message
     */
    private void validateVmNotDuringSnapshot(Guid vmId, boolean isValid, VdcBllMessages message) {
        ValidationResult validationResult = validator.vmNotDuringSnapshot(vmId);
        assertEquals(isValid, validationResult.isValid());
        assertEquals(message, validationResult.getMessage());
    }
}
