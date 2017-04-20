package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
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
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class UpdateVmDiskCommandTest extends BaseCommandTest {

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
    private QuotaDao quotaDao;
    @Mock
    private DiskValidator diskValidator;
    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private DiskVmElementValidator diskVmElementValidator;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private OsRepository osRepository;

    @Mock
    private QuotaManager quotaManager;

    @Mock
    private BackendInternal backend;

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.PassDiscardSupported, Version.v4_0, false));

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private UpdateVmDiskCommand<VmDiskOperationParameterBase> command =
            new UpdateVmDiskCommand<>(createParameters(), CommandContext.createContext(""));

    @Test
    public void validateFailedVMNotFound() throws Exception {
        initializeCommand();
        mockNullVm();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void validateFailedVMHasNotDisk() throws Exception {
        initializeCommand();
        createNullDisk();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST)).
                when(diskValidator).isDiskExists();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateFailedShareableDiskVolumeFormatUnsupported() throws Exception {
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
    public void validateFailedUpdateReadOnly() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        command.getParameters().getDiskVmElement().setReadOnly(true);
        initializeCommand(createVm(VMStatus.Up));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
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
    public void validateFailedShareableDiskOnGlusterDomain() throws Exception {
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
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        command.getParameters().setDiskInfo(disk);

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());

        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        mockInterfaceList();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeVmCommand();
        assertNull(oldDisk.getVmSnapshotId());
    }

    @Test
    public void validateMakeDiskBootableSuccess() {
        validateMakeDiskBootable(false);
    }

    @Test
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

        mockInterfaceList();

        // The command should only succeed if there is no other bootable disk
        assertEquals(!boot, command.validate());
    }

    @Test
    public void validateUpdateWipeAfterDeleteVmDown() {
        validateUpdateWipeAfterDelete(VMStatus.Down);
    }

    @Test
    public void validateUpdateWipeAfterDeleteVmUp() {
        validateUpdateWipeAfterDelete(VMStatus.Up);
    }

    private void validateUpdateWipeAfterDelete(VMStatus status) {
        DiskImage disk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        command.getParameters().getDiskInfo().setWipeAfterDelete(true);
        initializeCommand(createVm(status));

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateUpdateDescriptionVmDown() {
        validateUpdateDescription(VMStatus.Down);
    }

    @Test
    public void validateUpdateDescriptionVmUp() {
        validateUpdateDescription(VMStatus.Up);
    }

    private void validateUpdateDescription(VMStatus status) {
        DiskImage disk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        disk.setDescription(RandomUtils.instance().nextString(10));
        initializeCommand(createVm(status));

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void clearAddressOnInterfaceChange() {
        // update new disk interface so it will be different than the old one
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.VirtIO);

        DiskImage diskFromDb = createDiskImage();
        doReturn(diskFromDb).when(diskDao).get(diskImageGuid);

        initializeCommand();

        DiskVmElement dve = new DiskVmElement(diskImageGuid, vmId);
        dve.setDiskInterface(DiskInterface.IDE);
        doReturn(dve).when(command).getOldDiskVmElement();

        mockVdsCommandSetVolumeDescription();
        assertNotSame(dve.getDiskInterface(), command.getParameters().getDiskVmElement().getDiskInterface());
        command.executeVmCommand();

        // verify that device address was cleared exactly once
        verify(vmDeviceDao).clearDeviceAddress(diskImageGuid);
    }

    private void mockVdsCommandSetVolumeDescription() {
        doNothing().when(command).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testUpdateReadOnlyPropertyOnChange() {
        // Disk should be updated as Read Only
        command.getParameters().getDiskVmElement().setReadOnly(true);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        stubVmDevice(diskImageGuid, vmId);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertTrue(command.updateReadOnlyRequested());
        verify(vmDeviceDao).update(any(VmDevice.class));
    }

    @Test
    public void testUpdateDiskInterfaceUnsupported() {
        command.getParameters().getDiskVmElement().setDiskInterface(DiskInterface.IDE);

        initializeCommand();
        mockVdsCommandSetVolumeDescription();

        DiskVmElement dve = new DiskVmElement(diskImageGuid, vmId);
        dve.setDiskInterface(DiskInterface.VirtIO);
        doReturn(dve).when(command).getOldDiskVmElement();
        doReturn(createDiskImage()).when(command).getOldDisk();
        stubVmDevice(diskImageGuid, vmId);

        when(diskVmElementValidator.isDiskInterfaceSupported(any(VM.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(command.getParameters().getDiskInfo())).thenReturn(diskValidator);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void testDoNotUpdateDeviceWhenReadOnlyIsNotChanged() {
        command.getParameters().getDiskVmElement().setReadOnly(false);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertFalse(command.updateReadOnlyRequested());
        verify(vmDeviceDao, never()).update(any(VmDevice.class));
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
    public void testSucceedInterfaceCanUpdateReadOnly() {
        initializeCommand();
        doReturn(true).when(command).updateReadOnlyRequested();

        assertTrue(command.validateCanUpdateReadOnly());
    }

    @Test
    public void testUpdateOvfDiskNotSupported() {
        DiskImage updatedDisk = createDiskImage();
        updatedDisk.setDiskAlias("Iron");

        DiskImage diskFromDB = createDiskImage();
        diskFromDB.setDiskAlias("Maiden");
        diskFromDB.setContentType(DiskContentType.OVF_STORE);

        when(diskDao.get(diskImageGuid)).thenReturn(diskFromDB);

        initializeCommand();

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED)).
                when(diskValidator).isDiskUsedAsOvfStore();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void testResize() {
        DiskImage oldDisk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        ((DiskImage) command.getParameters().getDiskInfo()).setSize(oldDisk.getSize() * 2L);
        initializeCommand();

        assertTrue(command.validateCanResizeDisk());
    }

    @Test
    public void testAmend() {
        DiskImage oldDisk = createDiskImage();
        oldDisk.setVolumeFormat(VolumeFormat.COW);
        oldDisk.setQcowCompat(QcowCompat.QCOW2_V2);
        oldDisk.setDiskAlias("Test");
        oldDisk.setDiskDescription("Test_Desc");
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        newDisk.setQcowCompat(QcowCompat.QCOW2_V3);
        command.getParameters().setDiskInfo(newDisk);
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).amendDiskImage();
    }

    @Test
    public void testAmendWithPropertyChange() {
        DiskImage oldDisk = createDiskImage();
        oldDisk.setVolumeFormat(VolumeFormat.COW);
        oldDisk.setQcowCompat(QcowCompat.QCOW2_V2);
        oldDisk.setDiskAlias("Test");
        oldDisk.setDiskDescription("Test_Desc");
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        newDisk.setQcowCompat(QcowCompat.QCOW2_V3);
        newDisk.setDiskAlias("New Disk Alias");
        command.getParameters().setDiskInfo(newDisk);
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).amendDiskImage();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testAmendFailedWithPropertyChange() {
        DiskImage oldDisk = createDiskImage();
        oldDisk.setVolumeFormat(VolumeFormat.COW);
        oldDisk.setQcowCompat(QcowCompat.QCOW2_V2);
        oldDisk.setDiskAlias("Test");
        oldDisk.setDiskDescription("Test_Desc");
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        DiskImage newDisk = DiskImage.copyOf(oldDisk);
        newDisk.setQcowCompat(QcowCompat.QCOW2_V3);
        newDisk.setDiskAlias("New Disk Alias");
        command.getParameters().setDiskInfo(newDisk);
        initializeCommand();
        VdcReturnValueBase ret = new VdcReturnValueBase();
        ret.setSucceeded(false);
        ArrayList<String> msgList = new ArrayList<>();
        msgList.add(EngineMessage.ACTION_TYPE_FAILED_AMEND_NOT_SUPPORTED_BY_DC_VERSION.toString());
        ret.setValidationMessages(msgList);
        when(backend.runInternalAction(eq(VdcActionType.AmendImageGroupVolumes), any(StorageDomainParametersBase.class), any(CommandContext.class))).thenReturn(ret);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).amendDiskImage();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testAmendNotRunningWithExtend() {
        Guid quotaId = Guid.newGuid();

        DiskImage oldDiskImage = createDiskImage();
        oldDiskImage.setQuotaId(quotaId);
        oldDiskImage.setSize(SizeConverter.convert(3L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());
        oldDiskImage.setVolumeFormat(VolumeFormat.COW);
        oldDiskImage.setQcowCompat(QcowCompat.QCOW2_V2);


        DiskImage newDiskImage = createDiskImage();
        newDiskImage.setQuotaId(quotaId);
        newDiskImage.setSize(SizeConverter.convert(5L, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());
        newDiskImage.setVolumeFormat(VolumeFormat.COW);
        newDiskImage.setQcowCompat(QcowCompat.QCOW2_V3);

        command.getParameters().setDiskVmElement(new DiskVmElement(newDiskImage.getId(), vmId));
        command.getParameters().setDiskInfo(newDiskImage);

        when(diskDao.get(diskImageGuid)).thenReturn(oldDiskImage);
        initializeCommand();
        assertTrue(command.amendDiskRequested());
        verify(command, times(0)).amendDiskImage();
    }

    @Test
    public void testFaultyResize() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        ((DiskImage) command.getParameters().getDiskInfo()).setSize(command.getParameters().getDiskInfo().getSize() / 2L);
        initializeCommand();

        assertFalse(command.validateCanResizeDisk());
        ValidateTestUtils.assertValidationMessages
                ("wrong failure", command, EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
    }

    @Test
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
                .when(quotaManager).getDefaultQuotaIfNull(any(Guid.class), any(Guid.class));

        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));
        doReturn(diskVmElementValidator).when(command).getDiskVmElementValidator(any(Disk.class), any(DiskVmElement.class));
        doReturn(true).when(command).setAndValidateDiskProfiles();

        doReturn(true).when(command).validateQuota();

        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        mockVmsStoragePoolInfo(vm);
        mockToUpdateDiskVm(vm);

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        doReturn(sdValidator).when(command).getStorageDomainValidator(any(DiskImage.class));
        VdcReturnValueBase ret = new VdcReturnValueBase();
        ret.setSucceeded(true);
        when(backend.runInternalAction(eq(VdcActionType.AmendImageGroupVolumes), any(StorageDomainParametersBase.class), any(CommandContext.class))).thenReturn(ret);
        command.init();
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldNotBeUpdated() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
    }

    @Test
    public void testUpdateLockedDisk() {
        DiskImage disk = createDiskImage();
        disk.setImageStatus(ImageStatus.LOCKED);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);

        initializeCommand();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldBeUpdated() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        command.getParameters().getDiskInfo().setDiskAlias("New Disk Alias");
        command.getParameters().getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testOnlyDiskAliasChangedMetaDataShouldBeUpdated() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        command.getParameters().getDiskInfo().setDiskAlias("New Disk Alias");
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testOnlyDescriptionsChangedMetaDataShouldBeUpdated() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        command.getParameters().getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand();
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
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
    public void testExistingQuota() {
        Quota quota = new Quota();
        quota.setId(Guid.newGuid());

        when(quotaDao.getById(any(Guid.class))).thenReturn(null);
        when(quotaDao.getById(quota.getId())).thenReturn(quota);

        when(diskDao.get(any(Guid.class))).thenReturn(createDiskImage());

        ((DiskImage) command.getParameters().getDiskInfo()).setQuotaId(quota.getId());
        initializeCommand();

        StoragePool pool = mockStoragePool();
        command.setStoragePoolId(pool.getId());
        quota.setStoragePoolId(pool.getId());

        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testNonExistingQuota() {
        when(quotaDao.getById(any(Guid.class))).thenReturn(null);

        when(diskDao.get(any(Guid.class))).thenReturn(createDiskImage());

        ((DiskImage) command.getParameters().getDiskInfo()).setQuotaId(Guid.newGuid());
        initializeCommand();

        doCallRealMethod().when(command).validateQuota();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
    }

    private void mockToUpdateDiskVm(VM vm) {
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        when(diskVmElementDao.get(new VmDeviceId(command.getParameters().getDiskInfo().getId(), vm.getId()))).thenReturn(new DiskVmElement());
    }

    @Test
    public void validateDiscardFailedNotSupportedByDiskInterface() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        when(diskVmElementValidator.isPassDiscardSupported(any(Guid.class))).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE);
    }

    @Test
    public void validateDiscardFailedNotSupportedByDcVersion() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();

        command.getParameters().getDiskVmElement().setPassDiscard(true);

        DiskVmElement oldDiskVmElement = new DiskVmElement();
        oldDiskVmElement.setPassDiscard(false);
        doReturn(oldDiskVmElement).when(command).getOldDiskVmElement();
        command.getStoragePool().setCompatibilityVersion(Version.v4_0);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DC_VERSION);
    }

    @Test
    public void validateDiscardSucceeded() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void mockNullVm() {
        mockGetForDisk(null);
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(null);
    }

    protected void mockInterfaceList() {
        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn
                (Arrays.asList("IDE", "VirtIO", "VirtIO_SCSI"));
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
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);

        return storagePool;
    }

    /**
     * @return Valid parameters for the command.
     */
    protected VmDiskOperationParameterBase createParameters() {
        DiskImage diskInfo = createDiskImage();
        return new VmDiskOperationParameterBase(new DiskVmElement(diskInfo.getId(), vmId), diskInfo);
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
}
