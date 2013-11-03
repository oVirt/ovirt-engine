package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class DiskValidatorTest {

    @Mock
    private VmDAO vmDAO;
    private DiskValidator validator;
    private DiskImage disk;

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        return disk;
    }

    private static VM createVM() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        return vm;
    }

    @Before
    public void setUp() {
        disk = createDisk();
        disk.setDiskAlias("disk1");
        validator = spy(new DiskValidator(disk));
        doReturn(vmDAO).when(validator).getVmDAO();
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
}
