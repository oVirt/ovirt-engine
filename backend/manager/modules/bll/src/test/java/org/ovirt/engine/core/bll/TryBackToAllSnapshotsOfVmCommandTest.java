package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;

/** A test case for the {@link TryBackToAllSnapshotsOfVmCommand} class. */
@RunWith(MockitoJUnitRunner.class)
public class TryBackToAllSnapshotsOfVmCommandTest {
    private TryBackToAllSnapshotsOfVmCommand<TryBackToAllSnapshotsOfVmParameters> cmd;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmDAO vmDao;

    private VM vm;
    private Snapshot snapshot;

    @Before
    public void setUp() {
        Guid vmId = Guid.NewGuid();
        vm = new VM();
        vm.setId(vmId);
        when(vmDao.get(vmId)).thenReturn(vm);

        Guid snapshotId = Guid.NewGuid();
        snapshot = new Snapshot();
        snapshot.setId(snapshotId);
        snapshot.setVmId(vmId);
        when(snapshotDao.get(snapshotId)).thenReturn(snapshot);

        TryBackToAllSnapshotsOfVmParameters params = new TryBackToAllSnapshotsOfVmParameters(vmId, snapshotId);

        cmd = spy(new TryBackToAllSnapshotsOfVmCommand<TryBackToAllSnapshotsOfVmParameters>(params));
        doNothing().when(cmd).updateVmDisksFromDb();
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        doReturn(vmDao).when(cmd).getVmDAO();
    }

    @Test
    public void testCanDoActionVmNotDown() {
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }
}
