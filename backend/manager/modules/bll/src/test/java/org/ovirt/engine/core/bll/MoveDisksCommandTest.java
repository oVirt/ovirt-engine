package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class MoveDisksCommandTest {

    private final Guid diskImageId = Guid.newGuid();
    private final Guid templateDiskImageId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final Guid dstStorageId = Guid.newGuid();

    @Mock
    private DiskImageDAO diskImageDao;

    @Mock
    private VmDAO vmDao;

    /**
     * The command under test
     */
    protected MoveDisksCommand<MoveDisksParameters> command;

    @Before
    public void setupCommand() {
        initSpyCommand();
        mockDaos();
    }

    private void initSpyCommand() {
        command = spy(new MoveDisksCommand<MoveDisksParameters>(
                new MoveDisksParameters(new ArrayList<MoveDiskParameters>())));
    }

    private List<MoveDiskParameters> createMoveDisksParameters() {
        return Arrays.asList(new MoveDiskParameters(diskImageId, srcStorageId, dstStorageId));
    }

    @Test
    public void canDoActionNoDisksSpecified() {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED.toString()));
    }

    @Test
    public void canDoActionInvalidVmStatus() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Unknown, Guid.newGuid(), diskImageId);

        command.updateParameters();
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP.toString()));
    }

    @Test
    public void moveDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Down, null, diskImageId);

        command.updateParameters();
        assertTrue(command.getMoveDiskParametersList().size() == 1);
    }

    @Test
    public void moveFloatingDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);

        command.updateParameters();
        assertTrue(command.getMoveDiskParametersList().size() == 1);
    }

    @Test
    public void liveMigrateDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageId);

        command.updateParameters();
        assertTrue(command.getLiveMigrateVmDisksParametersList().size() == 1);
    }

    @Test
    public void liveMigrateDiskBasedOnTemplate() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImageBasedOnTemplate(diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageId);

        command.updateParameters();
        assertTrue(command.getLiveMigrateVmDisksParametersList().size() == 1);
    }

    @Test
    public void moveUnpluggedDiskVmDown() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Down, Guid.newGuid(), diskImageId, false);

        command.updateParameters();
        assertEquals(command.getMoveDiskParametersList().size(), 1);
    }

    @Test
    public void moveUnpluggedDiskVmUp() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageId, false);

        command.updateParameters();
        assertEquals(command.getMoveDiskParametersList().size(), 1);
    }

    @Test
    public void moveDiskAndLiveMigrateDisk() {
        Guid diskImageId1 = Guid.newGuid();
        Guid diskImageId2 = Guid.newGuid();

        MoveDiskParameters moveDiskParameters1 = new MoveDiskParameters(diskImageId1, srcStorageId, dstStorageId);
        MoveDiskParameters moveDiskParameters2 = new MoveDiskParameters(diskImageId2, srcStorageId, dstStorageId);
        command.getParameters().setParametersList(Arrays.asList(moveDiskParameters1, moveDiskParameters2));

        initDiskImage(diskImageId1);
        initDiskImage(diskImageId2);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageId1);
        initVm(VMStatus.Down, Guid.newGuid(), diskImageId2);

        command.updateParameters();
        assertTrue(command.getMoveDiskParametersList().size() == 1);
        assertTrue(command.getLiveMigrateVmDisksParametersList().size() == 1);
    }

    @Test
    public void movePluggedDiskAndUnpluggedDiskVmUp() {
        Guid diskImageId1 = Guid.newGuid();
        Guid diskImageId2 = Guid.newGuid();

        MoveDiskParameters moveDiskParameters1 = new MoveDiskParameters(diskImageId1, srcStorageId, dstStorageId);
        MoveDiskParameters moveDiskParameters2 = new MoveDiskParameters(diskImageId2, srcStorageId, dstStorageId);
        command.getParameters().setParametersList(Arrays.asList(moveDiskParameters1, moveDiskParameters2));

        initDiskImage(diskImageId1);
        initDiskImage(diskImageId2);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageId1, true, diskImageId2, false);

        command.updateParameters();
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_MOVE_DISKS_MIXED_PLUGGED_STATUS.toString()));
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId) {
        initVm(vmStatus, runOnVds, diskImageId, true, null, false);
    }

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId, boolean isPlugged) {
        initVm(vmStatus, runOnVds, diskImageId, isPlugged, null, false);
    }

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId1, boolean isPlugged1,
            Guid diskImageId2, boolean isPlugged2) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);

        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        when(vmDao.getForDisk(diskImageId1, false)).thenReturn(
                Collections.singletonMap(isPlugged1, Collections.singletonList(vm)));

        if (diskImageId2 != null) {
            when(vmDao.getForDisk(diskImageId2, false)).thenReturn(
                    Collections.singletonMap(isPlugged2, Collections.singletonList(vm)));
        }
    }

    private void initDiskImage(Guid diskImageId) {
        DiskImage diskImage = mockDiskImage(diskImageId);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);
    }

    private void initDiskImageBasedOnTemplate(Guid diskImageId) {
        DiskImage diskImage = mockDiskImage(diskImageId);
        diskImage.setParentId(templateDiskImageId);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);
    }

    private DiskImage mockDiskImage(Guid diskImageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(diskImageId);
        diskImage.setImageId(diskImageId);

        return diskImage;
    }

    /** Mock DAOs */

    private void mockDaos() {
        mockVmDao();
        mockDiskImageDao();
    }

    private void mockVmDao() {
        doReturn(vmDao).when(command).getVmDAO();
    }

    private void mockDiskImageDao() {
        doReturn(diskImageDao).when(command).getDiskImageDao();
    }
}
