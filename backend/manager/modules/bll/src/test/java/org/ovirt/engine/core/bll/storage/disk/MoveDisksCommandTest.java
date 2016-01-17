package org.ovirt.engine.core.bll.storage.disk;

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
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;

public class MoveDisksCommandTest extends BaseCommandTest {

    private final Guid diskImageId = Guid.newGuid();
    private final Guid templateDiskImageId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final Guid dstStorageId = Guid.newGuid();

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private DiskDao diskDao;

    @Mock
    private VmDao vmDao;

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
        command = spy(new MoveDisksCommand<>(new MoveDisksParameters(new ArrayList<>()), null));
    }

    private List<MoveDiskParameters> createMoveDisksParameters() {
        return Arrays.asList(new MoveDiskParameters(diskImageId, srcStorageId, dstStorageId));
    }

    @Test
    public void validateNoDisksSpecified() {
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED.toString()));
    }

    @Test
    public void validateInvalidVmStatus() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Unknown, Guid.newGuid(), diskImageId);

        command.updateParameters();
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP.toString()));
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
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_MOVE_DISKS_MIXED_PLUGGED_STATUS.toString()));
    }

    @Test
    public void validateFailureOnMovingLunDisk() {
        MoveDiskParameters moveDiskParameters1 = new MoveDiskParameters(Guid.newGuid(), srcStorageId, dstStorageId);
        command.getParameters().setParametersList(Collections.singletonList(moveDiskParameters1));
        initLunDisk();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void validateFailureOnMovingCinderDisk() {
        MoveDiskParameters moveDiskParameters = new MoveDiskParameters(Guid.newGuid(), srcStorageId, dstStorageId);
        command.getParameters().setParametersList(Collections.singletonList(moveDiskParameters));
        initCinderDisk();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
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

    private void initLunDisk() {
        Disk lunDisk = new LunDisk();
        when(diskDao.get(any(Guid.class))).thenReturn(lunDisk);
    }

    private void initCinderDisk() {
        Disk cinderDisk = new CinderDisk();
        when(diskDao.get(any(Guid.class))).thenReturn(cinderDisk);
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

    /** Mock Daos */

    private void mockDaos() {
        mockVmDao();
        mockDiskImageDao();
        mockDiskDao();
    }

    private void mockVmDao() {
        doReturn(vmDao).when(command).getVmDao();
    }

    private void mockDiskImageDao() {
        doReturn(diskImageDao).when(command).getDiskImageDao();
    }

    private void mockDiskDao() {
        doReturn(diskDao).when(command).getDiskDao();
    }
}
