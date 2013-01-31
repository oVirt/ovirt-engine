package org.ovirt.engine.core.bll.lsm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class LiveMigrateVmDisksCommandTest {

    private final Guid diskImageId = Guid.NewGuid();
    private final Guid srcStorageId = Guid.NewGuid();
    private final Guid dstStorageId = Guid.NewGuid();
    private final Guid vmId = Guid.NewGuid();
    private final Guid quotaId = Guid.NewGuid();
    private final Guid storagePoolId = Guid.NewGuid();
    private final Guid templateDiskId = Guid.NewGuid();

    @Mock
    private DiskImageDAO diskImageDao;

    @Mock
    private StorageDomainDAO storageDomainDao;

    @Mock
    private StoragePoolDAO storagePoolDao;

    @Mock
    private VmDAO vmDao;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.LiveStorageMigrationEnabled, Version.v3_1.toString(), true)
            );

    /**
     * The command under test
     */
    protected LiveMigrateVmDisksCommand<LiveMigrateVmDisksParameters> command;

    @Before
    public void setupCommand() {
        initSpyCommand();
        initStoragePool();
        mockDaos();
    }

    private void initSpyCommand() {
        command = spy(new LiveMigrateVmDisksCommand<LiveMigrateVmDisksParameters>(
                new LiveMigrateVmDisksParameters(new ArrayList<LiveMigrateDiskParameters>(), vmId)));

        doReturn(true).when(command).isValidSpaceRequirements();
        doReturn(true).when(command).checkImagesStatus();
    }

    private List<LiveMigrateDiskParameters> createLiveMigrateVmDisksParameters() {
        return Arrays.asList(new LiveMigrateDiskParameters(diskImageId, srcStorageId, dstStorageId, vmId, quotaId));
    }

    private void createParameters() {
        command.getParameters().setParametersList(createLiveMigrateVmDisksParameters());
        command.getParameters().setVmId(vmId);
    }

    @Test
    public void canDoActionNoDisksSpecified() {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED.toString()));
    }

    @Test
    public void canDoActionVmShareableDisk() {
        createParameters();

        DiskImage diskImage = initDiskImage(diskImageId);
        diskImage.setShareable(true);

        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED.toString()));
    }

    @Test
    public void canDoActionMissingTemplateDisk() {
        createParameters();

        DiskImage diskImage = initDiskImage(diskImageId);
        diskImage.setit_guid(templateDiskId);

        initDiskImage(templateDiskId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN.toString()));
    }

    @Test
    public void canDoActionInvalidSourceDomain() {
        createParameters();

        storage_domains storageDomain = initStorageDomain(srcStorageId);
        storageDomain.setstatus(StorageDomainStatus.Locked);

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString()));
    }

    @Test
    public void canDoActionInvalidDestinationDomain() {
        createParameters();

        storage_domains srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setstatus(StorageDomainStatus.Active);

        storage_domains dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setstatus(StorageDomainStatus.Active);
        dstStorageDomain.setstorage_domain_type(StorageDomainType.ISO);

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL.toString()));
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, NGuid runOnVds, Guid diskImageId) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);

        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        when(vmDao.getVmsListForDisk(diskImageId)).thenReturn(Collections.singletonList(vm));
    }

    private DiskImage initDiskImage(Guid diskImageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(diskImageId);
        diskImage.setstorage_pool_id(storagePoolId);
        diskImage.setstorage_ids(new ArrayList<Guid>());

        when(diskImageDao.getAncestor(diskImageId)).thenReturn(diskImage);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);

        return diskImage;
    }

    private storage_domains initStorageDomain(Guid storageDomainId) {
        storage_domains storageDomain = new storage_domains();
        storageDomain.setId(storageDomainId);
        storageDomain.setstorage_pool_id(storagePoolId);

        when(storageDomainDao.get(any(Guid.class))).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        return storageDomain;
    }

    private void initStoragePool() {
        storage_pool storagePool = new storage_pool();
        storagePool.setcompatibility_version(Version.v3_1);

        when(storagePoolDao.get(any(Guid.class))).thenReturn(storagePool);
        when(command.getStoragePoolId()).thenReturn(storagePoolId);
    }

    /** Mock DAOs */

    private void mockDaos() {
        mockVmDao();
        mockDiskImageDao();
        mockStorageDomainDao();
        mockStoragePoolDao();
    }

    private void mockVmDao() {
        doReturn(vmDao).when(command).getVmDAO();
    }

    private void mockDiskImageDao() {
        doReturn(diskImageDao).when(command).getDiskImageDao();
    }

    private void mockStorageDomainDao() {
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
    }

    private void mockStoragePoolDao() {
        doReturn(storagePoolDao).when(command).getStoragePoolDAO();
    }
}
