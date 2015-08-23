package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.LockManager;


/** A test case for {@link GetDiskAlignmentCommandTest} */
public class GetDiskAlignmentCommandTest extends BaseCommandTest {
    private LockManager lockManager = new InMemoryLockManager();

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.LOCK_MANAGER, lockManager);

    @Mock
    private VmDao vmDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @Mock
    private StoragePoolDao spDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VdsGroupDao vdsGroupDao;

    private GetDiskAlignmentCommand<GetDiskAlignmentParameters> cmd;

    private Guid vmId, diskId, poolId, storageDomainId, groupId, vdsId;
    private DiskImage disk;
    private VM vm;
    private VDS vds;
    private VmDevice vmDevice;
    private StoragePool storagePool;
    private StorageDomain storageDomain;
    private VDSGroup vdsGroup;

    @Before
    public void setUp() {
        vmId = Guid.newGuid();
        diskId = Guid.newGuid();
        poolId = Guid.newGuid();
        storageDomainId = Guid.newGuid();
        groupId = Guid.newGuid();
        vdsId = Guid.newGuid();

        disk = new DiskImage();
        disk.setId(diskId);
        disk.setVmEntityType(VmEntityType.VM);
        disk.setImageStatus(ImageStatus.OK);
        disk.setStorageIds(new ArrayList<Guid>(Arrays.asList(storageDomainId)));

        vm = new VM();
        vm.setId(vmId);
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(poolId);
        vm.setVdsGroupId(groupId);

        VmDeviceId vmDeviceId = new VmDeviceId(diskId, vmId);
        vmDevice = new VmDevice();
        vmDevice.setId(vmDeviceId);

        vdsGroup = new VDSGroup();
        vdsGroup.setId(groupId);

        vds = new VDS();
        vds.setId(vdsId);

        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);

        storageDomain = new StorageDomain();
        storageDomain.setStorageType(StorageType.ISCSI);

        when(vmDao.getVmsListForDisk(diskId, Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
        when(vmDeviceDao.get(vmDeviceId)).thenReturn(vmDevice);
        when(vdsDao.getAllForVdsGroupWithStatus(groupId, VDSStatus.Up)).thenReturn(Collections.singletonList(vds));
        when(spDao.get(poolId)).thenReturn(storagePool);
        when(vdsGroupDao.get(groupId)).thenReturn(vdsGroup);
        when(storageDomainStaticDao.get(storageDomainId)).thenReturn(storageDomain.getStorageStaticData());

        cmd = spy(new GetDiskAlignmentCommand<GetDiskAlignmentParameters>(new GetDiskAlignmentParameters(diskId)));

        doReturn(disk).when(cmd).getDisk();
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(vmDao).when(cmd).getVmDao();
        doReturn(spDao).when(cmd).getStoragePoolDao();
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDao();
        doReturn(storageDomainStaticDao).when(cmd).getStorageDomainStaticDao();
    }

    /* Tests for canDoAction() flow */

    @Test
    public void testCanDoActionSuccess() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testCanDoActionImageIsLocked() {
        disk.setImageStatus(ImageStatus.LOCKED);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testCanDoActionFloatingDisk() {
        when(vmDao.getVmsListForDisk(diskId, Boolean.FALSE)).thenReturn(Collections.<VM>emptyList());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
    }

    @Test
    public void testCanDoActionVmRunningFail() {
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ERROR_CANNOT_RUN_ALIGNMENT_SCAN_VM_IS_RUNNING);
    }

    @Test
    public void testCanDoActionVdsNotFound() {
        when(vdsDao.getAllForVdsGroupWithStatus(groupId, VDSStatus.Up))
                .thenReturn(Collections.<VDS>emptyList());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
    }

    @Test
    public void testCanDoActionStoragePoolDown() {
        storagePool.setStatus(StoragePoolStatus.Maintenance);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void testCanDoActionStorageDomainIsFileStorage() {
        storageDomain.setStorageType(StorageType.NFS);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ALIGNMENT_SCAN_STORAGE_TYPE);
    }
}
