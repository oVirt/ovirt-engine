package org.ovirt.engine.core.bll.snapshots;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VgamemVideoSettings;
import org.ovirt.engine.core.bll.utils.VideoDeviceSettings;
import org.ovirt.engine.core.bll.utils.VmOverheadCalculatorImpl;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

/** A test case for the {@link CreateSnapshotForVmCommand} class. */
@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateSnapshotForVmCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private CreateSnapshotForVmCommand<CreateSnapshotForVmParameters> cmd =
            new CreateSnapshotForVmCommand<>
        (new CreateSnapshotForVmParameters(Guid.newGuid(), "", false), null);

    @Mock
    private VmValidator vmValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private VM vm;

    @Mock
    private VmStatic vmStatic;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private DiskExistenceValidator diskExistenceValidator;

    @Mock
    private MultipleStorageDomainsValidator multipleStorageDomainsValidator;

    @Mock
    private StoragePoolValidator storagePoolValidator;

    @Mock
    private MemoryImageBuilder memoryImageBuilder;

    @Mock
    private BackendInternal backend;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VDS vds;

    @Spy
    private VgamemVideoSettings vgamemVideoSettings;

    @Spy
    private ClusterUtils clusterUtils;

    @Spy
    @InjectMocks
    private VideoDeviceSettings videoDeviceSettings;

    @Spy
    @InjectMocks
    private VmOverheadCalculatorImpl vmOverheadCalculator;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        doReturn(true).when(vm).isManagedVm();
        doReturn(DisplayType.vga).when(vmStatic).getDefaultDisplayType();
        doReturn(vmStatic).when(vm).getStaticData();
        doReturn(vm).when(cmd).getVm();
        doReturn(vmValidator).when(cmd).createVmValidator();
        doReturn(storagePoolValidator).when(cmd).createStoragePoolValidator();
        doReturn(diskImagesValidator).when(cmd).createDiskImageValidator(any());
        doReturn(diskExistenceValidator).when(cmd).createDiskExistenceValidator(any());
        doReturn(multipleStorageDomainsValidator).when(cmd).createMultipleStorageDomainsValidator(any());
        doReturn(memoryImageBuilder).when(cmd).getMemoryImageBuilder();
        doReturn(true).when(cmd).validateCinder();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainIdForVmMemory(any());
        doReturn(getEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(getEmptyDiskList()).when(cmd).getDiskImagesForVm();
        doReturn(null).when(cmd).getCallback();
        doReturn(null).when(cmd).cloneContextAndDetachFromParent();
        OsRepository osRepositoryMock = mock(OsRepository.class);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepositoryMock);
    }

    @Test
    public void testPositiveValidateWithNoDisks() {
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testVMIsNotValid() {
        when(vmValidator.vmNotSavingRestoring()).thenReturn
            (new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING));
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
    }

    @Test
    public void testStoragePoolIsNotUp() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL)).when(storagePoolValidator)
                .existsAndUp();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testVmDuringSnaoshot() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT)).when(snapshotsValidator)
                .vmNotDuringSnapshot(any());
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT);
    }

    @Test
    public void testVmInPreview() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW)).when(snapshotsValidator)
                .vmNotInPreview(any());
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void testVmDuringMigration() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS)).when(vmValidator)
            .vmNotDuringMigration();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
    }

    @Test
    public void testSaveMemoryPciPassthroughFailure() {
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES))
            .when(vmValidator)
            .vmNotHavingPciPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
            EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES);
    }

    @Test
    public void testNoMemoryPciPassthroughSuccess() {
        cmd.getParameters().setSaveMemory(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES))
            .when(vmValidator)
            .vmNotHavingPciPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testSaveMemoryScsiPassthroughFailure() {
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_SCSI_HOST_DEVICES))
                .when(vmValidator)
                .vmNotHavingScsiPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
            EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_SCSI_HOST_DEVICES);
    }

    @Test
    public void testNoMemoryScsiPassthroughSuccess() {
        cmd.getParameters().setSaveMemory(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_SCSI_HOST_DEVICES))
                .when(vmValidator)
                .vmNotHavingScsiPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testSaveMemoryNvdimmFailure() {
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_NVDIMM_DEVICES))
                .when(vmValidator)
                .vmNotHavingNvdimmDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
            EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_NVDIMM_DEVICES);
    }

    @Test
    public void testNoMemoryNvdimSuccess() {
        cmd.getParameters().setSaveMemory(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_NVDIMM_DEVICES))
                .when(vmValidator)
                .vmNotHavingNvdimmDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testVmUsesMdevTypeHookFailure() {
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_MDEV_TYPE_HOOK)).when(vmValidator)
                .vmNotUsingMdevTypeHook();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_USES_MDEV_TYPE_HOOK);
    }

    @Test
    public void testVmUsesMdevTypeHookSucceess() {
        cmd.getParameters().setSaveMemory(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_MDEV_TYPE_HOOK)).when(vmValidator)
                .vmNotUsingMdevTypeHook();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testVmRunningStateless() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS)).when(vmValidator)
                .vmNotRunningStateless();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS);
    }

    @Test
    public void testLiveSnapshotWhenNoPluggedDiskSnapshot() {
        doReturn(true).when(cmd).isLiveSnapshotApplicable();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testVmIllegal() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL)).when(vmValidator)
                .vmNotIllegal();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL);
    }

    @Test
    public void testVmLocked() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED)).when(vmValidator)
                .vmNotLocked();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED);
    }

    @Test
    public void testPositiveValidateWithDisks() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImagesLocked() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)).when(diskImagesValidator)
                .diskImagesNotLocked();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testImagesIllegal() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).when(diskImagesValidator)
                .diskImagesNotIllegal();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    @Test
    public void testImagesDoesNotExist() {
        Set<Guid> guidsForDiskImages = new HashSet<>(Arrays.asList(Guid.newGuid(), Guid.newGuid()));

        cmd.getParameters().setDiskIds(guidsForDiskImages);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST)).when(diskExistenceValidator).disksNotExist();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST);
    }

    @Test
    public void testAllDomainsExistAndActive() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST)).when(multipleStorageDomainsValidator)
                .allDomainsExistAndActive();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksFailure() {
        List<DiskImage> disksList = Collections.emptyList();
        doReturn(disksList).when(cmd).getDisksList();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).when(multipleStorageDomainsValidator)
                .allDomainsHaveSpaceForNewDisks(disksList);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForNewDisks(disksList);
    }

    @Test
    public void testAllDomainsHaveSpaceForNewDisksSuccess() {
        List<DiskImage> disksList = Collections.emptyList();
        doReturn(disksList).when(cmd).getDisksList();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForNewDisks(disksList);
    }

    @Test
    public void testAllDomainsWithinThreshold() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).when(multipleStorageDomainsValidator)
                .allDomainsExistAndActive();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
            EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksFailure() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).when(multipleStorageDomainsValidator)
                .allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), any());
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), any());
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksSuccess() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        cmd.getParameters().setSaveMemory(true);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), any());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationFreezeAllowInconsistent() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.LiveSnapshotAllowInconsistent, true),
                MockConfigDescriptor.of(ConfigValues.LiveSnapshotPerformFreezeInEngine, Version.getLast(), false));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationDisallowInconsistent() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.LiveSnapshotAllowInconsistent, false));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationFreezeDisallowInconsistent() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.LiveSnapshotAllowInconsistent, false),
                MockConfigDescriptor.of(ConfigValues.LiveSnapshotPerformFreezeInEngine, Version.getLast(), false));
    }

    private void setUpLiveVmWithMbsDisk() {
        doReturn(true).when(vm).isRunning();
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        doReturn(Version.getLast()).when(vm).getClusterCompatibilityVersion();
        doReturn(Guid.newGuid()).when(vds).getId();
        cmd.setVds(vds);
        doReturn(getMbsDiskList()).when(cmd).getDisksList();
        cmd.init();
    }

    private ActionReturnValue actionReturnValue(boolean succeeded) {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(succeeded);
        return returnValue;
    }

    @Test
    @MockedConfig("mockConfigurationFreezeAllowInconsistent")
    public void testFreezeMarksThawOwedBeforeFreezeIsAttempted() {
        setUpLiveVmWithMbsDisk();
        doAnswer(invocation -> {
            assertTrue(cmd.getParameters().isVmNeedsThaw(),
                    "vmNeedsThaw must be set and persisted before the FreezeVm command is attempted");
            return actionReturnValue(true);
        }).when(backend).runInternalAction(eq(ActionType.FreezeVm), any(), any());

        cmd.freezeVm();

        verify(backend).runInternalAction(eq(ActionType.FreezeVm), any(), any());
        assertTrue(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    @MockedConfig("mockConfigurationFreezeAllowInconsistent")
    public void testToleratedFreezeFailureKeepsThawOwed() {
        setUpLiveVmWithMbsDisk();
        when(backend.runInternalAction(eq(ActionType.FreezeVm), any(), any()))
                .thenReturn(actionReturnValue(false));

        cmd.freezeVm();

        assertTrue(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    @MockedConfig("mockConfigurationFreezeDisallowInconsistent")
    public void testFreezeFailureAbortsWhenInconsistentDisallowed() {
        setUpLiveVmWithMbsDisk();
        when(backend.runInternalAction(eq(ActionType.FreezeVm), any(), any()))
                .thenReturn(actionReturnValue(false));

        assertThrows(EngineException.class, () -> cmd.freezeVm());

        assertTrue(cmd.getParameters().isVmNeedsThaw(),
                "a failed freeze may still have frozen the guest, so a thaw must remain owed");
    }

    @Test
    public void testThawIssuedWhenLiveSnapshotFailedToStart() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setLiveSnapshotRequired(true);
        cmd.getParameters().setLiveSnapshotSucceeded(false);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(true));

        cmd.thawVmIfNeeded();

        verify(backend).runInternalAction(eq(ActionType.ThawVm), any(), any());
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testNoThawWhileStartedLiveSnapshotIsRunning() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setTaskGroupSuccess(true);
        cmd.getParameters().setCreatedSnapshotId(Guid.newGuid());
        cmd.getParameters().setCreateSnapshotStage(
                CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED);
        when(snapshotDao.get(any(Guid.class))).thenReturn(new Snapshot());
        doReturn(true).when(cmd).shouldPerformLiveSnapshot(any());
        doReturn(true).when(cmd).performLiveSnapshot(any());

        assertTrue(cmd.performNextOperation(0));

        verify(backend, never()).runInternalAction(eq(ActionType.ThawVm), any(), any());
        assertTrue(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    @MockedConfig("mockConfigurationFreezeAllowInconsistent")
    public void testThawFailureNotAuditedWhenInconsistentAllowed() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(false));

        cmd.thawVmIfNeeded();

        verify(auditLogDirector, never()).log(any(), eq(AuditLogType.FAILED_TO_THAW_VM));
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testThawIssuedWhenLiveSnapshotChildNotExecuted() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setLiveSnapshotRequired(false);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(true));

        cmd.thawVmIfNeeded();

        verify(backend).runInternalAction(eq(ActionType.ThawVm), any(), any());
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testNoThawWhenLiveSnapshotChildSucceeded() {
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setLiveSnapshotRequired(true);
        cmd.getParameters().setLiveSnapshotSucceeded(true);

        cmd.thawVmIfNeeded();

        verify(backend, never()).runInternalAction(eq(ActionType.ThawVm), any(), any());
        assertTrue(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testNoThawWhenFreezeWasNeverAttempted() {
        cmd.thawVmIfNeeded();

        verify(backend, never()).runInternalAction(eq(ActionType.ThawVm), any(), any());
    }

    @Test
    @MockedConfig("mockConfigurationFreezeAllowInconsistent")
    public void testThawSkippedQuietlyWhenVmNoLongerRunning() {
        doReturn(null).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);

        cmd.thawVmIfNeeded();

        verify(backend, never()).runInternalAction(eq(ActionType.ThawVm), any(), any());
        verify(auditLogDirector, never()).log(any(), eq(AuditLogType.FAILED_TO_THAW_VM));
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    @MockedConfig("mockConfigurationDisallowInconsistent")
    public void testThawFailureIsNonFatal() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(false));

        cmd.thawVmIfNeeded();

        verify(auditLogDirector).log(any(), eq(AuditLogType.FAILED_TO_THAW_VM));
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testPerformNextOperationThawsWhenLiveSnapshotNotPerformed() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setCreateSnapshotStage(
                CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(true));

        assertTrue(cmd.performNextOperation(0));

        verify(backend).runInternalAction(eq(ActionType.ThawVm), any(), any());
        assertFalse(cmd.getParameters().isVmNeedsThaw());
    }

    @Test
    public void testPerformNextOperationKeepsThawOwedWhenThawFails() {
        doReturn(Guid.newGuid()).when(vm).getRunOnVds();
        cmd.getParameters().setVmNeedsThaw(true);
        cmd.getParameters().setCreateSnapshotStage(
                CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED);
        when(backend.runInternalAction(eq(ActionType.ThawVm), any(), any()))
                .thenReturn(actionReturnValue(false));

        assertTrue(cmd.performNextOperation(0));

        assertTrue(cmd.getParameters().isVmNeedsThaw());
    }

    private static List<DiskImage> getMbsDiskList() {
        List<DiskImage> diskList = new ArrayList<>();
        diskList.add(new ManagedBlockStorageDisk());
        return diskList;
    }

    private static List<DiskImage> getEmptyDiskList() {
        List<DiskImage> diskList = new ArrayList<>();
        return diskList;
    }

    private static List<DiskImage> getNonEmptyDiskList() {
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage newDiskImage = new DiskImage();
        newDiskImage.setStorageIds(new ArrayList<>());
        diskList.add(newDiskImage);
        return diskList;
    }
}
