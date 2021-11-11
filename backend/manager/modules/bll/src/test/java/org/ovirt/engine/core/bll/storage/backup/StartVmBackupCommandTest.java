package org.ovirt.engine.core.bll.storage.backup;

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
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.VmCheckpointState;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
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
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({ MockitoExtension.class, MockConfigExtension.class})
public class StartVmBackupCommandTest extends BaseCommandTest {

    public static Stream<MockConfigDescriptor<?>> mockConfigIsIncrementalBackupSupported() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.IsIncrementalBackupSupported, Version.getLast(), true),
                MockConfigDescriptor.of(ConfigValues.IsIncrementalBackupSupported, Version.v4_4, true));
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
    private SnapshotsValidator snapshotsValidator;
    @Mock
    private VmBackupDao vmBackupDao;
    @Mock
    private VmCheckpointDao vmCheckpointDao;
    @Mock
    private VmDeviceDao vmDeviceDao;

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
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any());
        when(vmCheckpointDao.get(fromCheckpointId)).thenReturn(mockVmCheckpoint(VmCheckpointState.CREATED));
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
    public void validateFailedDiskLocked() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED))
                .when(diskImagesValidator).diskImagesNotLocked();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVmNotQualifiedForBackup() {
        mockVm(VMStatus.PoweringUp);
        mockVds(true, true);
        mockVmDevice(true);
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_VM_SHOULD_BE_IN_UP_OR_DOWN_STATUS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedBackupAlreadyInProgress() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);

        VmBackup vmBackup = mockVmBackup();
        vmBackup.setPhase(VmBackupPhase.STARTING);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(vmBackup));

        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedBackupIdAlreadyExists() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);

        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        when(vmBackupDao.get(any())).thenReturn(mockVmBackup());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_ID_ALREADY_EXIST);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVdsNotSupportBackup() {
        mockVds(false, true);
        mockVm(VMStatus.Up);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_NOT_SUPPORTED_BY_VDS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVdsNotSupportScratchDisksOnSharedStorage() {
        mockVds(true, false);
        mockVm(VMStatus.Up);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.CANNOT_START_BACKUP_USING_OUTDATED_HOST);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedNotAllDisksSupportsIncremental() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS))
                .when(diskImagesValidator).incrementalBackupEnabled();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedMixedBackup() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        doReturn(Set.of(diskImages)).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_MIXED_INCREMENTAL_AND_FULL_BACKUP_NOT_SUPPORTED);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateMixedBackupAllowed() {
        command.getCluster().setCompatibilityVersion(Version.getLast());
        mockVds(true, true);
        mockVm(VMStatus.Up);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedMissingCheckpoint() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        when(vmCheckpointDao.get(fromCheckpointId)).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_CHECKPOINT_NOT_EXIST);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedInvalidCheckpoint() {
        mockVm(VMStatus.Up);
        mockVds(true, true);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(List.of(mockVmBackup()));
        when(vmCheckpointDao.get(fromCheckpointId)).thenReturn(mockVmCheckpoint(VmCheckpointState.INVALID));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_CHECKPOINT_INVALID);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateVmQualifiedForBackup() {
        mockVds(true, true);
        mockVm(VMStatus.Up);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateFailedVmDeviceNotActive() {
        mockVds(true, true);
        mockVm(VMStatus.Up);
        mockVmDevice(false);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_ARE_NOT_ACTIVE);
    }

    @Test
    @MockedConfig("mockConfigIsIncrementalBackupSupported")
    public void validateVmInPreview() {
        mockVds(true, true);
        mockVm(VMStatus.Up);
        mockVmDevice(true);
        when(vmBackupDao.getAllForVm(vmId)).thenReturn(new ArrayList<>());
        doReturn(Collections.emptySet()).when(command).getDisksNotInPreviousCheckpoint();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW)).when(snapshotsValidator)
                .vmNotInPreview(any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
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

    private VmCheckpoint mockVmCheckpoint(VmCheckpointState state) {
        VmCheckpoint vmCheckpoint = new VmCheckpoint();
        vmCheckpoint.setId(fromCheckpointId);
        vmCheckpoint.setState(state);
        return vmCheckpoint;
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
        cluster.setCompatibilityVersion(Version.v4_4);
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

    private void mockVds(Boolean backupEnabled, Boolean coldBackupEnabled) {
        VDS vds = new VDS();
        vds.setBackupEnabled(backupEnabled);
        vds.setColdBackupEnabled(coldBackupEnabled);
        when(vdsDao.get(any())).thenReturn(vds);
    }

    private void mockVmDevice(Boolean isPlugged) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), Guid.newGuid()));
        vmDevice.setPlugged(isPlugged);
        when(vmDeviceDao.get(any())).thenReturn(vmDevice);
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
