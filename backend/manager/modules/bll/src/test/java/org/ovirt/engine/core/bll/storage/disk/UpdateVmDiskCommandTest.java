package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
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
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
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
    private VdsDao vdsDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private VmStaticDao vmStaticDao;
    @Mock
    private BaseDiskDao baseDiskDao;
    @Mock
    private ImageDao imageDao;
    @Mock
    private SnapshotDao snapshotDao;
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
    private DbFacade dbFacade;
    @Mock
    private DiskValidator diskValidator;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private OsRepository osRepository;

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    /**
     * The command under test.
     */
    private UpdateVmDiskCommand<VmDiskOperationParameterBase> command;

    @Test
    public void validateFailedVMNotFound() throws Exception {
        initializeCommand(createParameters());
        mockNullVm();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void validateFailedVMHasNotDisk() throws Exception {
        initializeCommand(createParameters());
        createNullDisk();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST)).
                when(diskValidator).isDiskExists();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateFailedShareableDiskVolumeFormatUnsupported() throws Exception {
        VmDiskOperationParameterBase parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.COW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        parameters.setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand(parameters);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
    }

    @Test
    public void validateFailedUpdateReadOnly() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);
        initializeCommand(parameters, Collections.singletonList(createVm(VMStatus.Up)));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void validateFailedROVmAttachedToPool() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(parameters, Collections.singletonList(vm));

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId); // Default RO is false
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        vmDevice.setIsReadOnly(true);
        parameters.getDiskInfo().setReadOnly(false);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    public void validateFailedWipeVmAttachedToPool() {
        Disk oldDisk = createDiskImage();
        oldDisk.setWipeAfterDelete(true);
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setWipeAfterDelete(false);
        VM vm = createVm(VMStatus.Down);
        vm.setVmPoolId(Guid.newGuid());
        initializeCommand(parameters, Collections.singletonList(vm));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);

        oldDisk.setWipeAfterDelete(false);
        parameters.getDiskInfo().setWipeAfterDelete(true);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
    }

    @Test
    public void validateFailedShareableDiskOnGlusterDomain() throws Exception {
        VmDiskOperationParameterBase parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.GLUSTERFS);
        parameters.setDiskInfo(disk);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());
        initializeCommand(parameters);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
    }


    @Test
    public void nullifiedSnapshotOnUpdateDiskToShareable() {
        VmDiskOperationParameterBase parameters = createParameters();
        DiskImage disk = createShareableDisk(VolumeFormat.RAW);
        parameters.setDiskInfo(disk);
        StorageDomain storage = addNewStorageDomainToDisk(disk, StorageType.NFS);
        parameters.setDiskInfo(disk);

        DiskImage oldDisk = createDiskImage();
        oldDisk.setVmSnapshotId(Guid.newGuid());

        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);
        when(storageDomainStaticDao.get(storage.getId())).thenReturn(storage.getStorageStaticData());

        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        mockInterfaceList();

        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeVmCommand();
        assertTrue(oldDisk.getVmSnapshotId() == null);
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
        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskVmElement().setBoot(true);

        DiskImage otherDisk = new DiskImage();
        otherDisk.setId(Guid.newGuid());
        otherDisk.setActive(true);
        when(diskValidator.isVmNotContainsBootDisk(createVm(VMStatus.Down))).
                thenReturn(boot ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE) : ValidationResult.VALID);
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand(parameters);

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
        disk.setReadOnly(false);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);
        parameters.getDiskInfo().setWipeAfterDelete(true);
        initializeCommand(parameters, Collections.singletonList(createVm(status)));

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
        disk.setReadOnly(false);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);
        VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);
        disk.setDescription(RandomUtils.instance().nextString(10));
        initializeCommand(parameters, Collections.singletonList(createVm(status)));

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void clearAddressOnInterfaceChange() {
        final VmDiskOperationParameterBase parameters = createParameters();
        // update new disk interface so it will be different than the old one
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.VirtIO);

        DiskImage diskFromDb = createDiskImage();
        doReturn(diskFromDb).when(diskDao).get(diskImageGuid);

        initializeCommand(parameters);

        DiskVmElement dve = new DiskVmElement(diskImageGuid, vmId);
        dve.setDiskInterface(DiskInterface.IDE);
        doReturn(dve).when(command).getOldDiskVmElement();

        mockVdsCommandSetVolumeDescription();
        assertNotSame(dve.getDiskInterface(), parameters.getDiskVmElement().getDiskInterface());
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
        final VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(true);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);
        stubVmDevice(diskImageGuid, vmId);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertTrue(command.updateReadOnlyRequested());
        verify(vmDeviceDao).update(any(VmDevice.class));
    }

    @Test
    public void testUpdateDiskInterfaceUnsupported() {
        final VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskVmElement().setDiskInterface(DiskInterface.IDE);

        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();

        DiskVmElement dve = new DiskVmElement(diskImageGuid, vmId);
        dve.setDiskInterface(DiskInterface.VirtIO);
        doReturn(dve).when(command).getOldDiskVmElement();
        doReturn(createDiskImage()).when(command).getOldDisk();

        when(diskValidator.isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskInterfaceSupported(any(VM.class), any(DiskVmElement.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
        when(command.getDiskValidator(parameters.getDiskInfo())).thenReturn(diskValidator);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED);
    }

    @Test
    public void testDoNotUpdateDeviceWhenReadOnlyIsNotChanged() {
        final VmDiskOperationParameterBase parameters = createParameters();
        parameters.getDiskInfo().setReadOnly(false);

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();

        verify(command, atLeast(1)).updateReadOnlyRequested();
        assertFalse(command.updateReadOnlyRequested());
        verify(vmDeviceDao, never()).update(any(VmDevice.class));
    }

    @Test
    public void testFailInterfaceCanUpdateReadOnly() {
        initializeCommand(new VmDiskOperationParameterBase(new DiskVmElement(diskImageGuid, vmId), createDiskImage()));
        doReturn(true).when(command).updateReadOnlyRequested();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                when(diskValidator).isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class));

        assertFalse(command.validateCanUpdateReadOnly(diskValidator));
    }

    @Test
    public void testSucceedInterfaceCanUpdateReadOnly() {
        initializeCommand(new VmDiskOperationParameterBase(new DiskVmElement(diskImageGuid, vmId), createDiskImage()));
        doReturn(true).when(command).updateReadOnlyRequested();
        doReturn(ValidationResult.VALID).when(diskValidator).isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class));

        assertTrue(command.validateCanUpdateReadOnly(diskValidator));
    }

    @Test
    public void testUpdateOvfDiskNotSupported() {
        DiskImage updatedDisk = createDiskImage();
        updatedDisk.setReadOnly(true);

        DiskImage diskFromDB = createDiskImage();
        diskFromDB.setReadOnly(false);
        diskFromDB.setContentType(DiskContentType.OVF_STORE);

        when(diskDao.get(diskImageGuid)).thenReturn(diskFromDB);

        initializeCommand(new VmDiskOperationParameterBase(new DiskVmElement(diskImageGuid, vmId), updatedDisk));

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED)).
                when(diskValidator).isDiskUsedAsOvfStore();

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void testResize() {
        DiskImage oldDisk = createDiskImage();
        when(diskDao.get(diskImageGuid)).thenReturn(oldDisk);

        VmDiskOperationParameterBase parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(oldDisk.getSize() * 2);
        initializeCommand(parameters);

        assertTrue(command.validateCanResizeDisk());
    }

    @Test
    public void testFaultyResize() {
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        VmDiskOperationParameterBase parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(parameters.getDiskInfo().getSize() / 2);
        initializeCommand(parameters);

        assertFalse(command.validateCanResizeDisk());
        ValidateTestUtils.assertValidationMessages
                ("wrong failure", command, EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
    }

    @Test
    public void testFailedRoDiskResize() {
        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);

        VmDiskOperationParameterBase parameters = createParameters();
        ((DiskImage) parameters.getDiskInfo()).setSize(parameters.getDiskInfo().getSize() * 2);
        initializeCommand(parameters);

        DiskImage oldDisk = createDiskImage();
        doReturn(oldDisk).when(command).getOldDisk();

        VmDevice vmDevice = stubVmDevice(diskImageGuid, vmId);
        vmDevice.setIsReadOnly(true);

        assertFalse(command.validateCanResizeDisk());
        ValidateTestUtils.assertValidationMessages
                ("wrong failure", command, EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
    }

    private void initializeCommand(VmDiskOperationParameterBase params) {
        initializeCommand(params, Collections.singletonList(createVmStatusDown()));
    }

    protected void initializeCommand(VmDiskOperationParameterBase params, List<VM> vms) {
        // Done before creating the spy to have correct values during the ctor run
        mockCtorRelatedDaoCalls(vms);
        command = spy(new UpdateVmDiskCommand<VmDiskOperationParameterBase>(
                params, CommandContext.createContext(params.getSessionId())) {
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            public DiskDao getDiskDao() {
                return diskDao;
            }

            @Override
            public VmDao getVmDao() {
                return vmDao;
            }

        });
        doReturn(snapshotDao).when(command).getSnapshotDao();
        doReturn(diskImageDao).when(command).getDiskImageDao();
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
        doReturn(storageDomainStaticDao).when(command).getStorageDomainStaticDao();
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        doReturn(vmStaticDao).when(command).getVmStaticDao();
        doReturn(baseDiskDao).when(command).getBaseDiskDao();
        doReturn(imageDao).when(command).getImageDao();
        doReturn(vmDeviceDao).when(command).getVmDeviceDao();
        doReturn(vmDao).when(command).getVmDao();
        doReturn(diskDao).when(command).getDiskDao();
        doReturn(diskVmElementDao).when(command).getDiskVmElementDao();
        doNothing().when(command).reloadDisks();
        doNothing().when(command).updateBootOrder();
        doNothing().when(vmStaticDao).incrementDbGeneration(any(Guid.class));

        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
        when(diskValidator.isVirtIoScsiValid(any(VM.class), any(DiskVmElement.class))).thenReturn(ValidationResult.VALID);
        when(diskValidator.isDiskUsedAsOvfStore()).thenReturn(ValidationResult.VALID);
        doReturn(ValidationResult.VALID).when(diskValidator).isDiskAttachedToVm(any(VM.class));
        doReturn(ValidationResult.VALID).when(diskValidator).isDiskExists();
        doReturn(ValidationResult.VALID).when(diskValidator).validateNotHostedEngineDisk();
        doReturn(ValidationResult.VALID).when(diskValidator).isReadOnlyPropertyCompatibleWithInterface(any(DiskVmElement.class));
        doReturn(diskValidator).when(command).getDiskValidator(any(Disk.class));
        doReturn(true).when(command).setAndValidateDiskProfiles();

        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        mockVds();
        mockVmsStoragePoolInfo(vms);
        mockToUpdateDiskVm(vms);

        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(Integer.MAX_VALUE);
        sd.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        doReturn(sdValidator).when(command).getStorageDomainValidator(any(DiskImage.class));

        command.init();
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldNotBeUpdated() {
        // Disk should be updated as Read Only
        final VmDiskOperationParameterBase parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
    }

    @Test
    public void testUpdateLockedDisk() {
        final VmDiskOperationParameterBase parameters = createParameters();
        DiskImage disk = createDiskImage();
        disk.setImageStatus(ImageStatus.LOCKED);
        when(diskDao.get(diskImageGuid)).thenReturn(disk);

        initializeCommand(parameters);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void testDiskAliasAdnDescriptionMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final VmDiskOperationParameterBase parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskAlias("New Disk Alias");
        parameters.getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testOnlyDiskAliasChangedMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final VmDiskOperationParameterBase parameters = createParameters();
        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskAlias("New Disk Alias");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testOnlyDescriptionsChangedMetaDataShouldBeUpdated() {
        // Disk should be updated as Read Only
        final VmDiskOperationParameterBase parameters = createParameters();

        when(diskDao.get(diskImageGuid)).thenReturn(createDiskImage());

        parameters.getDiskInfo().setDiskDescription("New Disk Description");
        initializeCommand(parameters);
        mockVdsCommandSetVolumeDescription();
        command.executeVmCommand();
        verify(command, times(1)).setVolumeDescription(any(DiskImage.class), any(StorageDomain.class));
    }

    @Test
    public void testExtendingDiskWithQuota() {
        Guid quotaId = Guid.newGuid();

        DiskImage oldDiskImage = createDiskImage();
        oldDiskImage.setQuotaId(quotaId);
        oldDiskImage.setSize(SizeConverter.convert(3, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        DiskImage newDiskImage = createDiskImage();
        newDiskImage.setQuotaId(quotaId);
        newDiskImage.setSize(SizeConverter.convert(5, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue());

        VmDiskOperationParameterBase parameters = new VmDiskOperationParameterBase(new DiskVmElement(newDiskImage.getId(), vmId), newDiskImage);
        long diskExtendingDiffInGB = newDiskImage.getSizeInGigabytes() - oldDiskImage.getSizeInGigabytes();

        when(diskDao.get(diskImageGuid)).thenReturn(oldDiskImage);
        initializeCommand(parameters);

        QuotaStorageConsumptionParameter consumptionParameter =
                (QuotaStorageConsumptionParameter) command.getQuotaStorageConsumptionParameters().get(0);
        assertEquals(consumptionParameter.getRequestedStorageGB().longValue(), diskExtendingDiffInGB);
    }

    private void mockToUpdateDiskVm(List<VM> vms) {
        for (VM vm: vms) {
            if (vm.getId().equals(command.getParameters().getVmId())) {
                when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
                when(diskVmElementDao.get(new VmDeviceId(command.getParameters().getDiskInfo().getId(), vm.getId()))).thenReturn(new DiskVmElement());
                break;
            }
        }
    }

    private void mockNullVm() {
        mockGetForDisk((VM) null);
        mockGetVmsListForDisk(null);
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(null);
    }

    protected void mockInterfaceList() {
        ArrayList<String> diskInterfaces = new ArrayList<>(
                Arrays.asList(new String[] {
                        "IDE",
                        "VirtIO",
                        "VirtIO_SCSI"
                }));

        when(osRepository.getDiskInterfaces(anyInt(), any(Version.class))).thenReturn(diskInterfaces);
    }

    protected VM createVmStatusDown(VM... otherPluggedVMs) {
        return createVm(VMStatus.Down);
    }

    protected VM createVm(VMStatus status) {
        VM vm = new VM();
        vm.setStatus(status);
        vm.setGuestOs("rhel6");
        vm.setId(vmId);
        return vm;
    }

    private void mockCtorRelatedDaoCalls(List<VM> vms) {
        mockGetForDisk(vms);
        mockGetVmsListForDisk(vms);
    }

    private void mockVmsStoragePoolInfo(List<VM> vms) {
        StoragePool storagePool = mockStoragePool();
        for (VM vm : vms) {
            vm.setStoragePoolId(storagePool.getId());
        }
    }

    private void mockGetForDisk(VM vm) {
        mockGetForDisk(Collections.singletonList(vm));
    }

    private void mockGetForDisk(List<VM> vms) {
        Map<Boolean, List<VM>> vmsMap = new HashMap<>();
        vmsMap.put(Boolean.TRUE, vms);
        when(vmDao.getForDisk(diskImageGuid, true)).thenReturn(vmsMap);
    }

    private void mockGetVmsListForDisk(List<VM> vms) {
        List<Pair<VM, VmDevice>> vmsWithVmDevice = new ArrayList<>();
        if (vms != null) {
            for (VM vm : vms) {
            VmDevice device = createVmDevice(diskImageGuid, vm.getId());
            vmsWithVmDevice.add(new Pair<>(vm, device));
            }
        } else {
            vms = Collections.emptyList();
        }

        when(vmDao.getVmsWithPlugInfo(diskImageGuid)).thenReturn(vmsWithVmDevice);
    }

    /**
     * Mock VDS
     */
    protected void mockVds() {
        VDS vds = new VDS();
        command.setVdsId(Guid.Empty);
        doReturn(vdsDao).when(command).getVdsDao();
        when(vdsDao.get(Guid.Empty)).thenReturn(vds);
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
                0,
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
