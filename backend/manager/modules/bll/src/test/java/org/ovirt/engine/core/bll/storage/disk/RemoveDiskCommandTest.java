package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.LockManager;

/** A test case for {@link RemoveDiskCommandTest} */
public class RemoveDiskCommandTest extends BaseCommandTest {

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
    private Disk disk;
    private VM vm;
    private VmDevice vmDevice;

    @Before
    public void setUp() {
        disk = new DiskImage();
        setupDisk();

        Guid vmId = Guid.newGuid();
        vm = new VM();
        vm.setId(vmId);

        VmDeviceId vmDeviceId = new VmDeviceId(disk.getId(), vmId);
        vmDevice = new VmDevice();
        vmDevice.setId(vmDeviceId);
        vmDevice.setIsPlugged(true);

        when(vmDao.getVmsListForDisk(disk.getId(), Boolean.TRUE)).thenReturn(Collections.singletonList(vm));
        when(vmDeviceDao.get(vmDeviceId)).thenReturn(vmDevice);

        RemoveDiskParameters params = new RemoveDiskParameters(disk.getId());

        cmd = spy(new RemoveDiskCommand<>(params, null));
        doReturn(disk).when(cmd).getDisk();
        doReturn(vmDeviceDao).when(cmd).getVmDeviceDao();
        doReturn(vmDao).when(cmd).getVmDao();
    }

    protected void setupDisk() {
        disk.setId(Guid.newGuid());
        disk.setVmEntityType(VmEntityType.VM);
    }

    /* Tests for validate() flow */

    @Test
    public void testValidateFlowImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
    }

    @Test
    public void testValidateVmUp() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }


    @Test
    public void testValidateTemplateEntity() {
        disk.setVmEntityType(VmEntityType.TEMPLATE);

        StorageDomain domain = new StorageDomain();
        domain.setId(Guid.newGuid());
        domain.setStatus(StorageDomainStatus.Active);
        cmd.getParameters().setStorageDomainId(domain.getId());

        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(domain.getId());
        storageIds.add(Guid.newGuid());
        ((DiskImage)disk).setStorageIds(storageIds);

        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(true).when(cmd).checkDerivedDisksFromDiskNotExist(any(DiskImage.class));
        doReturn(disk).when(diskImageDao).get(any(Guid.class));
        doReturn(diskImageDao).when(cmd).getDiskImageDao();

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateTemplateWithNoDomain() {
        disk.setVmEntityType(VmEntityType.TEMPLATE);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CANT_DELETE_TEMPLATE_DISK_WITHOUT_SPECIFYING_DOMAIN);
    }

    @Test
    public void testValidateOvfDiskNotIllegal() {
        ((DiskImage)disk).setImageStatus(ImageStatus.OK);
        disk.setContentType(DiskContentType.OVF_STORE);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS);
    }

    @Test
    public void testPreImportedHostedEngineLunDiskRemove() {
        disk = new LunDisk();
        setupDisk();
        disk.setDiskAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        doReturn(disk).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportedHostedEngineLunDiskRemove() {
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vm.setStatus(VMStatus.Up);
        disk = new LunDisk();
        setupDisk();
        disk.setDiskAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        doReturn(disk).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportedHostedEngineImageDiskRemove() {
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vm.setStatus(VMStatus.Up);
        doReturn(disk).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateFailure(
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DISK);
    }

}
