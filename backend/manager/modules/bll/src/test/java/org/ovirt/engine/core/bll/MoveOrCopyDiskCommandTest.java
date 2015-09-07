package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
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
    private SnapshotsValidator snapshotsValidator;

    private Disk disk;

    /**
     * The command under test.
     */
    protected MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> command;

    @Test
    public void canDoActionImageNotFound() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        when(diskImageDao.get(any(Guid.class))).thenReturn(null);
        when(diskImageDao.getSnapshotById(any(Guid.class))).thenReturn(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionWrongDiskImageTypeTemplate() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initTemplateDiskImage();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK.toString()));
    }

    @Test
    public void moveShareableDiskToGlusterDomain() {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(true);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS.toString()));
    }

    @Test
    public void moveShareableDisk() {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        initVmDiskImage(true);

        assertTrue(command.canDoAction());
    }

    @Test
    public void moveDiskToGluster() {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(false);

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionSameSourceAndDest() throws Exception {
        destStorageId = srcStorageId;
        initializeCommand(ImageOperation.Move, new DiskImage());
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.toString()));
    }

    @Test
    public void canDoActionVmIsNotDown() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSnapshotValidator();
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(vmDeviceDao).when(command).getVmDeviceDao();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString()));
    }

    @Test
    public void canDoActionDiskIsLocked() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(vmDeviceDao).when(command).getVmDeviceDao();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED.toString()));
    }

    @Test
    public void canDoActionDiskIsOvfStore() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initVmDiskImage(false);
        command.getImage().setContentType(DiskContentType.OVF_STORE);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void canDoActionTemplateImageIsLocked() throws Exception {
        initializeCommand(ImageOperation.Copy, new DiskImage());
        initTemplateDiskImage();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(new VmTemplate()).when(command).getTemplateForImage();

        command.defineVmTemplate();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED.toString()));
    }

    @Test
    public void canDoActionNotEnoughSpace() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canDoActionEnoughSpace() throws Exception {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void successVmInPreviewForAttachedSnapshot() {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        vmDevice.setSnapshotId(Guid.newGuid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionVmInPreview() {
        initializeCommand(ImageOperation.Move, new DiskImage());
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        when(snapshotsValidator.vmNotInPreview(any(Guid.class))).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void canDoActionFailureOnMovingLunDisk() {
        initializeCommand(ImageOperation.Move, new LunDisk());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void canDoActionFailureOnCopyingLunDisk() {
        initializeCommand(ImageOperation.Copy, new LunDisk());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void canDoActionFailureOnMovingVmLunDisk() {
        initializeCommand(ImageOperation.Move, new LunDisk());
        vmDevice.setSnapshotId(Guid.newGuid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void canDoActionFailureOnMovingCinderDisk() {
        initializeCommand(ImageOperation.Move, new CinderDisk());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void canDoActionFailureOnCopyingCinderDisk() {
        initializeCommand(ImageOperation.Copy, new CinderDisk());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    protected void initVmForSpace() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);

        // Re-mock the vmDao to return this specific VM for it to be correlated with the vm list mocked by getVmsWithPlugInfo(..).
        doReturn(vmDao).when(command).getVmDao();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        List<Pair<VM, VmDevice>> vmList = Collections.singletonList(new Pair<>(vm, vmDevice));
        when(vmDao.getVmsWithPlugInfo(any(Guid.class))).thenReturn(vmList);
    }

    private void mockGetVmsListForDisk() {
        List<Pair<VM, VmDevice>> vmList = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setStatus(VMStatus.PoweringDown);
        VM vm2 = new VM();
        vm2.setStatus(VMStatus.Down);
        VmDevice device1 = new VmDevice();
        device1.setIsPlugged(true);
        VmDevice device2 = new VmDevice();
        device2.setIsPlugged(true);
        vmList.add(new Pair<>(vm1, device1));
        vmList.add(new Pair<>(vm2, device2));

        when(vmDao.getVmsWithPlugInfo(any(Guid.class))).thenReturn(vmList);
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
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(stDomain);
    }

    private void initDestStorageDomain(StorageType storageType) {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageType(storageType);
        destDomain.setStatus(StorageDomainStatus.Active);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    @SuppressWarnings("unchecked")
    protected void initializeCommand(ImageOperation operation, Disk disk) {
        command = spy(new MoveOrCopyDiskCommandDummy(new MoveOrCopyImageGroupParameters(diskImageGuid,
                srcStorageId,
                destStorageId,
                operation)));


        doReturn(vmDao).when(command).getVmDao();
        doReturn(diskDao).when(command).getDiskDao();
        this.disk = disk;
        when(diskDao.get(any(Guid.class))).thenReturn(this.disk);

        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        when(vmDao.get(any(Guid.class))).thenReturn(vm);

        when(vmDao.getVmsWithPlugInfo(any(Guid.class))).thenReturn(new ArrayList<Pair<VM, VmDevice>>());
        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        doReturn(false).when(command).acquireLock();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
    }

    private void initSnapshotValidator() {
        when(snapshotsValidator.vmNotInPreview(any(Guid.class))).thenReturn(ValidationResult.VALID);
        when(snapshotsValidator.vmNotDuringSnapshot(any(Guid.class))).thenReturn(ValidationResult.VALID);
        when(command.getSnapshotsValidator()).thenReturn(snapshotsValidator);
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

    /**
     * The following class is created in order to allow to use a mock diskImageDao in constructor
     */
    private class MoveOrCopyDiskCommandDummy extends MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> {

        public MoveOrCopyDiskCommandDummy(MoveOrCopyImageGroupParameters parameters) {
            super(parameters);
        }

        @Override
        protected VmTemplate getTemplateForImage() {
            return null;
        }

        @Override
        protected DiskImageDao getDiskImageDao() {
            return diskImageDao;
        }
    }
}
