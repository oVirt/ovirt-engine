package org.ovirt.engine.core.bll.lsm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmDAO;

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

    @Mock
    private VmValidator vmValidator;

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
        initVm(VMStatus.Up, Guid.NewGuid(), null);
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
        diskImage.setImageTemplateId(templateDiskId);

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

        StorageDomain storageDomain = initStorageDomain(srcStorageId);
        storageDomain.setStatus(StorageDomainStatus.Locked);

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

        StorageDomain srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setStatus(StorageDomainStatus.Active);

        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setStatus(StorageDomainStatus.Active);
        dstStorageDomain.setStorageDomainType(StorageDomainType.ISO);

        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL.toString()));
    }

    @Test
    public void canDoActionVmRunningStateless() {
        createParameters();
        initDiskImage(diskImageId);
        initVm(VMStatus.Up, Guid.NewGuid(), diskImageId);

        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS)).when(vmValidator)
                .vmNotRunningStateless();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS.name()));
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, NGuid runOnVds, Guid diskImageId) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);
        vm.setStoragePoolId(storagePoolId);

        doReturn(vm).when(command).getVm();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        when(vmDao.getVmsListForDisk(diskImageId)).thenReturn(Collections.singletonList(vm));
    }

    private DiskImage initDiskImage(Guid diskImageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(diskImageId);
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(srcStorageId)));

        when(diskImageDao.getAncestor(diskImageId)).thenReturn(diskImage);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);

        return diskImage;
    }

    private StorageDomain initStorageDomain(Guid storageDomainId) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        storageDomain.setStoragePoolId(storagePoolId);

        when(storageDomainDao.get(any(Guid.class))).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        return storageDomain;
    }

    private void initStoragePool() {
        StoragePool storagePool = new StoragePool();
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
        mockValidators();
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

    private void mockValidators() {
        doReturn(vmValidator).when(command).createVmValidator();
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotRunningStateless();
    }
}
