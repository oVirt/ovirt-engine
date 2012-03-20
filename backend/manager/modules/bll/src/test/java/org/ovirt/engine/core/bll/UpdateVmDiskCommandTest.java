package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VmHandler.class, Config.class})
public class UpdateVmDiskCommandTest {

    private Guid diskImageGuid = Guid.NewGuid();
    private Guid vmId = Guid.NewGuid();

    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private DiskImageDAO diskImageDao;
    @Mock
    private VmDeviceDAO vmDeviceDAO;

    /**
     * The command under test.
     */
    protected UpdateVmDiskCommand<UpdateVmDiskParameters> command;

    @Before
    public void setUp() {
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.HotPlugSupportedOsList)).
                thenReturn("Windows2008,Windows2008x64,Windows2008R2x64,RHEL5,RHEL5x64,RHEL6,RHEL6x64");
        when(Config.GetValue(ConfigValues.HotPlugEnabled, "3.1")).thenReturn(true);
        mockStatic(VmHandler.class);
        MockitoAnnotations.initMocks(this);
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
        mockVmStatusUp();
        createNullDisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionFailedVirtIODisk() throws Exception {
        initializeCommand();
        mockVmWithOutVirtIODisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_VIRTIO.toString()));
    }

    @Test
    public void canDoActionFailedPluggWrongPlugStatus() throws Exception {
        initializeCommand(true, true);
        mockVmWithVirtIODisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_UNPLUGGED.toString()));
    }

    @Test
    public void canDoActionFailedUnPluggWrongPlugStatus() throws Exception {
        initializeCommand(false, false);
        mockVmWithVirtIODisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.HOT_UNPLUG_DISK_IS_NOT_PLUGGED.toString()));
    }

    @Test
    public void canDoActionFailedGuestOsIsNotSupported() {
        initializeCommand();
        mockVmWithVirtIODisk().setguest_os(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED.toString()));
    }

    @Test
    public void canDoActionSuccess() {
        initializeCommand();
        mockVmWithVirtIODisk();
        assertTrue(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().isEmpty());
    }

    protected void initializeCommand() {
        initializeCommand(true, false);
    }

    protected void initializeCommand(boolean paramsPlugged, boolean diskPlugged) {
        command = spy(new UpdateVmDiskCommand<UpdateVmDiskParameters>(createParameters(paramsPlugged)));
        mockVds();
        mockVmDevice(diskPlugged);
    }

    /**
     * Mock a VM that has a VirtIO disk.
     */
    private VM mockVmWithVirtIODisk() {
        VM vm = mockVmStatusUp();
        vm.addDriveToImageMap(RandomUtils.instance().nextNumericString(1), cretaeVirtIODisk());
        return vm;
    }

    /**
     * Mock a VM that has not a VirtIO disk.
     */
    private void mockVmWithOutVirtIODisk() {
        mockVmStatusUp().addDriveToImageMap(RandomUtils.instance().nextNumericString(1), createNotVirtIODisk());
    }

    private void mockNullVm() {
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        when(vmDAO.getById(command.getParameters().getVmId())).thenReturn(null);
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusUp() {
        VM vm = new VM();
        vm.setstatus(VMStatus.Up);
        vm.setguest_os("rhel6");
        AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
        when(vmDAO.getById(command.getParameters().getVmId())).thenReturn(vm);

        return vm;
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

    private void mockVmDevice(boolean plugged) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId());
        vmDevice.setIsPlugged(plugged);
        doReturn(vmDeviceDAO).when(command).getVmDeviceDao();
        when(vmDeviceDAO.get(Mockito.any(VmDeviceId.class))).thenReturn(vmDevice);
    }

    /**
     * @return Valid parameters for the command.
     */
    protected UpdateVmDiskParameters createParameters(boolean plugged) {
        DiskImage diskInfo = new DiskImage();
        diskInfo.setPlugged(plugged);
        return new UpdateVmDiskParameters(vmId, diskImageGuid, diskInfo);
    }

    /**
     * The following method will simulate a situation when disk was not found in DB
     */
    private void createNullDisk() {
        doReturn(diskImageDao).when(command).getDiskImageDao();
        when(diskImageDao.get(diskImageGuid)).thenReturn(null);
    }

    /**
     * The following method will create a disk which is not VirtIO
     * @return
     */
    private DiskImage createNotVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setId(diskImageGuid);
        disk.setdisk_interface(DiskInterface.IDE);
        disk.setdisk_type(DiskType.Data);
        disk.setactive(true);
        disk.setvm_guid(vmId);
        disk.setimage_group_id(diskImageGuid);
        doReturn(diskImageDao).when(command).getDiskImageDao();
        when(diskImageDao.get(diskImageGuid)).thenReturn(disk);
        return disk;
    }

    /**
     * The following method will create a VirtIO disk , which is marked as unplugged
     * @return
     */
    protected DiskImage cretaeVirtIODisk() {
        DiskImage disk = new DiskImage();
        disk.setId(diskImageGuid);
        disk.setdisk_interface(DiskInterface.VirtIO);
        disk.setdisk_type(DiskType.Data);
        disk.setPlugged(false);
        disk.setactive(true);
        disk.setvm_guid(vmId);
        disk.setimage_group_id(diskImageGuid);
        doReturn(diskImageDao).when(command).getDiskImageDao();
        when(diskImageDao.get(diskImageGuid)).thenReturn(disk);
        return disk;
    }
}
