package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class MoveOrCopyDiskCommandTest extends BaseCommandTest {

    private final Guid diskImageGuid = Guid.newGuid();
    private Guid destStorageId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final VmDevice vmDevice = new VmDevice();

    @Mock
    private DiskDao diskDao;
    @Mock
    private DiskImageDao diskImageDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private DiskVmElementDao diskVmElementDao;
    @Mock
    private SnapshotsValidator snapshotsValidator;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    protected MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> command =
            new MoveOrCopyDiskCommand<>(new MoveOrCopyImageGroupParameters(diskImageGuid,
                    srcStorageId,
                    destStorageId,
                    ImageOperation.Move),
                    null);

    @Test
    public void validateImageNotFound() throws Exception {
        initializeCommand(new DiskImage());
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void validateWrongDiskImageTypeTemplate() throws Exception {
        initializeCommand(new DiskImage());
        initTemplateDiskImage();
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK.toString()));
    }

    @Test
    public void moveShareableDiskToGlusterDomain() {
        initializeCommand(new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(true);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS.toString()));
    }

    @Test
    public void moveShareableDisk() {
        initializeCommand(new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        initVmDiskImage(true);

        assertTrue(command.validate());
    }

    @Test
    public void moveDiskToGluster() {
        initializeCommand(new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(false);

        assertTrue(command.validate());
    }

    @Test
    public void validateSameSourceAndDest() throws Exception {
        destStorageId = srcStorageId;
        initializeCommand(new DiskImage());
        command.getParameters().setStorageDomainId(destStorageId);
        command.setStorageDomainId(destStorageId);
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.toString()));
    }

    @Test
    public void validateVmIsNotDown() throws Exception {
        initializeCommand(new DiskImage());
        initSnapshotValidator();
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString()));
    }

    @Test
    public void validateDiskIsLocked() throws Exception {
        initializeCommand(new DiskImage());
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        assertFalse(command.validate());
        assertTrue(command.getReturnValue().getValidationMessages().contains(
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED.toString()));
    }

    @Test
    public void validateDiskIsOvfStore() throws Exception {
        initializeCommand(new DiskImage());
        initVmDiskImage(false);
        command.getImage().setContentType(DiskContentType.OVF_STORE);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void validateTemplateImageIsLocked() throws Exception {
        initializeCommand(new DiskImage());
        command.getParameters().setOperation(ImageOperation.Copy);
        command.init();
        initTemplateDiskImage();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(new VmTemplate()).when(command).getTemplateForImage();

        command.init();
        assertFalse(command.validate());
        assertTrue(command.getReturnValue().getValidationMessages().contains(
                EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED.toString()));
    }

    @Test
    public void validateNotEnoughSpace() throws Exception {
        initializeCommand(new DiskImage());
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void validateEnoughSpace() throws Exception {
        initializeCommand(new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void successVmInPreviewForAttachedSnapshot() {
        initializeCommand(new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        vmDevice.setSnapshotId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateVmInPreview() {
        initializeCommand(new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        when(snapshotsValidator.vmNotInPreview(any())).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void validateFailureOnMovingLunDisk() {
        initializeCommand(new LunDisk());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void validateFailureOnCopyingLunDisk() {
        initializeCommand(new LunDisk());
        command.getParameters().setOperation(ImageOperation.Copy);
        command.init();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void validateFailureOnMovingVmLunDisk() {
        initializeCommand(new LunDisk());
        vmDevice.setSnapshotId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void validateFailureOnMovingCinderDisk() {
        initializeCommand(new CinderDisk());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void validateFailureOnCopyingCinderDisk() {
        initializeCommand(new CinderDisk());
        command.getParameters().setOperation(ImageOperation.Copy);
        command.init();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void passDiscardSupportedForDestSdMoveOp() {
        initializeCommand(new DiskImage());
        mockPassDiscardSupportedForDestSd(ValidationResult.VALID, ImageOperation.Move);
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardSupportedForDestSdCopyTemplateDiskOp() {
        initializeCommand(new DiskImage());
        mockPassDiscardSupportedForDestSd(ValidationResult.VALID, ImageOperation.Copy);
        initTemplateDiskImage();
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardSupportedForCopyFloatingDiskOp() {
        initializeCommand(new DiskImage());
        command.getParameters().setOperation(ImageOperation.Copy);
        initTemplateDiskImage();
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardNotSupportedForDestSd() {
        initializeCommand(new DiskImage());
        mockPassDiscardSupportedForDestSd(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE), ImageOperation.Move);
        assertFalse(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    protected void initVmForSpace() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);

        // Re-mock the vmDao to return this specific VM for it to be correlated with the vm list mocked by getVmsWithPlugInfo(..).
        when(vmDao.get(any())).thenReturn(vm);
        List<Pair<VM, VmDevice>> vmList = Collections.singletonList(new Pair<>(vm, vmDevice));
        when(vmDao.getVmsWithPlugInfo(any())).thenReturn(vmList);
    }

    private void mockGetVmsListForDisk() {
        List<Pair<VM, VmDevice>> vmList = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setStatus(VMStatus.PoweringDown);
        VM vm2 = new VM();
        vm2.setStatus(VMStatus.Down);
        VmDevice device1 = new VmDevice();
        device1.setPlugged(true);
        VmDevice device2 = new VmDevice();
        device2.setPlugged(true);
        vmList.add(new Pair<>(vm1, device1));
        vmList.add(new Pair<>(vm2, device2));

        when(vmDao.getVmsWithPlugInfo(any())).thenReturn(vmList);
    }

    private static StorageDomainValidator mockStorageDomainValidatorWithoutSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForDiskWithSnapshots(any(DiskImage.class))).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidatorWithSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForDiskWithSnapshots(any(DiskImage.class))).thenReturn(ValidationResult.VALID);
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidator() {
        StorageDomainValidator storageDomainValidator = mock(StorageDomainValidator.class);
        when(storageDomainValidator.isDomainExistAndActive()).thenReturn(ValidationResult.VALID);
        when(storageDomainValidator.isDomainWithinThresholds()).thenReturn(ValidationResult.VALID);
        return storageDomainValidator;
    }

    private void initSrcStorageDomain() {
        StorageDomain stDomain = new StorageDomain();
        stDomain.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(), any())).thenReturn(stDomain);
    }

    private void initDestStorageDomain(StorageType storageType) {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageType(storageType);
        destDomain.setStatus(StorageDomainStatus.Active);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    protected void initializeCommand(Disk disk) {
        when(diskDao.get(any())).thenReturn(disk);

        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        when(vmDao.get(any())).thenReturn(vm);

        when(vmDao.getVmsWithPlugInfo(any())).thenReturn(new ArrayList<>());
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(disk.getId()).when(command).getImageGroupId();
    }

    private void initSnapshotValidator() {
        when(snapshotsValidator.vmNotInPreview(any())).thenReturn(ValidationResult.VALID);
        when(snapshotsValidator.vmNotDuringSnapshot(any())).thenReturn(ValidationResult.VALID);
    }

    private void initTemplateDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.TEMPLATE);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    private void initVmDiskImage(boolean isShareable) {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.VM);
        diskImage.setShareable(isShareable);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    private void mockPassDiscardSupportedForDestSd(ValidationResult validationResult, ImageOperation imageOperation) {
        command.getParameters().setOperation(imageOperation);
        MultipleDiskVmElementValidator multipleDiskVmElementValidator = mock(MultipleDiskVmElementValidator.class);
        doReturn(multipleDiskVmElementValidator).when(command).createMultipleDiskVmElementValidator();
        when(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSd(any(Guid.class)))
                .thenReturn(validationResult);
    }
}
