package org.ovirt.engine.core.bll.storage.lsm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class LiveMigrateDiskCommandTest extends BaseCommandTest {

    private final Guid diskImageId = Guid.newGuid();
    private final Guid diskImageGroupId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final Guid dstStorageId = Guid.newGuid();
    private final Guid vmId = Guid.newGuid();
    private final Guid quotaId = Guid.newGuid();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid diskProfileId = Guid.newGuid();

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
    private DiskValidator diskValidator;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private DiskDao diskDao;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private DiskProfileHelper diskProfileHelper;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private MultipleStorageDomainsValidator multipleStorageDomainsValidator;

    /**
     * The command under test
     */
    @Spy
    @InjectMocks
    protected LiveMigrateDiskCommand<LiveMigrateDiskParameters> command =
            new LiveMigrateDiskCommand<>(createLiveMigrateDiskParameters(), null);

    @BeforeEach
    public void setupCommand() {
        initSpyCommand();
        initStoragePool();
        mockValidators();
    }

    private void initSpyCommand() {
        doReturn(true).when(command).validateDestDomainsSpaceRequirements();
        doReturn(true).when(command).validateCreateAllSnapshotsFromVmCommand();
        doReturn(true).when(command).setAndValidateQuota();
        doReturn(ActionType.LiveMigrateDisk).when(command).getActionType();
    }

    private LiveMigrateDiskParameters createLiveMigrateDiskParameters() {
        return new LiveMigrateDiskParameters(diskImageId,
                srcStorageId,
                dstStorageId,
                vmId,
                quotaId,
                diskProfileId,
                diskImageGroupId);
    }

    @Test
    public void validateFailsWhenCreateAllSnapshotFromVmValidationFails() {
        initStorageDomain(srcStorageId);
        initStorageDomain(dstStorageId);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(false).when(command).validateCreateAllSnapshotsFromVmCommand();
        assertFalse(command.validate());
    }

    @Test
    public void validateVmShareableDisk() {
        initStorageDomain(srcStorageId);
        initStorageDomain(dstStorageId);

        DiskImage diskImage = initDiskImage(diskImageGroupId, diskImageId);
        diskImage.setShareable(true);

        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED);
    }

    @Test
    public void validateLiveMigrateDownVmFails() {
        initStorageDomain(srcStorageId);
        initStorageDomain(dstStorageId);
        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Down, Guid.newGuid(), diskImageGroupId);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.CANNOT_LIVE_MIGRATE_VM_SHOULD_BE_IN_PAUSED_OR_UP_STATUS);
    }

    @Test
    public void validateInvalidDestinationDomain() {
        initStorageDomain(srcStorageId);

        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setStorageDomainType(StorageDomainType.ISO);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void validateVmHavingDeviceSnapshotsPluggedToOtherVmsThatAreNotDown() {
        initStorageDomain(srcStorageId);
        initStorageDomain(dstStorageId);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN)).when(diskValidator)
                .isDiskPluggedToAnyNonDownVm(anyBoolean());

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void validateCantLiveMigrateToBackupDomain() {
        initStorageDomain(srcStorageId);
        StorageDomain dstStorageDomain = initStorageDomain(dstStorageId);
        dstStorageDomain.setBackup(true);

        initDiskImage(diskImageGroupId, diskImageId);
        initVm(VMStatus.Up, Guid.newGuid(), diskImageGroupId);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE);
    }

    /** Initialize Entities */

    private void initVm(VMStatus vmStatus, Guid runOnVds, Guid diskImageId) {
        VM vm = new VM();
        vm.setStatus(vmStatus);
        vm.setRunOnVds(runOnVds);
        vm.setStoragePoolId(storagePoolId);

        doReturn(vm).when(command).getVm();
        when(vmDao.get(any())).thenReturn(vm);
        when(vmDao.getVmsListForDisk(diskImageId, Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
    }

    private DiskImage initDiskImage(Guid diskImageGroupId, Guid diskImageId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(diskImageGroupId);
        diskImage.getImage().setId(diskImageId);
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(srcStorageId)));
        diskImage.setVmEntityType(VmEntityType.VM);

        when(diskImageDao.getAncestor(diskImageId)).thenReturn(diskImage);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);

        return diskImage;
    }

    private StorageDomain initStorageDomain(Guid storageDomainId) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        storageDomain.setStoragePoolId(storagePoolId);
        storageDomain.setStatus(StorageDomainStatus.Active);

        command.setStorageDomain(storageDomain);
        when(storageDomainDao.get(any())).thenReturn(storageDomain);
        when(storageDomainDao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);


        return storageDomain;
    }

    private void initStoragePool() {
        StoragePool storagePool = new StoragePool();

        command.setStoragePoolId(storagePoolId);
        when(storagePoolDao.get(any())).thenReturn(storagePool);
    }

    /** Mock Daos */

    private void mockValidators() {
        doReturn(diskValidator).when(command).createDiskValidator(any());
        doReturn(diskImagesValidator).when(command).createDiskImagesValidator(any());
        doReturn(multipleStorageDomainsValidator).when(command).createMultipleStorageDomainsValidator(any());
    }
}
