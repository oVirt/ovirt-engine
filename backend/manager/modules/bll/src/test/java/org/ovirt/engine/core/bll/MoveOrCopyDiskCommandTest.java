package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class MoveOrCopyDiskCommandTest {

    private final Guid diskImageGuid = Guid.newGuid();
    private Guid destStorageId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final static int FREE_SPACE_CRITICAL_LOW_IN_GB = 0;
    private final VmDevice vmDevice = new VmDevice();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, FREE_SPACE_CRITICAL_LOW_IN_GB));

    @Mock
    private DiskImageDAO diskImageDao;
    @Mock
    private StorageDomainDAO storageDomainDao;
    @Mock
    private VmDAO vmDao;
    @Mock
    private VmDeviceDAO vmDeviceDao;
    @Mock
    private SnapshotsValidator snapshotsValidator;

    /**
     * The command under test.
     */
    protected MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> command;

    @Test
    public void canDoActionImageNotFound() throws Exception {
        initializeCommand(ImageOperation.Move);
        when(diskImageDao.get(any(Guid.class))).thenReturn(null);
        when(diskImageDao.getSnapshotById(any(Guid.class))).thenReturn(null);
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionWrongDiskImageTypeTemplate() throws Exception {
        initializeCommand(ImageOperation.Move);
        initTemplateDiskImage();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK.toString()));
    }

    @Test
    public void canDoActionWrongDiskImageTypeVm() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initVmDiskImage();
        doReturn(null).when(command).getTemplateForImage();
        command.defineVmTemplate();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK.toString()));
    }

    @Test
    public void canDoActionCanNotFindTemplate() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initTemplateDiskImage();
        doReturn(null).when(command).getTemplateForImage();
        command.defineVmTemplate();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void canDoActionSameSourceAndDest() throws Exception {
        destStorageId = srcStorageId;
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        initVmForFail();
        initSrcStorageDomain();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.toString()));
    }

    @Test
    public void canDoActionVmIsNotDown() throws Exception {
        initializeCommand(ImageOperation.Move);
        initSnapshotValidator();
        initVmDiskImage();
        initVmForFail();
//        initVmDiskImage(false);
//        mockGetVmsListForDisk();
        initSrcStorageDomain();
        initDestStorageDomain();
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();
        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString()));
    }

    @Test
    public void canDoActionDiskIsLocked() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        initVmForFail();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.toString()));
    }

    @Test
    public void canDoActionDiskIsOvfStore() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage();
        command.getImage().setOvfStore(true);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED);
    }

    @Test
    public void canDoActionTemplateImageIsLocked() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initTemplateDiskImage();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(new VmTemplate()).when(command).getTemplateForImage();

        command.defineVmTemplate();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString()));
    }

    @Test
    public void canDoActionNotEnoughSpace() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmForSpace();
        initVmDiskImage();
        initSrcStorageDomain();
        initDestStorageDomain();
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();
        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canDoActionEnoughSpace() throws Exception {
        initializeCommand(ImageOperation.Move);
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage();
        initSrcStorageDomain();
        initDestStorageDomain();
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void successVmInPreviewForAttachedSnapshot() {
        initializeCommand(ImageOperation.Move);
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage();
        initSrcStorageDomain();
        initDestStorageDomain();
        vmDevice.setSnapshotId(Guid.newGuid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void canDoActionVmInPreview() {
        initializeCommand(ImageOperation.Move);
        initSnapshotValidator();
        initVmForSpace();
        initVmDiskImage();
        initSrcStorageDomain();
        initDestStorageDomain();
        when(snapshotsValidator.vmNotInPreview(any(Guid.class))).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    protected void initVmForSpace() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vmDao).when(command).getVmDAO();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        List<Pair<VM, VmDevice>> vmList = Collections.singletonList(new Pair<>(vm, vmDevice));
        when(vmDao.getVmsWithPlugInfo(any(Guid.class))).thenReturn(vmList);
    }

    protected void initVmForFail() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vmDao).when(command).getVmDAO();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        mockGetVmsListForDisk();
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
        when(storageDomainValidator.hasSpaceForClonedDisk(any(DiskImage.class))).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        return storageDomainValidator;
    }

    private static StorageDomainValidator mockStorageDomainValidatorWithSpace() {
        StorageDomainValidator storageDomainValidator = mockStorageDomainValidator();
        when(storageDomainValidator.hasSpaceForClonedDisk(any(DiskImage.class))).thenReturn(ValidationResult.VALID);
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
        doReturn(storageDomainDao).when(command).getStorageDomainDAO();
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(stDomain);
    }

    private void initDestStorageDomain() {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStatus(StorageDomainStatus.Active);
        destDomain.setStorageType(StorageType.NFS);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    @SuppressWarnings("unchecked")
    protected void initializeCommand(ImageOperation operation) {
        command = spy(new MoveOrCopyDiskCommandDummy(new MoveOrCopyImageGroupParameters(diskImageGuid,
                srcStorageId,
                destStorageId,
                operation)));

        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        doReturn(false).when(command).acquireLock();
        doReturn(true).when(command).setAndValidateDiskProfiles();
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

    private void initVmDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setVmEntityType(VmEntityType.VM);
        when(diskImageDao.get(any(Guid.class))).thenReturn(diskImage);
    }

    private DiskValidator spyDiskValidator(Disk disk) {
        DiskValidator diskValidator = spy(new DiskValidator(disk));
        doReturn(diskValidator).when(command).createDiskValidator();
        return diskValidator;
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
        protected DiskImageDAO getDiskImageDao() {
            return diskImageDao;
        }
    }
}
