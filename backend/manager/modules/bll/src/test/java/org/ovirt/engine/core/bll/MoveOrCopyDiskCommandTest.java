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
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
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
    public void moveShareableDiskToGlusterDomain() {
        initializeCommand(ImageOperation.Move);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(true);

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS.toString()));
    }

    @Test
    public void moveShareableDisk() {
        initializeCommand(ImageOperation.Move);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        initVmDiskImage(true);

        assertTrue(command.canDoAction());
    }

    @Test
    public void moveDiskToGluster() {
        initializeCommand(ImageOperation.Move);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        initVmDiskImage(false);

        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionWrongDiskImageTypeVm() throws Exception {
        initializeCommand(ImageOperation.Copy);
        initVmDiskImage(false);
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
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME.toString()));
    }

    @Test
    public void canDoActionVmIsNotDown() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();

        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString()));
    }

    @Test
    public void canDoActionDiskIsLocked() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage(false);
        mockGetVmsListForDisk();
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(vmDeviceDao).when(command).getVmDeviceDAO();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.toString()));
    }

    @Test
    public void canDoActionDiskIsOvfStore() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmDiskImage(false);
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
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithoutSpace()).when(command).createStorageDomainValidator();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void canDoActionEnoughSpace() throws Exception {
        initializeCommand(ImageOperation.Move);
        initVmForSpace();
        initVmDiskImage(false);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    protected void initVmForSpace() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);

        // Re-mock the vmDao to return this specific VM for it to be correlated with the vm list mocked by getVmsWithPlugInfo(..).
        doReturn(vmDao).when(command).getVmDAO();
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
        VmDevice device = new VmDevice();
        List<Pair<VM, VmDevice>> vmList = Collections.singletonList(new Pair<>(vm, device));
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
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(stDomain);
    }

    private void initDestStorageDomain(StorageType storageType) {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageType(storageType);
        destDomain.setStatus(StorageDomainStatus.Active);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    @SuppressWarnings("unchecked")
    protected void initializeCommand(ImageOperation operation) {
        command = spy(new MoveOrCopyDiskCommandDummy(new MoveOrCopyImageGroupParameters(diskImageGuid,
                srcStorageId,
                destStorageId,
                operation)));


        doReturn(vmDao).when(command).getVmDAO();

        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        when(vmDao.get(any(Guid.class))).thenReturn(vm);

        when(vmDao.getVmsWithPlugInfo(any(Guid.class))).thenReturn(new ArrayList<Pair<VM, VmDevice>>());
        doReturn(new ArrayList<DiskImage>()).when(command).getAllImageSnapshots();
        doReturn(mockStorageDomainValidatorWithSpace()).when(command).createStorageDomainValidator();
        doReturn(false).when(command).acquireLock();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(storageDomainDao).when(command).getStorageDomainDAO();
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
        protected DiskImageDAO getDiskImageDao() {
            return diskImageDao;
        }
    }
}
