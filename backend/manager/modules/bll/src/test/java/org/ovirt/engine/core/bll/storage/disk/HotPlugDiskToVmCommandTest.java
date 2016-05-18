package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

public class HotPlugDiskToVmCommandTest extends BaseCommandTest {

    protected Guid diskImageGuid = Guid.newGuid();
    protected Guid vmId = Guid.newGuid();
    private VM vm = new VM();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid storageDomainId = Guid.newGuid();
    protected static final List<String> DISK_HOTPLUGGABLE_INTERFACES = Arrays.asList("VirtIO_SCSI", "VirtIO");

    private DiskImage disk = new DiskImage();
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

    @Spy
    private DiskValidator diskValidator = new DiskValidator(disk);

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    protected OsRepository osRepository;

    /**
     * The command under test.
     */
    protected HotPlugDiskToVmCommand<VmDiskOperationParameterBase> command;

    @Before
    public void setUp() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        command = spy(createCommand());
        mockVds();
        mockVmDevice(false);

        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));

        doReturn(storageDomainValidator).when(command).getStorageDomainValidator(any(StorageDomain.class));
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainExistAndActive();

        doReturn(vmNetworkInterfaceDao).when(command).getVmNetworkInterfaceDao();
        doReturn(vmDao).when(command).getVmDao();
        doReturn(diskDao).when(command).getDiskDao();
        doReturn(diskVmElementDao).when(command).getDiskVmElementDao();

        doReturn(new ArrayList<>()).when(diskVmElementDao).getAllForVm(vmId);
        doReturn(diskValidator).when(command).getDiskValidator(disk);
        doReturn(ValidationResult.VALID).when(diskValidator).isDiskExists();
        doReturn(ValidationResult.VALID).when(diskValidator).isDiskAttachedToVm(vm);

        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(new HashSet<>(DISK_HOTPLUGGABLE_INTERFACES));
    }

    @Test
    public void validateFailedVMNotFound() throws Exception {
        mockNullVm();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void validateFailedVMHasNotDisk() throws Exception {
        mockVmStatusUp();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateFailedVirtIODisk() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        when(osRepository.getOsName(0)).thenReturn("RHEL6");
        createNotVirtIODisk();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOT_PLUG_IDE_DISK_IS_NOT_SUPPORTED);
    }

    @Test
    public void validateFailedWrongPlugStatus() throws Exception {
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
        when(osRepository.getDiskHotpluggableInterfaces(any(Integer.class),
                any(Version.class))).thenReturn(Collections.<String>emptySet());
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
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
        when(diskValidator.isDiskInterfaceSupported(any(VM.class), any(DiskVmElement.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(any(Disk.class))).thenReturn(diskValidator);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    private void initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        storageDomain.setStoragePoolId(storagePoolId);

        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        when(storageDomainDao.get(any(Guid.class))).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);
    }

    protected HotPlugDiskToVmCommand<VmDiskOperationParameterBase> createCommand() {
        return new HotPlugDiskToVmCommand<>(createParameters(), null);
    }

    private void mockNullVm() {
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(null);
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
    protected VmDiskOperationParameterBase createParameters() {
        return new VmDiskOperationParameterBase(new DiskVmElement(diskImageGuid, vmId));
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
        vmDevice.setIsPlugged(plugged);
    }

    protected void mockVmDevice(boolean plugged) {
        vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId());
        vmDevice.setIsPlugged(plugged);
        doReturn(vmDeviceDao).when(command).getVmDeviceDao();
        when(vmDeviceDao.get(Mockito.any(VmDeviceId.class))).thenReturn(vmDevice);
    }

}
