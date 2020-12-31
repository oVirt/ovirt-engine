package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiskValidatorTest {
    @Mock
    private VmDao vmDao;

    @Mock
    @InjectedMock
    public StorageDomainDao storageDomainDao;

    @Mock
    private DiskImageDao diskImageDao;

    private DiskValidator validator;
    private DiskImage disk;
    private DiskValidator lunValidator;
    private DiskImagesValidator diskImagesValidator;

    private static DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        Image image = new Image();
        image.setVolumeType(VolumeType.Sparse);
        disk.setImage(image);
        return disk;
    }

    private static LunDisk createLunDisk() {
        LunDisk disk = new LunDisk();
        LUNs lun = new LUNs();
        lun.setLUNId("lun_id");
        lun.setLunType(StorageType.ISCSI);
        disk.setLun(lun);
        return disk;
    }

    private static VM createVM() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        vm.setVmOs(1);
        return vm;
    }

    @BeforeEach
    public void setUp() {
        disk = createDiskImage();
        disk.setDiskAlias("disk1");
        validator = spy(new DiskValidator(disk));
        diskImagesValidator = spy(new DiskImagesValidator(disk));
        doReturn(vmDao).when(validator).getVmDao();
        doReturn(diskImageDao).when(validator).getDiskImageDao();
        doReturn(diskImageDao).when(diskImagesValidator).getDiskImageDao();

    }

    private void setupForLun() {
        LunDisk lunDisk = createLunDisk();
        lunValidator = spy(new DiskValidator(lunDisk));
    }

    private StorageDomain createStorageDomainForDisk(StorageType storageType) {
        StorageDomain domain = new StorageDomain();
        domain.setId(Guid.newGuid());
        domain.setStorageType(storageType);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(domain.getId())));

        when(storageDomainDao.get(domain.getId())).thenReturn(domain);

        return domain;
    }

    private VmDevice createVmDeviceForDisk(VM vm, Disk disk) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(vm.getId(), disk.getId()));
        device.setSnapshotId(null);
        device.setPlugged(true);
        return device;
    }

    public List<Pair<VM, VmDevice>> prepareForCheckingIfDiskPluggedToVmsThatAreNotDown() {
        VM vm1 = createVM();
        VM vm2 = createVM();
        VmDevice device1 = createVmDeviceForDisk(vm1, disk);
        VmDevice device2 = createVmDeviceForDisk(vm1, disk);
        List<Pair<VM, VmDevice>> vmsInfo = new LinkedList<>();
        vmsInfo.add(new Pair<>(vm1, device1));
        vmsInfo.add(new Pair<>(vm2, device2));
        return vmsInfo;
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsUnFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.UNFILTERED);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), createDiskVmElementUsingScsiReserevation(), lunDisk1),
                isValid());
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.FILTERED);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), createDiskVmElementUsingScsiReserevation(), lunDisk1),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED));
    }

    @Test
    public void testDiskAttachedToVMValid() {
        VM vm = createVM();
        when(vmDao.getVmsListForDisk(any(), anyBoolean())).thenReturn(Collections.singletonList(vm));
        assertThat(validator.isDiskAttachedToVm(vm), isValid());
    }

    @Test
    public void testDiskAttachedToVMFail() {
        VM vm = createVM();
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM));
    }

    @Test
    public void testDiskAttachedToAnyNonDownVM() {
        assertThat(validator.isDiskPluggedToAnyNonDownVm(false), isValid());
    }

    @Test
    public void testDiskAttachedToAnyNonDownVMWithProblems() {
        testDiskAttachedToAnyNonDownVMWithProblems
                (false, "anotherPausedPlugged,runningSnapshotPlugged,vmPausedPlugged");
    }

    @Test
    public void testDiskAttachedToAnyNonDownVMWithProblemsOnlyPlugged() {
        testDiskAttachedToAnyNonDownVMWithProblems(true, "runningSnapshotPlugged");
    }

    private void testDiskAttachedToAnyNonDownVMWithProblems(boolean checkOnlyPlugged, String expectedNames) {
        VM vmPausedPlugged = createVM();
        vmPausedPlugged.setName("vmPausedPlugged");
        vmPausedPlugged.setStatus(VMStatus.Paused);
        VmDevice device1 = new VmDevice();
        device1.setPlugged(true);
        Pair<VM, VmDevice> pair1 = new Pair<>(vmPausedPlugged, device1);

        VM vmDownPlugged = createVM();
        vmDownPlugged.setName("vmDownPlugged");
        VmDevice device2 = new VmDevice();
        device2.setPlugged(true);
        Pair<VM, VmDevice> pair2 = new Pair<>(vmDownPlugged, device2);

        VM vmRunningUnplugged = createVM();
        vmRunningUnplugged.setName("vmRunningUnplugged");
        vmRunningUnplugged.setStatus(VMStatus.Up);
        VmDevice device3 = new VmDevice();
        device3.setPlugged(false);
        Pair<VM, VmDevice> pair3 = new Pair<>(vmRunningUnplugged, device3);

        VM anotherPausedPlugged = createVM();
        anotherPausedPlugged.setName("anotherPausedPlugged");
        anotherPausedPlugged.setStatus(VMStatus.Paused);
        VmDevice device4 = new VmDevice();
        device4.setPlugged(true);
        Pair<VM, VmDevice> pair4 = new Pair<>(anotherPausedPlugged, device4);

        VM runningSnapshotPlugged = createVM();
        runningSnapshotPlugged.setName("runningSnapshotPlugged");
        runningSnapshotPlugged.setStatus(VMStatus.Up);
        VmDevice device5 = new VmDevice();
        device5.setPlugged(true);
        device5.setSnapshotId(Guid.newGuid());
        Pair<VM, VmDevice> pair5 = new Pair<>(runningSnapshotPlugged, device5);

        List<Pair<VM, VmDevice>> vmList = Arrays.asList(pair1, pair2, pair3, pair4, pair5);

        when(vmDao.getVmsWithPlugInfo(any())).thenReturn(vmList);
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_LIST, expectedNames)};

        assertThat(validator.isDiskPluggedToAnyNonDownVm(checkOnlyPlugged),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_PLUGGED_TO_NON_DOWN_VMS, expectedReplacements));
    }

    @Test
    public void testDiskAttachedToVMFailWithCorrectReplacements() {
        VM vm = createVM();
        vm.setName("MyVm");
        disk.setDiskAlias("MyDisk");
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_NAME_VARIABLE, vm.getName())};
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM, expectedReplacements));
    }

    @Test
    public void sparsifyNotSupportedForDirectLun() {
        setupForLun();
        assertThat(lunValidator.isSparsifySupported(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_DISK_STORAGE_TYPE));
    }

    @Test
    public void sparsifySupportedByFileDomain() {
        createStorageDomainForDisk(StorageType.NFS);
        assertThat(validator.isSparsifySupported(), isValid());
    }

    @Test
    public void sparsifyNotSupportedByOpenstackDomain() {
        createStorageDomainForDisk(StorageType.CINDER);
        assertThat(validator.isSparsifySupported(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_STORAGE_TYPE));
    }

    @Test
    public void sparsifySupportedWhenWipeAfterDeleteIsOff() {
        createStorageDomainForDisk(StorageType.ISCSI);
        assertThat(validator.isSparsifySupported(), isValid());
    }

    @Test
    public void sparsifyNotSupportedWhenWipeAfterDeleteIsOn() {
        createStorageDomainForDisk(StorageType.ISCSI);
        disk.setWipeAfterDelete(true);
        assertThat(validator.isSparsifySupported(), failsWith(EngineMessage
                .ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED));
    }

    @Test
    public void sparsifyNotSupportedWhenDiskIsPreallocated() {
        createStorageDomainForDisk(StorageType.NFS);
        disk.getImage().setVolumeType(VolumeType.Preallocated);
        assertThat(validator.isSparsifySupported(), failsWith(EngineMessage
                .ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_FOR_PREALLOCATED));
    }

    @Test
    public void sparsifyNotSupportedWhenDiskIsCow() {
        createStorageDomainForDisk(StorageType.NFS);
        disk.getImage().setVolumeFormat(VolumeFormat.COW);
        assertThat(validator.isSparsifySupported(), failsWith(EngineMessage
                .ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_FOR_COW));
    }

    private LunDisk createLunDisk(ScsiGenericIO sgio) {
        LunDisk lunDisk = createLunDisk();
        lunDisk.setSgio(sgio);

        return lunDisk;
    }

    private static DiskVmElement createDiskVmElementUsingScsiReserevation() {
        DiskVmElement dve = new DiskVmElement();
        dve.setUsingScsiReservation(true);
        return dve;
    }


}
