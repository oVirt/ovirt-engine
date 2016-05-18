package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class DiskValidatorTest {

    @Mock
    private OsRepository osRepository;
    @Mock
    private VmDao vmDao;
    private DiskValidator validator;
    private DiskImage disk;
    private LunDisk lunDisk;
    private DiskValidator lunValidator;

    private static DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
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

    private VDS createVds() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        return vds;
    }

    private void initializeOsRepository (int osId, DiskInterface diskInterface) {
        ArrayList<String> supportedDiskInterfaces = new ArrayList<>();
        supportedDiskInterfaces.add(diskInterface.name());
        when(osRepository.getDiskInterfaces(1, null)).thenReturn(supportedDiskInterfaces);
        when(osRepository.getDiskInterfaces(2, null)).thenReturn(new ArrayList<>());
        // init the injector with the osRepository instance
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    @Before
    public void setUp() {
        initializeOsRepository(1, DiskInterface.VirtIO);
        disk = createDiskImage();
        disk.setDiskAlias("disk1");
        validator = spy(new DiskValidator(disk));
        doReturn(vmDao).when(validator).getVmDao();
    }

    private void setupForLun() {
        lunDisk = createLunDisk();
        lunValidator = spy(new DiskValidator(lunDisk));
        doReturn(vmDao).when(lunValidator).getVmDao();
    }

    private VmDevice createVmDeviceForDisk(VM vm, Disk disk, Guid snapshotId, boolean isPlugged) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(vm.getId(), disk.getId()));
        device.setSnapshotId(snapshotId);
        device.setIsPlugged(isPlugged);
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
        when(vmDao.getVmsWithPlugInfo(disk.getId())).thenReturn(vmsInfo);
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
    public void diskInterfaceSupportedByOs() {
        VM vm = createVM();
        initializeOsRepository(vm.getOs(), DiskInterface.VirtIO);
        DiskVmElement dve = new DiskVmElement();
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm, dve), isValid());
    }

    @Test
    public void diskInterfaceNotSupportedByOs() {
        VM vm = createVM();
        vm.setVmOs(2);
        initializeOsRepository(vm.getOs(), DiskInterface.VirtIO);
        DiskVmElement dve = new DiskVmElement();
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm, dve), failsWith(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
    }

    @Test
    public void readOnlyIsNotSupportedByDiskInterface() {
        disk.setReadOnly(true);
        DiskVmElement dve = new DiskVmElement();
        dve.setDiskInterface(DiskInterface.IDE);

        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(dve),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                        and(replacements(hasItem(String.format("$interface %1$s", DiskInterface.IDE)))));

        setupForLun();
        lunDisk.setReadOnly(true);
        lunDisk.setSgio(ScsiGenericIO.FILTERED);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(lunValidator.isReadOnlyPropertyCompatibleWithInterface(dve),
                failsWith(EngineMessage.SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK));
    }

    @Test
    public void readOnlyIsSupportedByDiskInterface() {
        disk.setReadOnly(true);
        DiskVmElement dve = new DiskVmElement();
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());

        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());

        disk.setReadOnly(false);
        dve.setDiskInterface(DiskInterface.IDE);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());

        setupForLun();
        lunDisk.setReadOnly(true);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(lunValidator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());

        lunDisk.setReadOnly(false);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(lunValidator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());

        dve.setDiskInterface(DiskInterface.IDE);
        assertThat(lunValidator.isReadOnlyPropertyCompatibleWithInterface(dve), isValid());
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsUnFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.UNFILTERED, true);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), lunDisk1),
                isValid());
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.FILTERED, true);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), lunDisk1),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED));
    }

    @Test
    public void testIsUsingScsiReservationValidWhenAddingFloatingDisk() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.UNFILTERED, true);

        assertThat(lunValidator.isUsingScsiReservationValid(null, lunDisk1),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SCSI_RESERVATION_NOT_VALID_FOR_FLOATING_DISK));
    }

    @Test
    public void testDiskAttachedToVMValid() {
        VM vm = createVM();
        when(vmDao.getVmsListForDisk(Matchers.any(Guid.class), anyBoolean())).thenReturn(Collections.singletonList(vm));
        assertThat(validator.isDiskAttachedToVm(vm), isValid());
    }

    @Test
    public void testDiskAttachedToVMFail() {
        VM vm = createVM();
        when(vmDao.getVmsListForDisk(Matchers.any(Guid.class), anyBoolean())).thenReturn(Collections.emptyList());
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM));
    }

    @Test
    public void testDiskAttachedToVMFailWithCorrectReplacements() {
        VM vm = createVM();
        vm.setName("MyVm");
        disk.setDiskAlias("MyDisk");
        when(vmDao.getVmsListForDisk(Matchers.any(Guid.class), anyBoolean())).thenReturn(Collections.emptyList());
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_NAME_VARIABLE, vm.getName())};
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM, expectedReplacements));
    }

    private LunDisk createLunDisk(ScsiGenericIO sgio, boolean isUsingScsiReservation) {
        LunDisk lunDisk = createLunDisk();
        lunDisk.setSgio(sgio);
        lunDisk.setUsingScsiReservation(isUsingScsiReservation);

        return lunDisk;
    }
}
