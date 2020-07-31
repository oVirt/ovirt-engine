package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith({MockConfigExtension.class, RandomUtilsSeedingExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateDiskCommandTest extends BaseCommandTest {

    private Guid diskImageGuid = Guid.newGuid();
    private Guid vmId = Guid.newGuid();
    private Guid sdId = Guid.newGuid();
    private Guid spId = Guid.newGuid();

    @Mock
    private VmDao vmDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmStaticDao vmStaticDao;
    @Mock
    private BaseDiskDao baseDiskDao;
    @Mock
    private ImageDao imageDao;
    @Mock
    private DiskImageDao diskImageDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private QuotaValidator quotaValidator;
    @Mock
    private DiskValidator diskValidator;
    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private QuotaManager quotaManager;

    @Mock
    private BackendInternal backend;

    @Mock
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    @Mock
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxBlockDiskSizeInGibiBytes, 8),
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private UpdateDiskCommand<UpdateDiskParameters> command =
            new UpdateDiskCommand<>(createParameters(), CommandContext.createContext(""));

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedVMHasNotDisk() {
        initializeCommand();
        createNullDisk();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST)).
                when(diskValidator).isDiskExists();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedShareableDiskVolumeFormatUnsupported() {
        DiskImage disk = createShareableDisk(VolumeFormat.COW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        command.getParameters().setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedUpdateShareableForRunningVm() {
        updateShareable();
        initializeCommand(createVm(VMStatus.Up));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateShareableFloatingDisk() {
        updateShareable();

        initializeCommand();
        command.getParameters().setDiskVmElement(null);
        mockNullVm();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void updateShareable() {
        DiskImage disk = createShareableDisk(VolumeFormat.COW);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        command.getParameters().getDiskInfo().setShareable(false);
    }

    @Test
    public void validateFailedUpdateReadOnly() {
        updateReadOnly(false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void validateFailedUpdateReadOnlyForFloatingDisk() {
        updateReadOnly(true);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    private void updateReadOnly(boolean isFloating) {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setReadOnly(true);

        if (isFloating) {
            initializeCommand();
            mockNullVm();
        } else {
            initializeCommand(createVm(VMStatus.Up));
        }
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedUpdatePassDiscardForFloatingDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setPassDiscard(true);

        initializeCommand();
        mockNullVm();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedROVmAttachedToPool() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setReadOnly(true);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(vm);

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId); // Default RO is false
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        vmDevice.setReadOnly(true);
        command.getParameters().getDiskVmElement().setReadOnly(false);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedWipeVmAttachedToPool() {
        Disk oldDisk = createDiskImage();
        oldDisk.setWipeAfterDelete(true);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        command.getParameters().getDiskInfo().setWipeAfterDelete(false);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(vm);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        oldDisk.setWipeAfterDelete(false);
        command.getParameters().getDiskInfo().setWipeAfterDelete(true);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedShareableDiskOnGlusterDomain() {
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.GLUSTERFS);
        command.getParameters().setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedBackupEnabledForRawDisk() {
        DiskImage disk = createDiskImage();
        disk.setBackup(DiskBackup.Incremental);
        command.getParameters().setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_NOT_SUPPORTED_FOR_RAW_DISK);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        command.getParameters().setDiskInfo(disk);

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());

        doNothing().when(command).lockImageInDb();
        doNothing().when(command).unlockImageInDb();

        initializeCommand();
        mockVdsCommandSetVolumeDescription();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeVmCommand();
        command.performNextOperation(0);
        assertNull(oldDisk.getVmSnapshotId());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateMakeDiskBootableSuccess() {
        validateMakeDiskBootable(false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateMakeDiskBootableFail() {
        validateMakeDiskBootable(true);
    }

    private void validateMakeDiskBootable(boolean boot) {
        command.getParameters().getDiskVmElement().setBoot(true);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        when(diskValidator.isVmNotContainsBootDisk(createVm(VMStatus.Down))).
                thenReturn(boot ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE) : ValidationResult.VALID);
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand();

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.validate());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedUpdateBootableForFloatingDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setBoot(true);

        initializeCommand();
        mockNullVm();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateWipeAfterDeleteVmDown() {
        validateUpdateWipeAfterDelete(VMStatus.Down, false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateWipeAfterDeleteVmUp() {
        validateUpdateWipeAfterDelete(VMStatus.Up, false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateWipeAfterDeleteForFloatingDisk() {
        validateUpdateWipeAfterDelete(null, true);
    }

    private void validateUpdateWipeAfterDelete(VMStatus status, boolean isFloating) {
        DiskImage disk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        command.getParameters().getDiskInfo().setWipeAfterDelete(true);

        initCommandForDisk(status, isFloating);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateDescriptionVmDown() {
        validateUpdateDescription(VMStatus.Down, false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateDescriptionVmUp() {
        validateUpdateDescription(VMStatus.Up, false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateUpdateDescriptionForFloatingDisk() {
        validateUpdateDescription(null, true);
    }

    private void validateUpdateDescription(VMStatus status, boolean isFloating) {
        DiskImage disk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        String desc = "new-description";
        disk.setDescription(desc);

        initCommandForDisk(status, isFloating);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
        assertEquals(desc, disk.getDescription());
    }

    private void mockVdsCommandSetVolumeDescription() {
        doNothing().when(command).setVolumeDescription(any(), any());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testUpdateDiskInterfaceUnsupported() {
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.IDE);

        initializeCommand();
        mockVdsCommandSetVolumeDescription();

        DiskVmElement dve = new DiskVmElement(diskImageGuid, vmId);
        dve.setDiskInterface(DiskInterface.VirtIO);
        doReturn(dve).when(command).getOldDiskVmElement();
        doReturn(createDiskImage()).when(command).getOldDisk();
        stubVmDevice(diskImageGuid, vmId);

        when(diskVmElementValidator.isDiskInterfaceSupported(any())).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(command.getParameters().getDiskInfo())).thenReturn(diskValidator);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void testFailInterfaceCanUpdateReadOnly() {
        initializeCommand();
        doReturn(true).when(command).updateReadOnlyRequested();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskVmElementValidator).isReadOnlyPropertyCompatibleWithInterface();

        assertFalse(command.validateCanUpdateReadOnly());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testSucceedInterfaceCanUpdateReadOnly() {
        initializeCommand();
        doReturn(true).when(command).updateReadOnlyRequested();

        assertTrue(command.validateCanUpdateReadOnly());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateFailedUpdateInterfaceForFloatingDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO_SCSI);

        initializeCommand();
        mockNullVm();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testSucceedResizeDisk() {
        resizeDisk(false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testSucceedResizeFloatingDisk() {
        resizeDisk(true);
    }

    private void resizeDisk(boolean isFloating) {
        DiskImage oldDisk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        ((DiskImage) command.getParameters().getDiskInfo()).setSize(oldDisk.getSize() * 2L);

        initCommandForDisk(VMStatus.Down, isFloating);

        assertTrue(command.validateCanResizeDisk());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testAmendAndUpdate() {
        DiskImage oldDisk = createDiskImage();
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        updateGeneralFields(oldDisk, newDisk);
        updateAmendFields(oldDisk, newDisk);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        command.getParameters().setDiskInfo(newDisk);
        doNothing().when(command).lockImageInDb();
        initializeCommand();
        command.executeVmCommand();
        List<UpdateDiskParameters.Phase> phaseList = command.getParameters().getDiskUpdatePhases();
        UpdateDiskParameters.Phase phase = phaseList.get(phaseList.size()-1);
        assertEquals(UpdateDiskParameters.Phase.UPDATE_DISK, phase);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testAmendExtendAndUpdate() {
        DiskImage oldDisk = createDiskImage();
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        updateGeneralFields(oldDisk, newDisk);
        updateAmendFields(oldDisk, newDisk);
        updateExtendFields(oldDisk, newDisk);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        command.getParameters().setDiskInfo(newDisk);
        doNothing().when(command).lockImageInDb();
        initializeCommand();
        command.executeVmCommand();
        List<UpdateDiskParameters.Phase> phaseList = command.getParameters().getDiskUpdatePhases();
        UpdateDiskParameters.Phase phase = phaseList.get(phaseList.size()-1);
        assertEquals(UpdateDiskParameters.Phase.UPDATE_DISK, phase);
    }

    private void updateGeneralFields(DiskImage oldDisk, DiskImage newDisk) {
        oldDisk.setDiskAlias("Test");
        oldDisk.setDiskDescription("Test_Desc");
        newDisk.setDiskAlias("New Disk Alias");
        oldDisk.setDiskDescription("New Test_Desc");
    }

    private void updateAmendFields(DiskImage oldDisk, DiskImage newDisk) {
        oldDisk.setVolumeFormat(VolumeFormat.COW);
        oldDisk.setQcowCompat(QcowCompat.QCOW2_V2);
        newDisk.setVolumeFormat(VolumeFormat.COW);
        newDisk.setQcowCompat(QcowCompat.QCOW2_V3);
    }

    private void updateExtendFields(DiskImage oldDisk, DiskImage newDisk) {
        oldDisk.setSize(SizeConverter.convert(3L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());
        newDisk.setSize(SizeConverter.convert(5L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());
    }

    private DiskImage setFormatAndSizeForDisk(Guid quotaId, long lsize, QcowCompat qcowFormat) {
        DiskImage diskImage = createDiskImage();
        diskImage.setQuotaId(quotaId);
        diskImage.setSize(SizeConverter.convert(lsize, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());
        diskImage.setVolumeFormat(VolumeFormat.COW);
        diskImage.setQcowCompat(qcowFormat);

        return diskImage;
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testFailedAmendWithNoQcowVolumes() {
        // Creating a RAW disk
        DiskImage oldDisk = createDiskImage();
        oldDisk.setQcowCompat(QcowCompat.QCOW2_V2);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        newDisk.setQcowCompat(QcowCompat.QCOW2_V3);
        command.getParameters().setDiskInfo(newDisk);
        mockGetAllSnapshotsForDisk(Collections.singletonList(oldDisk));
        initializeCommand();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_RAW_DISK);
        verify(command, never()).amendDiskImage();
    }

    private void mockGetAllSnapshotsForDisk(List<DiskImage> images) {
        when(diskImageDao.getAllSnapshotsForImageGroup(any())).thenReturn(images);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testFaultyResize() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        ((DiskImage) command.getParameters().getDiskInfo()).setSize(command.getParameters().getDiskInfo().getSize() / 2L);
        initializeCommand();

        assertFalse(command.validateCanResizeDisk());
        ValidateTestUtils.assertValidationMessages
                ("wrong failure", command, EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testFailedRoDiskResize() {
        ((DiskImage) command.getParameters().getDiskInfo()).setSize(command.getParameters().getDiskInfo().getSize() * 2L);
        initializeCommand();

        DiskImage oldDisk = createDiskImage();
        doReturn(oldDisk).when(command).getOldDisk();

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId);
        vmDevice.setReadOnly(true);

        assertFalse(command.validateCanResizeDisk());
        ValidateTestUtils.assertValidationMessages
                ("wrong failure", command, EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
    }

    private void initializeCommand() {
        initializeCommand(createVmStatusDown());
    }

    protected void initializeCommand(VM vm) {
        mockGetForDisk(vm);
        mockGetVmsListForDisk(vm);
        doNothing().when(command).reloadDisks();

        doAnswer(invocation -> invocation.getArguments()[0] != null ?
                    invocation.getArguments()[0] : Guid.newGuid())
                .when(quotaManager).getFirstQuotaForUser(any(), any(), any());

        doReturn(diskValidator).when(command).getDiskValidator(any());
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(), any());
        doReturn(true).when(command).setAndValidateDiskProfiles();

        doReturn(true).when(command).validateQuota();

        mockVmsStoragePoolInfo(vm);
        mockToUpdateDiskVm(vm);

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.get(any())).thenReturn(sd);
        when(storageDomainDao.getForStoragePool(any(), any())).thenReturn(sd);
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        doReturn(sdValidator).when(command).getStorageDomainValidator(any());
        ActionReturnValue ret = new ActionReturnValue();
        ret.setSucceeded(true);
        when(backend.runInternalAction(eq(ActionType.AmendImageGroupVolumes), any(), any())).thenReturn(ret);
        command.init();
        doReturn(ActionType.UpdateDisk).when(command).getActionType();

        doReturn(new SerialChildCommandsExecutionCallback()).when(callbackProvider).get();
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldNotBeUpdated() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        doNothing().when(command).lockImageInDb();

        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
    }

    @Test
    public void testUpdateLockedDisk() {
        updateLockedDisk(false);
    }

    @Test
    public void testUpdateLockedFloatingDisk() {
        updateLockedDisk(true);
    }

    private void updateLockedDisk(boolean isFloating) {
        DiskImage disk = createDiskImage();
        disk.setImageStatus(ImageStatus.LOCKED);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);

        if (isFloating) {
            initializeCommand();
            mockNullVm();
        } else {
            initializeCommand();
        }

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testExtendingDiskWithQuota() {
        Guid quotaId = Guid.newGuid();

        DiskImage oldDiskImage = createDiskImage();
        oldDiskImage.setQuotaId(quotaId);
        oldDiskImage.setSize(SizeConverter.convert(3L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        DiskImage newDiskImage = createDiskImage();
        newDiskImage.setQuotaId(quotaId);
        newDiskImage.setSize(SizeConverter.convert(5L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        command.getParameters().setDiskVmElement(new DiskVmElement(newDiskImage.getId(), vmId));
        command.getParameters().setDiskInfo(newDiskImage);
        long diskExtendingDiffInGB = newDiskImage.getSizeInGigabytes() - oldDiskImage.getSizeInGigabytes();

        when(diskDao.get(diskImageGuid)).thenReturn(oldDiskImage);
        initializeCommand();

        QuotaStorageConsumptionParameter consumptionParameter =
                (QuotaStorageConsumptionParameter) command.getQuotaStorageConsumptionParameters().get(0);
        assertEquals(consumptionParameter.getRequestedStorageGB().longValue(), diskExtendingDiffInGB);
    }

    @Test
    public void testValidateQuota() {
        when(diskDao.get(any())).thenReturn(createDiskImage());

        Guid quotaId = Guid.newGuid();
        ((DiskImage) command.getParameters().getDiskInfo()).setQuotaId(quotaId);
        initializeCommand();

        StoragePool pool = mockStoragePool();
        command.setStoragePoolId(pool.getId());

        doReturn(ValidationResult.VALID).when(quotaValidator).isValid();
        doReturn(ValidationResult.VALID).when(quotaValidator).isDefinedForStoragePool(any(Guid.class));
        doReturn(quotaValidator).when(command).createQuotaValidator(any(Guid.class));
        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateSuccess(command);

        verify(command, times(1)).createQuotaValidator(quotaId);
        verify(quotaValidator, times(1)).isValid();
        verify(quotaValidator, times(1)).isDefinedForStoragePool(pool.getId());
    }

    private void mockToUpdateDiskVm(VM vm) {
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        when(diskVmElementDao.get(new VmDeviceId(command.getParameters().getDiskInfo().getId(), vm.getId()))).thenReturn(new DiskVmElement());
    }

    @Test
    public void validateDiscardFailedNotSupportedByDiskInterface() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        when(diskVmElementValidator.isPassDiscardSupported(any())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
    }

    @Test
    public void validateDiscardSucceeded() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testUpdateMemoryDiskFails() {
        DiskImage diskFromDB = createDiskImage();
        diskFromDB.setContentType(DiskContentType.MEMORY_DUMP_VOLUME);
        when(diskDao.get(diskImageGuid)).thenReturn(diskFromDB);

        initializeCommand();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_CONTENT_TYPE_NOT_SUPPORTED_FOR_OPERATION);
    }

    @Test
    public void testInvalidDiskExtend() {
        invalidDiskExtend(false);
    }

    @Test
    public void testInvalidFloatingDiskExtend() {
        invalidDiskExtend(true);
    }

    private void invalidDiskExtend(boolean isFloating) {
        DiskImage oldDiskImage = createDiskImage();
        oldDiskImage.setSize(SizeConverter.convert(8L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        DiskImage newDiskImage = createDiskImage();
        newDiskImage.setSize(SizeConverter.convert(10L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        command.getParameters().setDiskInfo(newDiskImage);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDiskImage);

        initCommandForDisk(VMStatus.Down, isFloating);

        if (!isFloating) {
            command.getParameters().setDiskVmElement(new DiskVmElement(newDiskImage.getId(), vmId));
        }

        StorageDomain sd = new StorageDomain();
        sd.setId(Guid.newGuid());
        sd.setStorageType(StorageType.ISCSI);
        when(storageDomainDao.get(any())).thenReturn(sd);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
    }

    private void mockNullVm() {
        mockGetForDisk(null);
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(null);
    }

    protected VM createVmStatusDown() {
        return createVm(VMStatus.Down);
    }

    protected VM createVm(VMStatus status) {
        VM vm = new VM();
        vm.setStatus(status);
        vm.setGuestOs("rhel6");
        vm.setId(vmId);
        return vm;
    }

    private void mockVmsStoragePoolInfo(VM vm) {
        StoragePool storagePool = mockStoragePool();
        vm.setStoragePoolId(storagePool.getId());
    }

    private void mockGetForDisk(VM vm) {
        when(vmDao.getForDisk(diskImageGuid, true)).thenReturn(
                Collections.singletonMap(Boolean.TRUE, Collections.singletonList(vm)));
    }

    private void mockGetVmsListForDisk(VM vm) {
        VmDevice device = createVmDevice(diskImageGuid, vm.getId());
        when(vmDao.getVmsWithPlugInfo(diskImageGuid)).thenReturn(Collections.singletonList(new Pair<>(vm, device)));
    }

    /**
     * Mock a {@link StoragePool}.
     */
    private StoragePool mockStoragePool() {
        Guid storagePoolId = Guid.newGuid();
        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.v4_3);
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * @return Valid parameters for the command.
     */
    protected UpdateDiskParameters createParameters() {
        DiskImage diskInfo = createDiskImage();
        return new UpdateDiskParameters(new DiskVmElement(diskInfo.getId(), vmId), diskInfo);
    }

    /**
     * The following method will simulate a situation when disk was not found in DB
     */
    private void createNullDisk() {
        when(diskDao.get(diskImageGuid)).thenReturn(null);
    }

    /**
     * The following method will create a new DiskImage
     */
    private DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(diskImageGuid);
        disk.setSize(100000L);
        disk.setStorageIds(new ArrayList<>(Collections.singleton(sdId)));
        disk.setStoragePoolId(spId);
        disk.setVolumeFormat(VolumeFormat.RAW);
        disk.setDescription(RandomUtils.instance().nextString(10));
        return disk;
    }

    /**
     * The following method will create a Shareable DiskImage with a specified format
     */
    private DiskImage createShareableDisk(VolumeFormat volumeFormat) {
        DiskImage disk = createDiskImage();
        disk.setVolumeFormat(volumeFormat);
        disk.setShareable(true);
        return disk;
    }

    private StorageDomain addNewStorageDomainToDisk(DiskImage diskImage, StorageType storageType) {
        StorageDomain storage = new StorageDomain();
        storage.setId(Guid.newGuid());
        storage.setStorageType(storageType);
        storage.setStatus(StorageDomainStatus.Active);
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storage.getId())));
        return storage;
    }

    private VmDevice createVmDevice(Guid diskId, Guid vmId) {
        return new VmDevice(new VmDeviceId(diskId, vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                true,
                null,
                "",
                null,
                null,
                null);
    }

    private VmDevice stubVmDevice(Guid diskId, Guid vmId) {
        VmDevice vmDevice = createVmDevice(diskId, vmId);
        doReturn(vmDevice).when(command).getVmDeviceForVm();
        return vmDevice;
    }

    private void initCommandForDisk(VMStatus status, boolean isFloating) {
        if (isFloating) {
            initializeCommand();
            command.getParameters().setDiskVmElement(null);
            mockNullVm();
        } else {
            initializeCommand(createVm(status));
        }
    }
}
