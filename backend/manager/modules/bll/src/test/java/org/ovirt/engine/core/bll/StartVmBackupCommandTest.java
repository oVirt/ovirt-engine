package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class StartVmBackupCommandTest extends BaseCommandTest {

    @Mock
    private VmDao vmDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private DiskExistenceValidator diskExistenceValidator;
    @Mock
    private DiskImagesValidator diskImagesValidator;
    @Mock
    private VmBackupDao vmBackupDao;

    private Guid vmId = Guid.newGuid();
    private VM vm = new VM();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid storageDomainId = Guid.newGuid();
    private Guid fromCheckpointId = Guid.newGuid();
    private Guid diskImage1Guid = Guid.newGuid();
    private Guid diskImage2Guid = Guid.newGuid();
    private List<DiskImage> diskImages = new ArrayList<>();

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private StartVmBackupCommand<VmBackupParameters> command = createCommand();

    @BeforeEach
    public void setUp() {
        mockDisksImages();
        doReturn(diskExistenceValidator).when(command).createDiskExistenceValidator(any());
        doReturn(diskImagesValidator).when(command).createDiskImagesValidator(any());
    }

    @Test
    public void validateFailedDiskNotExists() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST))
                .when(diskExistenceValidator).disksNotExist();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST);
    }

    @Test
    public void validateFailedVmNotQualifiedForBackup() {
        mockVm(VMStatus.Down);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_VM_SHOULD_BE_IN_UP_STATUS);
    }

    @Test
    public void validateFailedBackupAlreadyInProgress() {
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_ALREADY_IN_PROGRESS);
    }

    @Test
    public void validateFailedVdsNotSupportBackup() {
        mockVds(false);
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_NOT_SUPPORTED_BY_VDS);
    }

    @Test
    public void validateFailedNotAllDisksSupportsIncremental() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS))
                .when(diskImagesValidator).incrementalBackupEnabled();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS);
    }

    @Test
    public void validateVmQualifiedForBackup() {
        mockVds(true);
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private VmBackup mockVmBackup() {
        VmBackup vmBackup = new VmBackup();
        vmBackup.setId(Guid.newGuid());
        vmBackup.setDisks(diskImages);
        vmBackup.setVmId(vmId);
        vmBackup.setFromCheckpointId(fromCheckpointId);
        return vmBackup;
    }

    private DiskImage mockDiskImage(Guid guid) {
        DiskImage disk = new DiskImage();
        disk.setImageId(guid);
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageDomainId);
        disk.setStorageIds(storageIds);
        disk.setStoragePoolId(storagePoolId);
        disk.setActive(true);
        disk.setId(Guid.newGuid());
        when(diskDao.get(guid)).thenReturn(disk);
        return disk;
    }

    private void mockDisksImages() {
        List.of(diskImage1Guid, diskImage2Guid).forEach(diskId -> {
            diskImages.add(mockDiskImage(diskId));
        });
    }

    private void mockVm(VMStatus vmStatus) {
        vm.setStatus(vmStatus);
        vm.setId(vmId);
        vm.setRunOnVds(Guid.newGuid());
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
    }

    private void mockVds(Boolean backupEnabled) {
        VDS vds = new VDS();
        vds.setBackupEnabled(backupEnabled);
        when(vdsDao.get(any())).thenReturn(vds);
    }

    private StartVmBackupCommand<VmBackupParameters> createCommand() {
        return new StartVmBackupCommand<>(createParameters(), null);
    }

    /**
     * @return Valid parameters for the command.
     */
    private VmBackupParameters createParameters() {
        return new VmBackupParameters(mockVmBackup());
    }
}
