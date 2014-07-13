package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;

import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class DiskValidatorTest {

    @Mock
    private OsRepository osRepository;
    @Mock
    private VmDAO vmDAO;
    private DiskValidator validator;
    private DiskValidator lunValidator;
    private DiskImage disk;
    private LunDisk lunDisk;

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        return disk;
    }

    private static VM createVM() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        vm.setVmOs(1);
        return vm;
    }

    private void initializeOsRepository (int osId, DiskInterface diskInterface) {
        ArrayList<String> supportedDiskInterfaces = new ArrayList<>();
        supportedDiskInterfaces.add(diskInterface.name());
        when(osRepository.getDiskInterfaces(1, null)).thenReturn(supportedDiskInterfaces);
        when(osRepository.getDiskInterfaces(2, null)).thenReturn(new ArrayList<String>());
        // init the injector with the osRepository instance
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    @Before
    public void setUp() {
        initializeOsRepository(1, DiskInterface.VirtIO);
        disk = createDisk();
        disk.setDiskAlias("disk1");
        disk.setDiskInterface(DiskInterface.VirtIO);
        validator = spy(new DiskValidator(disk));
        doReturn(vmDAO).when(validator).getVmDAO();
    }

    private void setupForLun() {
        lunDisk = new LunDisk();
        lunValidator = spy(new DiskValidator(lunDisk));
        doReturn(vmDAO).when(lunValidator).getVmDAO();
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
        when(vmDAO.getVmsWithPlugInfo(disk.getId())).thenReturn(vmsInfo);
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
    }

    @Test
    public void diskInterfaceSupportedByOs() {
        VM vm = createVM();
        initializeOsRepository(vm.getOs(), DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), isValid());
    }

    @Test
    public void diskInterfaceNotSupportedByOs() {
        VM vm = createVM();
        vm.setVmOs(2);
        initializeOsRepository(vm.getOs(), DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), failsWith(VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
    }

    @Test
    public void readOnlyIsNotSupportedByDiskInterface() {
        disk.setReadOnly(true);
        disk.setDiskInterface(DiskInterface.IDE);

        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IDE_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR));

        setupForLun();
        lunDisk.setReadOnly(true);
        lunDisk.setDiskInterface(DiskInterface.VirtIO_SCSI);
        lunDisk.setSgio(ScsiGenericIO.FILTERED);
        assertThat(lunValidator.isReadOnlyPropertyCompatibleWithInterface(),
                failsWith(VdcBllMessages.SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK));
    }

    @Test
    public void readOnlyIsSupportedByDiskInterface() {
        disk.setReadOnly(true);
        disk.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        disk.setReadOnly(false);
        disk.setDiskInterface(DiskInterface.IDE);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());
    }
}
