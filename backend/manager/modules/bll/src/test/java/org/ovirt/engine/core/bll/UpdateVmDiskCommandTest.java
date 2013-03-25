package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVmDiskCommandTest {

    private Guid diskImageGuid = Guid.NewGuid();
    private Guid vmId = Guid.NewGuid();

    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDAO;
    @Mock
    private SnapshotDao snapshotDao;
    @Mock
    private DiskImageDAO diskImageDao;
    @Mock
    private StoragePoolDAO storagePoolDao;
    @Mock
    private DbFacade dbFacade;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ShareableDiskEnabled, Version.v3_1.toString(), true)
            );

    /**
     * The command under test.
     */
    protected UpdateVmDiskCommand<UpdateVmDiskParameters> command;

    @Test
    public void getOtherVmDisks() {
        UpdateVmDiskParameters parameters = createParameters();

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.NewGuid());
        otherDisk.setActive(true);
        when(diskDao.getAllForVm(vmId)).thenReturn(new LinkedList<Disk>(Arrays.asList(parameters.getDiskInfo(),
                otherDisk)));
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        VM vm = mockVmStatusDown();

        List<Disk> otherDisks = command.getOtherVmDisks(vm.getId());
        assertEquals("Wrong number of other disks", 1, otherDisks.size());
        assertFalse("Wrong other disk", otherDisks.contains(parameters.getDiskInfo()));
    }

    @Test
    public void canDoActionFailedVMNotFound() throws Exception {
        initializeCommand();
        mockNullVm();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString()));
    }

    @Test
    public void canDoActionFailedVMHasNotDisk() throws Exception {
        initializeCommand();
        mockVmStatusDown();
        createNullDisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionFailedShareableDiskVolumeFormatUnsupported() throws Exception {
        UpdateVmDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createShareableDisk(VolumeFormat.COW));

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        mockVmStatusDown();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT.toString()));
    }

    @Test
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        UpdateVmDiskParameters parameters = createParameters();
        parameters.setDiskInfo(createShareableDisk(VolumeFormat.RAW));

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.NewGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        initializeCommand(parameters);

        mockVmStatusDown();

        assertTrue(command.canDoAction());
        assertTrue(oldDisk.getVmSnapshotId() == null);
    }

    @Test
    public void canDoActionMakeDiskBootableSuccess() {
        canDoActionMakeDiskBootable(false);
    }

    @Test
    public void canDoActionMakeDiskBootableFail() {
        canDoActionMakeDiskBootable(true);
    }

    private void canDoActionMakeDiskBootable(boolean boot) {
        UpdateVmDiskParameters parameters = createParameters();
        Disk newDisk = parameters.getDiskInfo();
        newDisk.setBoot(true);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.NewGuid());
        otherDisk.setActive(true);
        otherDisk.setBoot(boot);
        when(diskDao.getAllForVm(vmId)).thenReturn(new LinkedList<Disk>(Collections.singleton(otherDisk)));
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        mockVmStatusDown();

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.canDoAction());
    }

    @Test
    public void canDoActionMakeDiskBootableOnOtherVmSuccess() {
        canDoActionMakeDiskBootableOnOtherVm(false);
    }

    @Test
    public void canDoActionMakeDiskBootableOnOtherVmFail() {
        canDoActionMakeDiskBootableOnOtherVm(true);
    }

    private void canDoActionMakeDiskBootableOnOtherVm(boolean boot) {
        UpdateVmDiskParameters parameters = createParameters();
        Disk newDisk = parameters.getDiskInfo();
        newDisk.setBoot(true);

        Guid otherVmId = Guid.NewGuid();
        VM otherVm = new VM();
        otherVm.setId(otherVmId);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.NewGuid());
        otherDisk.setActive(true);
        otherDisk.setBoot(boot);
        when(diskDao.getAllForVm(otherVmId)).thenReturn(new LinkedList<Disk>(Collections.singleton(otherDisk)));
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);

        mockVmStatusDown(otherVm);

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.canDoAction());
    }

    private void initializeCommand() {
        initializeCommand(createParameters());
    }

    protected void initializeCommand(UpdateVmDiskParameters params) {
        command = spy(new UpdateVmDiskCommand<UpdateVmDiskParameters>(params) {
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            protected DiskDao getDiskDao() {
                return diskDao;
            }

        });
        doReturn(true).when(command).acquireLockInternal();
        doReturn(vmNetworkInterfaceDAO).when(command).getVmNetworkInterfaceDao();
        doReturn(snapshotDao).when(command).getSnapshotDao();
        doReturn(diskImageDao).when(command).getDiskImageDao();
        doReturn(storagePoolDao).when(command).getStoragePoolDAO();

        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));

        mockVds();
    }

    private void mockNullVm() {
        doReturn(vmDAO).when(command).getVmDAO();
        mockGetForDisk((VM) null);
        mockGetVmsListForDisk(null);
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(null);
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusDown(VM... otherPluggedVMs) {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setGuestOs("rhel6");
        vm.setId(vmId);
        doReturn(vmDAO).when(command).getVmDAO();
        List<VM> vms = new LinkedList<VM>(Arrays.asList(otherPluggedVMs));
        vms.add(vm);
        mockGetForDisk(vms);
        mockGetVmsListForDisk(vm);
        storage_pool storagePool = mockStoragePool(Version.v3_1);
        vm.setStoragePoolId(storagePool.getId());
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
        return vm;
    }

    private void mockGetForDisk(VM vm) {
        mockGetForDisk(Collections.singletonList(vm));
    }

    private void mockGetForDisk(List<VM> vms) {
        Map<Boolean, List<VM>> vmsMap = new HashMap<Boolean, List<VM>>();
        vmsMap.put(Boolean.TRUE, vms);
        when(vmDAO.getForDisk(diskImageGuid)).thenReturn(vmsMap);
    }

    private void mockGetVmsListForDisk(VM vm) {
        List<VM> vms = new ArrayList<VM>();
        vms.add(vm);
        when(vmDAO.getVmsListForDisk(diskImageGuid)).thenReturn(vms);
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        vds.setVdsGroupCompatibilityVersion(new Version("3.1"));
        command.setVdsId(Guid.Empty);
        doReturn(vdsDao).when(command).getVdsDAO();
        when(vdsDao.get(Guid.Empty)).thenReturn(vds);
    }

    /**
     * Mock a {@link storage_pool}.
     *
     * @param compatibilityVersion
     * @return
     */
    private storage_pool mockStoragePool(Version compatibilityVersion) {
        Guid storagePoolId = Guid.NewGuid();
        storage_pool storagePool = new storage_pool();
        storagePool.setId(storagePoolId);
        storagePool.setcompatibility_version(compatibilityVersion);
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * @return Valid parameters for the command.
     */
    protected UpdateVmDiskParameters createParameters() {
        DiskImage diskInfo = new DiskImage();
        diskInfo.setId(diskImageGuid);
        return new UpdateVmDiskParameters(vmId, diskImageGuid, diskInfo);
    }

    /**
     * The following method will simulate a situation when disk was not found in DB
     */
    private void createNullDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(null);
    }

    /**
     * The following method will create a new DiskImage
     */
    private DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(diskImageGuid);
        return disk;
    }

    /**
     * The following method will create a Shareable DiskImage with a specified format
     */
    private DiskImage createShareableDisk(VolumeFormat volumeFormat) {
        DiskImage disk = createDiskImage();
        disk.setvolumeFormat(volumeFormat);
        disk.setShareable(true);
        return disk;
    }
}
