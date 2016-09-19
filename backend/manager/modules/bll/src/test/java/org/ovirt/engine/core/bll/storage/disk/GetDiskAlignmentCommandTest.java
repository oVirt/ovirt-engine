package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
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
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

/** A test case for {@link GetDiskAlignmentCommandTest} */
public class GetDiskAlignmentCommandTest extends BaseCommandTest {
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
    private ClusterDao clusterDao;

    private Guid diskId = Guid.newGuid();
    private Guid groupId;
    private DiskImage disk;
    private VM vm;
    private StoragePool storagePool;
    private StorageDomain storageDomain;

    @Spy
    @InjectMocks
    private GetDiskAlignmentCommand<GetDiskAlignmentParameters> cmd =
            new GetDiskAlignmentCommand<>(new GetDiskAlignmentParameters(diskId), null);

    @Before
    public void setUp() {
        Guid vmId = Guid.newGuid();
        Guid poolId = Guid.newGuid();
        Guid storageDomainId = Guid.newGuid();
        groupId = Guid.newGuid();
        Guid vdsId = Guid.newGuid();

        disk = new DiskImage();
        disk.setId(diskId);
        disk.setVmEntityType(VmEntityType.VM);
        disk.setImageStatus(ImageStatus.OK);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomainId)));

        vm = new VM();
        vm.setId(vmId);
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(poolId);
        vm.setClusterId(groupId);

        VmDeviceId vmDeviceId = new VmDeviceId(diskId, vmId);
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(vmDeviceId);

        Cluster cluster = new Cluster();
        cluster.setId(groupId);

        VDS vds = new VDS();
        vds.setId(vdsId);

        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);

        storageDomain = new StorageDomain();
        storageDomain.setStorageType(StorageType.ISCSI);

        when(vmDao.getVmsListForDisk(diskId, Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
        when(vmDeviceDao.get(vmDeviceId)).thenReturn(vmDevice);
        when(vdsDao.getAllForClusterWithStatus(groupId, VDSStatus.Up)).thenReturn(Collections.singletonList(vds));
        when(spDao.get(poolId)).thenReturn(storagePool);
        when(clusterDao.get(groupId)).thenReturn(cluster);
        when(storageDomainStaticDao.get(storageDomainId)).thenReturn(storageDomain.getStorageStaticData());

        doReturn(disk).when(cmd).getDisk();
    }

    /* Tests for validate() flow */

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidateImageIsLocked() {
        disk.setImageStatus(ImageStatus.LOCKED);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testValidateFloatingDisk() {
        when(vmDao.getVmsListForDisk(diskId, Boolean.FALSE)).thenReturn(Collections.emptyList());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
    }

    @Test
    public void testValidateVmRunningFail() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ERROR_CANNOT_RUN_ALIGNMENT_SCAN_VM_IS_RUNNING);
    }

    @Test
    public void testValidateVdsNotFound() {
        when(vdsDao.getAllForClusterWithStatus(groupId, VDSStatus.Up))
                .thenReturn(Collections.emptyList());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
    }

    @Test
    public void testValidateStoragePoolDown() {
        storagePool.setStatus(StoragePoolStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void testValidateStorageDomainIsFileStorage() {
        storageDomain.setStorageType(StorageType.NFS);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ALIGNMENT_SCAN_STORAGE_TYPE);
    }
}
