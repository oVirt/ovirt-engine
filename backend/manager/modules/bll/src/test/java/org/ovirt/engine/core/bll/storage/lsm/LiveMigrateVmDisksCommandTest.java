package org.ovirt.engine.core.bll.storage.lsm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
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
        command = spy(new LiveMigrateVmDisksCommand<>(new LiveMigrateVmDisksParameters(new ArrayList<>(), vmId), null));

        doReturn(true).when(command).validateSpaceRequirements();
        doReturn(true).when(command).checkImagesStatus();
        doReturn(true).when(command).setAndValidateDiskProfiles();
    }

    private List<LiveMigrateDiskParameters> createLiveMigrateVmDisksParameters() {
        return Arrays.asList(new LiveMigrateDiskParameters(diskImageId,
                srcStorageId,
                dstStorageId,
                vmId,
                quotaId,
                diskProfileId,
                diskImageGroupId));
    }

    private List<LiveMigrateDiskParameters> createLiveMigrateVmDisksParameters(Guid srcStorageId, Guid dstStorageId) {
        return Arrays.asList(new LiveMigrateDiskParameters(diskImageId,
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
    public void validateInvalidSourceDomain() {
        createParameters();

        StorageDomain storageDomain = initStorageDomain(srcStorageId);
        storageDomain.setStatus(StorageDomainStatus.Locked);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString()));
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
    public void validateVmRunningStateless() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS)).when(vmValidator)
                .vmNotRunningStateless();

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS.name()));
    }

    @Test
    public void validateVmInPreview() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, null, diskImageId);
        setVmInPreview(true);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW)).when(snapshotsValidator)
                .vmNotInPreview(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void validateVmDuringSnapshot() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, null, diskImageId);
        when(snapshotDao.exists(any(Guid.class), eq(Snapshot.SnapshotStatus.LOCKED))).thenReturn(true);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT)).when(snapshotsValidator)
                .vmNotDuringSnapshot(any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    @Test
    public void validateVmHavingDeviceSnapshotsPluggedToOtherVmsThatAreNotDown() {
        createParameters();
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN)).when(diskValidator)
                .isDiskPluggedToVmsThatAreNotDown(anyBoolean(), anyList());

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.name()));
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
        diskImage.setStorageIds(new ArrayList<>(Arrays.asList(srcStorageId)));

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

    private void setVmInPreview(boolean isInPreview) {
        when(snapshotDao.exists(any(Guid.class), eq(Snapshot.SnapshotStatus.IN_PREVIEW))).thenReturn(isInPreview);
    }

    /** Mock Daos */

    private void mockDaos() {
        mockVmDao();
        mockDiskImageDao();
        mockStorageDomainDao();
        mockStoragePoolDao();
        mockValidators();
    }

    private void mockVmDao() {
        doReturn(vmDao).when(command).getVmDao();
    }

    private void mockDiskImageDao() {
        doReturn(diskImageDao).when(command).getDiskImageDao();
    }

    private void mockStorageDomainDao() {
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
    }

    private void mockStoragePoolDao() {
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
    }

    private void mockValidators() {
        doReturn(vmValidator).when(command).createVmValidator();
        doReturn(snapshotsValidator).when(command).createSnapshotsValidator();
        doReturn(diskValidator).when(command).createDiskValidator(any(Disk.class));
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotRunningStateless();
        doReturn(ValidationResult.VALID).when(diskValidator).isDiskPluggedToVmsThatAreNotDown(anyBoolean(), anyList());
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
    }
}
