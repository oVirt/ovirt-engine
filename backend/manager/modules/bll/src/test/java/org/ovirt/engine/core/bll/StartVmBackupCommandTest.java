package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({ MockitoExtension.class, MockConfigExtension.class})
public class StartVmBackupCommandTest extends BaseCommandTest {

    public static Stream<MockConfigDescriptor<?>> mockConfigIsIncrementalBackupSupported() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.IsIncrementalBackupSupported, Version.getLast(), true));
    }

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
    @Mock
    private VmCheckpointDao vmCheckpointDao;

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
        initCluster();
        mockDisksImages();
        doReturn(diskExistenceValidator).when(command).createDiskExistenceValidator(any());
        doReturn(diskImagesValidator).when(command).createDiskImagesValidator(any());
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedDiskNotExists() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST))
                .when(diskExistenceValidator).disksNotExist();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVmNotQualifiedForBackup() {
        mockVm(VMStatus.Down);
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_VM_SHOULD_BE_IN_UP_STATUS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedBackupAlreadyInProgress() {
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_ALREADY_IN_PROGRESS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVdsNotSupportBackup() {
        mockVds(false);
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_NOT_SUPPORTED_BY_VDS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedNotAllDisksSupportsIncremental() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS))
                .when(diskImagesValidator).incrementalBackupEnabled();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedMixedBackup() {
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        doReturn(Set.of(diskImages)).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_MIXED_INCREMENTAL_AND_FULL_BACKUP_NOT_SUPPORTED);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateVmQualifiedForBackup() {
        mockVds(true);
        mockVm(VMStatus.Up);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private VmBackup mockVmBackup() {
        VmBackup vmBackup = new VmBackup();
        vmBackup.setId(Guid.newGuid());
        vmBackup.setDisks(diskImages);
        vmBackup.setVmId(vmId);
        vmBackup.setFromCheckpointId(fromCheckpointId);
        vmBackup.setToCheckpointId(null);
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

    private void initCluster() {
        Cluster cluster = new Cluster();
        cluster.setClusterId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.getLast());
        command.setClusterId(cluster.getId());
        command.setCluster(cluster);
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
