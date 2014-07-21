package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.LockManager;

/** A test case for {@link RemoveDiskCommandTest} */
@RunWith(MockitoJUnitRunner.class)
public class RemoveDiskCommandTest {

    private LockManager lockManager = new InMemoryLockManager();

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.LOCK_MANAGER, lockManager);

    @Mock
    private VmDAO vmDao;

    @Mock
    private VmDeviceDAO vmDeviceDao;

    private RemoveDiskCommand<RemoveDiskParameters> cmd;
    private DiskImage disk;
    private VM vm;
    private VmDevice vmDevice;

    @Before
    public void setUp() {
        Guid diskId = Guid.newGuid();

        disk = new DiskImage();
        disk.setId(diskId);
        disk.setVmEntityType(VmEntityType.VM);

        Guid vmId = Guid.newGuid();
        vm = new VM();
        vm.setId(vmId);

        VmDeviceId vmDeviceId = new VmDeviceId(diskId, vmId);
        vmDevice = new VmDevice();
        vmDevice.setId(vmDeviceId);
        vmDevice.setIsPlugged(true);

        when(vmDao.getVmsListForDisk(diskId, Boolean.TRUE)).thenReturn(Collections.singletonList(vm));
        when(vmDeviceDao.get(vmDeviceId)).thenReturn(vmDevice);

        RemoveDiskParameters params = new RemoveDiskParameters(diskId);

        cmd = spy(new RemoveDiskCommand<RemoveDiskParameters>(params));
        doReturn(disk).when(cmd).getDisk();
        doReturn(vmDeviceDao).when(cmd).getVmDeviceDAO();
        doReturn(vmDao).when(cmd).getVmDAO();
    }

    /* Tests for canDoAction() flow */

    @Test
    public void testCanDoActionFlowImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
    }

    @Test
    public void testCanDoActionVmUp() {
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void testCanDoActionOvfDiskNotIllegal() {
        disk.setImageStatus(ImageStatus.OK);
        disk.setOvfStore(true);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS);
    }
}
