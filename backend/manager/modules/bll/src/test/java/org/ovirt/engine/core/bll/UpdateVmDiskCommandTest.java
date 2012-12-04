package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
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
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;
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
        oldDisk.setvm_snapshot_id(Guid.NewGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        initializeCommand(parameters);

        mockVmStatusDown();

        assertTrue(command.canDoAction());
        assertTrue(oldDisk.getvm_snapshot_id() == null);
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
        mockVds();
    }

    private void mockNullVm() {
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        mockGetForDisk(null);
        mockGetVmsListForDisk(null);
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(null);
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusDown() {
        VM vm = new VM();
        vm.setstatus(VMStatus.Down);
        vm.setguest_os("rhel6");
        vm.setId(vmId);
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        mockGetForDisk(vm);
        mockGetVmsListForDisk(vm);
        storage_pool storagePool = mockStoragePool(Version.v3_1);
        vm.setstorage_pool_id(storagePool.getId());
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
        return vm;
    }

    private void mockGetForDisk(VM vm) {
        List<VM> vms = new ArrayList<VM>();
        vms.add(vm);
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
        vds.setvds_group_compatibility_version(new Version("3.1"));
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
        disk.setvolume_format(volumeFormat);
        disk.setShareable(true);
        return disk;
    }

    /**
     * The following method will create a VirtIO disk , which is marked as unplugged
     * @return
     */
    protected DiskImage cretaeVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageGuid);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setPlugged(false);
        disk.setactive(true);
        disk.setId(diskImageGuid);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        return disk;
    }
}
