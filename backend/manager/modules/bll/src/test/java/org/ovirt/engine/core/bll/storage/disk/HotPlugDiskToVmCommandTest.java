package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class HotPlugDiskToVmCommandTest extends BaseCommandTest {

    protected Guid diskImageGuid = Guid.newGuid();
    protected Guid vmId = Guid.newGuid();
    private VM vm = new VM();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid storageDomainId = Guid.newGuid();
    protected static final List<String> DISK_HOTPLUGGABLE_INTERFACES = Arrays.asList("VirtIO_SCSI", "VirtIO");

    private DiskImage disk = new DiskImage();
    private DiskVmElement diskVmElement = new DiskVmElement(diskImageGuid, vmId);
    protected VmDevice vmDevice;

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
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    protected OsRepository osRepository;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    protected HotPlugDiskToVmCommand<VmDiskOperationParameterBase> command = createCommand();

    @BeforeEach
    public void setUp() {
        mockVds();
        mockVmDevice(false);

        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any());
        doReturn(diskValidator).when(command).getDiskValidator(disk);
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(), any());

        when(osRepository.getDiskHotpluggableInterfaces(anyInt(), any()))
                .thenReturn(new HashSet<>(DISK_HOTPLUGGABLE_INTERFACES));

    }

    @Test
    public void validateFailedVMNotFound() {
        mockNullVm();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void validateFailedVMHasNotDisk() {
        mockVmStatusUp();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateFailedVirtIODisk() {
        mockVmStatusUp();
        mockInterfaceList();
        when(osRepository.getOsName(0)).thenReturn("RHEL6");
        createNotVirtIODisk();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOT_PLUG_IDE_DISK_IS_NOT_SUPPORTED);
    }

    @Test
    public void validateFailedWrongPlugStatus() {
        mockVmStatusUp();
        mockInterfaceList();
        createDiskWrongPlug(true);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOT_PLUG_DISK_IS_NOT_UNPLUGGED);
    }

    @Test
    public void validateFailedGuestOsIsNotSupported() {
        mockInterfaceList();
        VM vm = mockVmStatusUp();
        vm.setVmOs(15); // rhel3x64
        createVirtIODisk();
        when(osRepository.getOsName(15)).thenReturn("RHEL3x64");
        when(osRepository.getDiskHotpluggableInterfaces(anyInt(), any())).thenReturn(Collections.emptySet());
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void validateSuccess() {
        mockVmStatusUp();
        mockInterfaceList();
        createVirtIODisk();
        initStorageDomain();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailedDiskInterfaceUnsupported() {
        mockVmStatusUp();
        createVirtIODisk();
        initStorageDomain();
        when(diskVmElementValidator.isDiskInterfaceSupported(any())).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void validateFailedDiscardNotSupported() {
        mockVmStatusUp();
        mockInterfaceList();
        createVirtIODisk();
        initStorageDomain();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE))
                .when(diskVmElementValidator).isPassDiscardSupported(any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
    }

    @Test
    public void validateBackupStorageDomain() {
        mockVmStatusUp();
        mockInterfaceList();
        createVirtIODisk();
        initStorageDomain();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE))
                .when(storageDomainValidator).isNotBackupDomain();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE);
    }

    private void initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        storageDomain.setStoragePoolId(storagePoolId);

        when(storageDomainDao.get(any())).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);
    }

    protected HotPlugDiskToVmCommand<VmDiskOperationParameterBase> createCommand() {
        return new HotPlugDiskToVmCommand<>(createParameters(), null);
    }

    private void mockNullVm() {
        createVirtIODisk();
    }

    /**
     * Mock a VM in status Up
     */
    protected VM mockVmStatusUp() {
        vm.setStatus(VMStatus.Up);
        vm.setVmOs(8);
        vm.setId(vmId);
        vm.setRunOnVds(Guid.newGuid());
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        return vm;
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        vds.setClusterCompatibilityVersion(Version.getLast());
        when(vdsDao.get(any())).thenReturn(vds);
    }

    protected void mockInterfaceList() {
        ArrayList<String> diskInterfaces = new ArrayList<>(
                Arrays.asList(new String[]{
                        "IDE",
                        "VirtIO",
                        "VirtIO_SCSI"
                }));

        when(osRepository.getDiskInterfaces(anyInt(), any(), any())).thenReturn(diskInterfaces);
    }

    /**
     * @return Valid parameters for the command.
     */
    protected VmDiskOperationParameterBase createParameters() {
        return new VmDiskOperationParameterBase(diskVmElement);
    }

    /**
     * The following method will create a disk which is not VirtIO
     */
    private void createNotVirtIODisk() {
        mockDiskImage(DiskInterface.IDE);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.IDE);
    }

    /**
     * The following method will create a VirtIO disk , which is marked as unplugged
     */
    protected void createVirtIODisk() {
        mockDiskImage(DiskInterface.VirtIO);
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO);
    }

    private DiskImage mockDiskImage(DiskInterface iface) {
        disk.setImageId(diskImageGuid);
        ArrayList<Guid> storageIdList = new ArrayList<>();
        storageIdList.add(storageDomainId);
        disk.setStorageIds(storageIdList);
        disk.setStoragePoolId(storagePoolId);
        disk.setActive(true);
        disk.setId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        DiskVmElement dve = new DiskVmElement(disk.getId(), vmId);
        dve.setDiskInterface(iface);
        when(diskVmElementDao.get(new VmDeviceId(disk.getId(), vmId))).thenReturn(dve);
        return disk;
    }

    /**
      * The following method will create a VirtIO disk with provided plug option
      * @param plugged - the value which will be set to plug field
      */
    protected void createDiskWrongPlug(boolean plugged) {
        createVirtIODisk();
        vmDevice.setPlugged(plugged);
    }

    protected void mockVmDevice(boolean plugged) {
        vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId());
        vmDevice.setPlugged(plugged);
        when(vmDeviceDao.get(any())).thenReturn(vmDevice);
    }

}
