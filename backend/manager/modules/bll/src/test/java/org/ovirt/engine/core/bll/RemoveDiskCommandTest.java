package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
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
    private VmDao vmDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private DiskImageDao diskImageDao;

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
        doReturn(vmDeviceDao).when(cmd).getVmDeviceDao();
        doReturn(vmDao).when(cmd).getVmDao();
    }

    /* Tests for canDoAction() flow */

    @Test
    public void testCanDoActionFlowImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
    }

    @Test
    public void testCanDoActionVmUp() {
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }


    @Test
    public void testCanDoActionTemplateEntity() {
        disk.setVmEntityType(VmEntityType.TEMPLATE);

        StorageDomain domain = new StorageDomain();
        domain.setId(Guid.newGuid());
        domain.setStatus(StorageDomainStatus.Active);
        cmd.getParameters().setStorageDomainId(domain.getId());

        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(domain.getId());
        storageIds.add(Guid.newGuid());
        disk.setStorageIds(storageIds);

        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(true).when(cmd).checkDerivedDisksFromDiskNotExist(any(DiskImage.class));
        doReturn(disk).when(diskImageDao).get(any(Guid.class));
        doReturn(diskImageDao).when(cmd).getDiskImageDao();

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionOvfDiskNotIllegal() {
        disk.setImageStatus(ImageStatus.OK);
        disk.setContentType(DiskContentType.OVF_STORE);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS);
    }
}
