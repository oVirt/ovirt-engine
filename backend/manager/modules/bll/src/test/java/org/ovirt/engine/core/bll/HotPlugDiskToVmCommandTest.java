package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class HotPlugDiskToVmCommandTest extends BaseCommandTest {

    protected Guid diskImageGuid = Guid.newGuid();
    protected Guid vmId = Guid.newGuid();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid storageDomainId = Guid.newGuid();
    protected static final List<String> DISK_HOTPLUGGABLE_INTERFACES = Arrays.asList("VirtIO_SCSI", "VirtIO");

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.HotPlugEnabled, Version.v3_1.getValue(), true),
            mockConfig(ConfigValues.HotPlugDiskSnapshotSupported, Version.v3_1.getValue(), true));

    @Mock
    private VmDao vmDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    protected DiskDao diskDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    private DiskValidator diskValidator;
    @Mock
    protected OsRepository osRepository;

    /**
     * The command under test.
     */
    protected HotPlugDiskToVmCommand<HotPlugDiskToVmParameters> command;

    @Test
    public void canDoActionFailedVMNotFound() throws Exception {
        mockNullVm();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString()));
    }

    @Test
    public void getFCStorageTypeForLun() throws Exception {
        LUNs lun = new LUNs();
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        lun.setLunConnections(connections);
        StorageType storageType = command.getLUNStorageType(lun);
        assertEquals("Lun disk should be of FC storage type since it does not had connections",
                StorageType.FCP,
                storageType);
    }

    @Test
    public void getISCSIStorageTypeForLun() throws Exception {
        LUNs lun = new LUNs();
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        connections.add(new StorageServerConnections("Some LUN connection",
                "id",
                "iqn",
                "password",
                StorageType.ISCSI,
                "Username",
                "port",
                "portal"));
        lun.setLunConnections(connections);
        StorageType storageType = command.getLUNStorageType(lun);
        assertEquals("Lun disk should be of ISCSI storage type since it has one connection with ISCSI storage type",
                StorageType.ISCSI,
                storageType);
    }

    @Test
    public void canDoActionFailedVMHasNotDisk() throws Exception {
        mockVmStatusUp();
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionFailedVirtIODisk() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        when(osRepository.getOsName(0)).thenReturn("RHEL6");
        createNotVirtIODisk();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.HOT_PLUG_IDE_DISK_IS_NOT_SUPPORTED.toString()));
    }

    @Test
    public void canDoActionChecksIfHotPlugDiskSnapshotIsSupported() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        cretaeVirtIODisk();
        initStorageDomain();
        command.getParameters().setSnapshotId(Guid.newGuid());
        command.canDoAction();
        verify(command, times(1)).isHotPlugDiskSnapshotSupported();
    }

    @Test
    public void canDoActionFailedWrongPlugStatus() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        cretaeDiskWrongPlug(true);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.HOT_PLUG_DISK_IS_NOT_UNPLUGGED.toString()));
    }

    @Test
    public void canDoActionFailedGuestOsIsNotSupported() {
        mockInterfaceList();
        VM vm = mockVmStatusUp();
        vm.setVmOs(15); // rhel3x64
        cretaeVirtIODisk();
        when(osRepository.getOsName(15)).thenReturn("RHEL3x64");
        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(Collections.<String>emptySet());
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED.toString()));
    }

    @Test
    public void canDoActionSuccess() {
        mockVmStatusUp();
        mockInterfaceList();
        cretaeVirtIODisk();
        initStorageDomain();
        assertTrue(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().isEmpty());
    }

    @Test
    public void canDoActionSuccessFailedDiskInterfaceUnsupported() {
        mockVmStatusUp();
        cretaeVirtIODisk();
        initStorageDomain();
        when(diskValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(any(Disk.class))).thenReturn(diskValidator);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED.toString()));
    }

    @Before
    public void initializeCommand() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        command = spy(createCommand());
        mockVds();
        mockVmDevice(false);
        when(command.getActionType()).thenReturn(getCommandActionType());
        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any(StorageDomain.class));
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainExistAndActive();
        doReturn(vmNetworkInterfaceDao).when(command).getVmNetworkInterfaceDao();
    }

    private void initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        storageDomain.setStoragePoolId(storagePoolId);

        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        when(storageDomainDao.get(any(Guid.class))).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);
    }

    protected HotPlugDiskToVmCommand<HotPlugDiskToVmParameters> createCommand() {
        return new HotPlugDiskToVmCommand<>(createParameters());
    }

    protected VdcActionType getCommandActionType() {
        return VdcActionType.HotPlugDiskToVm;
    }

    private void mockNullVm() {
        doReturn(vmDao).when(command).getVmDao();
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(null);
        cretaeVirtIODisk();
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusUp() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Up);
        vm.setVmOs(8);
        vm.setId(vmId);
        vm.setVdsGroupCompatibilityVersion(Version.v3_1);
        vm.setRunOnVds(Guid.newGuid());
        doReturn(vmDao).when(command).getVmDao();
        mockVMDao(vm);
        return vm;
    }

    private void mockVMDao(VM vm) {
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        List<VM> vmList = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setId(command.getParameters().getVmId());
        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());
        vmList.add(vm1);
        vmList.add(vm2);
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(vmList);
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        vds.setVdsGroupCompatibilityVersion(new Version("3.1"));
        doReturn(vdsDao).when(command).getVdsDao();
        when(vdsDao.get(Mockito.any(Guid.class))).thenReturn(vds);
    }

    protected void mockInterfaceList() {
        ArrayList<String> diskInterfaces = new ArrayList<>(
                Arrays.asList(new String[]{
                        "IDE",
                        "VirtIO",
                        "VirtIO_SCSI"
                }));

        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn(diskInterfaces);
    }

    /**
     * @return Valid parameters for the command.
     */
    protected HotPlugDiskToVmParameters createParameters() {
        return new HotPlugDiskToVmParameters(vmId, diskImageGuid);
    }

    /**
     * The following method will create a disk which is not VirtIO
     * @return
     */
    private DiskImage createNotVirtIODisk() {
        DiskImage disk = getDiskImage();
        disk.setActive(true);
        disk.setDiskInterface(DiskInterface.IDE);
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(new HashSet<>(DISK_HOTPLUGGABLE_INTERFACES));
        return disk;
    }

    /**
     * The following method will create a VirtIO disk , which is marked as unplugged
     * @return
     */
    protected void cretaeVirtIODisk() {
        DiskImage disk = getDiskImage();
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setActive(true);
        doReturn(diskDao).when(command).getDiskDao();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(new HashSet<>(DISK_HOTPLUGGABLE_INTERFACES));
        mockVmDevice(false);
    }

    protected DiskImage getDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setImageId(diskImageGuid);
        ArrayList<Guid> storageIdList = new ArrayList<>();
        storageIdList.add(storageDomainId);
        disk.setStorageIds(storageIdList);
        disk.setStoragePoolId(storagePoolId);
        return disk;
    }

    /**
      * The following method will create a VirtIO disk with provided plug option
      * @param plugged - the value which will be set to plug field
      * @return
      */
    protected void cretaeDiskWrongPlug(boolean plugged) {
        cretaeVirtIODisk();
        mockVmDevice(plugged);
    }

    protected void mockVmDevice(boolean plugged) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId());
        vmDevice.setIsPlugged(plugged);
        doReturn(vmDeviceDao).when(command).getVmDeviceDao();
        when(vmDeviceDao.get(Mockito.any(VmDeviceId.class))).thenReturn(vmDevice);
    }

}
