package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class MoveDisksCommandTest {

    private final Guid diskImageId = Guid.NewGuid();
    private final Guid templateDiskImageId = Guid.NewGuid();
    private final Guid srcStorageId = Guid.NewGuid();
    private final Guid dstStorageId = Guid.NewGuid();

    @Mock
    private DiskImageDAO diskImageDao;

    @Mock
    private VmDAO vmDao;

    @Mock
    protected SnapshotDao snapshotDao;

    /**
     * The command under test
     */
    protected MoveDisksCommand<MoveDisksParameters> command;

    @Before
    public void setupCommand() {
        initSpyCommand();
        mockDaos();
        mockSnapshotValidator();
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
    public void canDoActionImagesNotFound() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionInvalidVmStatus() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Unknown, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP.toString()));
    }

    @Test
    public void canDoActionVmInPreview() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Down, null, diskImageId);
        setVmInPreview(true);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW.toString()));
    }

    @Test
    public void moveDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Down, null, diskImageId);

        assertTrue(command.canDoAction());
        assertFalse(command.getMoveDisksParametersList().isEmpty());
    }

    @Test
    public void moveFloatingDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);

        assertTrue(command.canDoAction());
        assertFalse(command.getMoveDisksParametersList().isEmpty());
    }

    @Test
    public void liveMigrateDisk() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertTrue(command.canDoAction());
        assertFalse(command.getLiveMigrateDisksParametersList().isEmpty());
    }

    @Test
    public void liveMigrateDiskBasedOnTemplate() {
        command.getParameters().setParametersList(createMoveDisksParameters());

        initDiskImageBasedOnTemplate(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertTrue(command.canDoAction());
        assertFalse(command.getLiveMigrateDisksParametersList().isEmpty());
    }

    @Test
    public void moveDiskAndLiveMigrateDisk() {
        Guid diskImageId1 = Guid.NewGuid();
        Guid diskImageId2 = Guid.NewGuid();

        MoveDiskParameters moveDiskParameters1 = new MoveDiskParameters(diskImageId1, srcStorageId, dstStorageId);
        MoveDiskParameters moveDiskParameters2 = new MoveDiskParameters(diskImageId2, srcStorageId, dstStorageId);
        command.getParameters().setParametersList(Arrays.asList(moveDiskParameters1, moveDiskParameters2));

        initDiskImage(diskImageId1);
        initDiskImage(diskImageId2);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId1);
        initVm(VMStatus.Down, Guid.NewGuid(), diskImageId2);

        assertTrue(command.canDoAction());
        assertFalse(command.getMoveDisksParametersList().isEmpty());
        assertFalse(command.getLiveMigrateDisksParametersList().isEmpty());
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);

        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        when(vmDao.getVmsListForDisk(diskImageId)).thenReturn(Collections.singletonList(vm));
    }

    private void setVmInPreview(boolean isInPreview) {
        when(snapshotDao.exists(any(Guid.class), eq(SnapshotStatus.IN_PREVIEW))).thenReturn(isInPreview);
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

        return diskImage;
    }

    private void mockSnapshotValidator() {
        SnapshotsValidator validator = new SnapshotsValidator() {
            @Override
            protected SnapshotDao getSnapshotDao() {
                return snapshotDao;
            }

        };
        doReturn(validator).when(command).createSnapshotsValidator();
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
