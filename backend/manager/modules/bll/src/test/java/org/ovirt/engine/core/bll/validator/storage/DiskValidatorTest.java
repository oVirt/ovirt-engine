package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertThat;
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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.Strict;
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
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.di.InjectorRule;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(Strict.class)
public class DiskValidatorTest {

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Mock
    private VmDao vmDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    private DiskValidator validator;
    private DiskImage disk;
    private DiskValidator lunValidator;

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

    @Before
    public void setUp() {
        disk = createDiskImage();
        disk.setDiskAlias("disk1");
        validator = spy(new DiskValidator(disk));
        doReturn(vmDao).when(validator).getVmDao();
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

        injectorRule.bind(StorageDomainDao.class, storageDomainDao);
        when(storageDomainDao.get(domain.getId())).thenReturn(domain);

        return domain;
    }

    private VmDevice createVmDeviceForDisk(VM vm, Disk disk, Guid snapshotId, boolean isPlugged) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(vm.getId(), disk.getId()));
        device.setSnapshotId(snapshotId);
        device.setPlugged(isPlugged);
        return device;
    }

    public List<Pair<VM, VmDevice>> prepareForCheckingIfDiskPluggedToVmsThatAreNotDown() {
        VM vm1 = createVM();
        VM vm2 = createVM();
        VmDevice device1 = createVmDeviceForDisk(vm1, disk, null, true);
        VmDevice device2 = createVmDeviceForDisk(vm1, disk, null, true);
        List<Pair<VM, VmDevice>> vmsInfo = new LinkedList<>();
        vmsInfo.add(new Pair<>(vm1, device1));
        vmsInfo.add(new Pair<>(vm2, device2));
        return vmsInfo;
    }

    @Test
    public void diskPluggedToVmsThatAreNotDownValid() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(false, vmsInfo), isValid());
    }

    @Test
    public void diskPluggedToVmsThatAreNotDownFail() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(0).getFirst().setStatus(VMStatus.Up);
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(false, vmsInfo),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
    }

    @Test
    public void diskPluggedToVmsNotAsSnapshotSuccess() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(0).getFirst().setStatus(VMStatus.Up);
        vmsInfo.get(1).getFirst().setStatus(VMStatus.Up);
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(true, vmsInfo),
                isValid());
    }

    @Test
    public void diskPluggedToVmsCheckSnapshotsFail() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(1).getFirst().setStatus(VMStatus.Up);
        vmsInfo.get(1).getSecond().setSnapshotId(Guid.newGuid());
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(true, vmsInfo),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
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
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(Collections.singletonList(vm));
        assertThat(validator.isDiskAttachedToVm(vm), isValid());
    }

    @Test
    public void testDiskAttachedToVMFail() {
        VM vm = createVM();
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM));
    }

    @Test
    public void testDiskAttachedToAnyVM() {
        assertThat(validator.isDiskAttachedToAnyVm(), isValid());
    }

    @Test
    public void testDiskAttachedToAnyVMFails() {
        VM vm1 = createVM();
        VM vm2 = createVM();
        vm1.setName("Vm1");
        vm2.setName("Vm2");
        List<VM> vmList = Arrays.asList(vm1, vm2);

        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(vmList);
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_LIST, "Vm1,Vm2")};

        assertThat(validator.isDiskAttachedToAnyVm(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_ATTACHED_TO_VMS, expectedReplacements));
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
        StorageDomain storageDomain = createStorageDomainForDisk(StorageType.ISCSI);
        disk.setWipeAfterDelete(true);
        storageDomain.setSupportsDiscardZeroesData(false);
        assertThat(validator.isSparsifySupported(), failsWith(EngineMessage
                .ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED));
    }

    @Test
    public void sparsifySupportedWhenWipeAfterDeleteIsOn() {
        StorageDomain storageDomain = createStorageDomainForDisk(StorageType.FCP);
        disk.setWipeAfterDelete(true);
        storageDomain.setSupportsDiscardZeroesData(true);
        assertThat(validator.isSparsifySupported(), isValid());
    }

    @Test
    public void sparsifyNotSupportedWhenDiskIsPreallocated() {
        createStorageDomainForDisk(StorageType.NFS);
        disk.getImage().setVolumeType(VolumeType.Preallocated);
        assertThat(validator.isSparsifySupported(), failsWith(EngineMessage
                .ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_FOR_PREALLOCATED));
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
