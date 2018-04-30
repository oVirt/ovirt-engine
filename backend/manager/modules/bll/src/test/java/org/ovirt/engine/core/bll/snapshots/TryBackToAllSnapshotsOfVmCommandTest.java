package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;

/** A test case for the {@link TryBackToAllSnapshotsOfVmCommand} class. */
@MockitoSettings(strictness = Strictness.LENIENT)
public class TryBackToAllSnapshotsOfVmCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private TryBackToAllSnapshotsOfVmCommand<TryBackToAllSnapshotsOfVmParameters> cmd =
            new TryBackToAllSnapshotsOfVmCommand<>(
                    new TryBackToAllSnapshotsOfVmParameters(Guid.newGuid(), Guid.newGuid()), null);

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmDao vmDao;

    private VM vm;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setId(cmd.getParameters().getVmId());
        when(vmDao.get(cmd.getParameters().getVmId())).thenReturn(vm);

        Snapshot snapshot = new Snapshot();
        snapshot.setId(cmd.getParameters().getDstSnapshotId());
        snapshot.setVmId(cmd.getParameters().getVmId());
        when(snapshotDao.get(cmd.getParameters().getDstSnapshotId())).thenReturn(snapshot);

        doNothing().when(cmd).updateVmDisksFromDb();
    }

    @Test
    public void testValidateVmNotDown() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void testValidateWithEmptySnapshotGuid() {
        cmd.getParameters().setDstSnapshotId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
    }
}
