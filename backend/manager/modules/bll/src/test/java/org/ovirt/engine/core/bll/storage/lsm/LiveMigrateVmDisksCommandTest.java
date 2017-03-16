package org.ovirt.engine.core.bll.storage.lsm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;

public class LiveMigrateVmDisksCommandTest extends BaseCommandTest {

    private final Guid diskImageId = Guid.newGuid();
    private final Guid diskImageGroupId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final Guid dstStorageId = Guid.newGuid();
    private final Guid vmId = Guid.newGuid();
    private final Guid quotaId = Guid.newGuid();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid diskProfileId = Guid.newGuid();

    private StoragePool storagePool;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VmDao vmDao;

    @Mock
    protected SnapshotDao snapshotDao;

    @Mock
    private VmValidator vmValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    /**
     * The command under test
     */
    @Spy
    @InjectMocks
    protected LiveMigrateVmDisksCommand<LiveMigrateVmDisksParameters> command =
            new LiveMigrateVmDisksCommand<>(new LiveMigrateVmDisksParameters(new ArrayList<>(), vmId), null);

    @Before
    public void setupCommand() {
        initSpyCommand();
        initStoragePool();
        mockValidators();
    }

    private void initSpyCommand() {
        doReturn(true).when(command).validateDestDomainsSpaceRequirements();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).validateCreateAllSnapshotsFromVmCommand();
    }

    private List<LiveMigrateDiskParameters> createLiveMigrateVmDisksParameters() {
        return Collections.singletonList(new LiveMigrateDiskParameters(diskImageId,
                srcStorageId,
                dstStorageId,
                vmId,
                quotaId,
                diskProfileId,
                diskImageGroupId));
    }

    private List<LiveMigrateDiskParameters> createLiveMigrateVmDisksParameters(Guid srcStorageId, Guid dstStorageId) {
        return Collections.singletonList(new LiveMigrateDiskParameters(diskImageId,
                srcStorageId,
                dstStorageId,
                vmId,
                quotaId,
                diskProfileId,
                diskImageGroupId));
    }

    private void createParameters() {
        command.getParameters().setParametersList(createLiveMigrateVmDisksParameters());
        command.getParameters().setVmId(vmId);
    }

    private void createParameters(Guid srcStorageId, Guid dstStorageId) {
        command.getParameters().setParametersList(createLiveMigrateVmDisksParameters(srcStorageId, dstStorageId));
        command.getParameters().setVmId(vmId);
    }

    @Test
    public void validateFailsWhenCreateAllSnapshotFromVmValidationFails() {
        createParameters(srcStorageId, dstStorageId);

        StorageDomain srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setStatus(StorageDomainStatus.Active);

        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setStatus(StorageDomainStatus.Active);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(false).when(command).validateCreateAllSnapshotsFromVmCommand();
        assertFalse(command.validate());
    }

    @Test
    public void validateNoDisksSpecified() {
        initVm(VMStatus.Up, Guid.newGuid(), null);
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED.toString()));
    }

    @Test
    public void validateVmShareableDisk() {
        createParameters();

        DiskImage diskImage = initDiskImage(diskImageGroupId, diskImageId);
        diskImage.setShareable(true);

        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED.toString()));
    }

    @Test
    public void validateMissingTemplateDisk() {
        createParameters();

        DiskImage diskImage = initDiskImage(diskImageGroupId, diskImageId);
        Guid templateImageId = Guid.newGuid();
        diskImage.setImageTemplateId(templateImageId);

        initDiskImage(Guid.newGuid(), templateImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN.toString()));
    }

    @Test
    public void validateInvalidDestinationDomain() {
        createParameters();

        StorageDomain srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setStatus(StorageDomainStatus.Active);

        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setStatus(StorageDomainStatus.Active);
        dstStorageDomain.setStorageDomainType(StorageDomainType.ISO);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL.toString()));
    }

    @Test
    public void validateSameSourceAndDest() throws Exception {
        createParameters(srcStorageId, srcStorageId);

        StorageDomain srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setStatus(StorageDomainStatus.Active);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.name()));
    }

    @Test
    public void validateFileToBlockSupported() {
        storagePool.setCompatibilityVersion(Version.v3_6);
        validateInvalidDestinationAndSourceDomainOfDifferentStorageSubtypes(StorageType.NFS, StorageType.ISCSI);
    }

    @Test
    public void validateBlockToFileSupported() {
        storagePool.setCompatibilityVersion(Version.v3_6);
        validateInvalidDestinationAndSourceDomainOfDifferentStorageSubtypes(StorageType.ISCSI, StorageType.NFS);
    }

    @Test
    public void validateBlockToBlock() {
        validateInvalidDestinationAndSourceDomainOfDifferentStorageSubtypes(StorageType.ISCSI, StorageType.ISCSI);
    }

    private void validateInvalidDestinationAndSourceDomainOfDifferentStorageSubtypes(StorageType sourceType, StorageType destType) {
        createParameters();

        StorageDomain srcStorageDomain = initStorageDomain(srcStorageId);
        srcStorageDomain.setStatus(StorageDomainStatus.Active);
        srcStorageDomain.setStorageType(sourceType);

        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setStatus(StorageDomainStatus.Active);
        dstStorageDomain.setStorageType(destType);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertTrue(command.validate());
    }

    @Test
    public void validateVmHavingDeviceSnapshotsPluggedToOtherVmsThatAreNotDown() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN)).when(diskValidator)
                .isDiskPluggedToVmsThatAreNotDown(anyBoolean(), any());

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.name()));
    }

    @Test
    public void validateFailsOnPassDiscardSupport() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        StorageDomain srcSd = initStorageDomain(srcStorageId);
        srcSd.setStatus(StorageDomainStatus.Active);

        StorageDomain dstSd = initStorageDomain(dstStorageId);
        dstSd.setStatus(StorageDomainStatus.Active);

        when(diskVmElementValidator.isPassDiscardSupported(any(Guid.class))).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
    }

    @Test
    public void passDiscardNotSupportedOnDestSd() {
        Guid dstSd = Guid.newGuid();
        when(diskVmElementValidator.isPassDiscardSupported(dstSd)).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        assertFalse(command.validatePassDiscardSupportedOnDestinationStorageDomain(
                new LiveMigrateDiskParameters(Guid.newGuid(), Guid.newGuid(), dstSd, Guid.newGuid(),
                        Guid.newGuid(), Guid.newGuid(), Guid.newGuid())));
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);
        vm.setStoragePoolId(storagePoolId);

        doReturn(vm).when(command).getVm();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        when(vmDao.getVmsListForDisk(diskImageId, Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
    }

    private DiskImage initDiskImage(Guid diskImageGroupId, Guid diskImageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(diskImageGroupId);
        diskImage.getImage().setId(diskImageId);
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(srcStorageId)));

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
        storagePool = new StoragePool();

        when(storagePoolDao.get(any(Guid.class))).thenReturn(storagePool);
        when(command.getStoragePoolId()).thenReturn(storagePoolId);
    }

    /** Mock Daos */

    private void mockValidators() {
        doReturn(vmValidator).when(command).createVmValidator();
        doReturn(diskValidator).when(command).createDiskValidator(any());
        doReturn(diskVmElementValidator).when(command).createDiskVmElementValidator(any(), any());
    }
}
