package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class MoveDiskCommandTest extends BaseCommandTest {
    @Mock
    private VmDao vmDao;

    /**
     * The command under test
     */
    @InjectMocks
    protected MoveDiskCommand<MoveDiskParameters> command = new MoveDiskCommand<>(null);

    @Test
    public void testVmDownAndDiskUnplugged() {
        VM vm = createMockVm(VMStatus.Down);
        DiskVmElement diskVmElement = createMockDiskVmElement(vm.getId(), false);
        doReturn(vm).when(vmDao).get(any());

        assertEquals(ActionType.MoveOrCopyDisk,
                command.getMoveActionType(Collections.singletonList(diskVmElement)));
    }

    @Test
    public void testVmUpAndDiskPlugged() {
        VM vm = createMockVm(VMStatus.Up);
        DiskVmElement diskVmElement = createMockDiskVmElement(vm.getId(), true);
        doReturn(vm).when(vmDao).get(any());

        assertEquals(ActionType.LiveMigrateDisk,
                command.getMoveActionType(Collections.singletonList(diskVmElement)));
    }

    @Test
    public void testFloatingDisk() {
        assertEquals(ActionType.MoveOrCopyDisk,
                command.getMoveActionType(Collections.emptyList()));
    }

    private DiskVmElement createMockDiskVmElement(Guid vmId, boolean plugged) {
        DiskVmElement diskVmElement = new DiskVmElement();
        diskVmElement.setId(new VmDeviceId(Guid.newGuid(), vmId));
        diskVmElement.setPlugged(plugged);

        return diskVmElement;
    }

    private VM createMockVm(VMStatus status) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(status);
        return vm;
    }
}
