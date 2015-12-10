package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;

/** A test case for the {@link TryBackToAllSnapshotsOfVmCommand} class. */
public class TryBackToAllSnapshotsOfVmCommandTest extends BaseCommandTest {

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    private TryBackToAllSnapshotsOfVmCommand<TryBackToAllSnapshotsOfVmParameters> cmd;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmDao vmDao;

    private VM vm;
    private Snapshot snapshot;
    Guid vmId;

    @Before
    public void setUp() {
        injectorRule.bind(MacPoolPerDc.class, mock(MacPoolPerDc.class));

        vmId = Guid.newGuid();
        vm = new VM();
        vm.setId(vmId);
        when(vmDao.get(vmId)).thenReturn(vm);

        Guid snapshotId = Guid.newGuid();
        snapshot = new Snapshot();
        snapshot.setId(snapshotId);
        snapshot.setVmId(vmId);
        when(snapshotDao.get(snapshotId)).thenReturn(snapshot);

        TryBackToAllSnapshotsOfVmParameters params = new TryBackToAllSnapshotsOfVmParameters(vmId, snapshotId);

        cmd = spy(new TryBackToAllSnapshotsOfVmCommand<>(params, null));
        doNothing().when(cmd).updateVmDisksFromDb();
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        doReturn(vmDao).when(cmd).getVmDao();
    }

    @Test
    public void testValidateVmNotDown() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void testValidateWithEmptySnapshotGuid() {
        TryBackToAllSnapshotsOfVmParameters params = new TryBackToAllSnapshotsOfVmParameters(vmId, Guid.Empty);
        cmd = spy(new TryBackToAllSnapshotsOfVmCommand<>(params, null));
        doNothing().when(cmd).updateVmDisksFromDb();
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
    }
}
