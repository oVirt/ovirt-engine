package org.ovirt.engine.core.bll.snapshots;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.utils.VmOverheadCalculator;
import org.ovirt.engine.core.bll.utils.VmOverheadCalculatorImpl;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link CreateAllSnapshotsFromVmCommand} class. */
public class CreateAllSnapshotsFromVmCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private CreateAllSnapshotsFromVmCommand<CreateAllSnapshotsFromVmParameters> cmd =
            new CreateAllSnapshotsFromVmCommand<>
                    (new CreateAllSnapshotsFromVmParameters(Guid.newGuid(), "", false), null);

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
    private MultipleStorageDomainsValidator multipleStorageDomainsValidator;

    @Mock
    private StoragePoolValidator storagePoolValidator;

    @Mock
    private MemoryImageBuilder memoryImageBuilder;

    @Spy
    private VmOverheadCalculator vmOverheadCalculator = new VmOverheadCalculatorImpl();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        doReturn(true).when(vm).isManagedVm();
        doReturn(DisplayType.vga).when(vmStatic).getDefaultDisplayType();
        doReturn(vmStatic).when(vm).getStaticData();
        doReturn(vm).when(cmd).getVm();
        doReturn(vmValidator).when(cmd).createVmValidator();
        doReturn(storagePoolValidator).when(cmd).createStoragePoolValidator();
        doReturn(diskImagesValidator).when(cmd).createDiskImageValidator(anyList());
        doReturn(multipleStorageDomainsValidator).when(cmd).createMultipleStorageDomainsValidator(anyList());
        doReturn(memoryImageBuilder).when(cmd).getMemoryImageBuilder();
        doReturn(true).when(cmd).validateCinder();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainIdForVmMemory(anyList());
        doReturn(getEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(getEmptyDiskList()).when(cmd).getDiskImagesForVm();
    }

    @Test
    public void testPositiveValidateWithNoDisks() {
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        assertTrue(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testVMIsNotValid() {
        doReturn(Boolean.FALSE).when(cmd).validateVM(vmValidator);
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
    }

    @Test
    public void testStoragePoolIsNotUp() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND)).when(storagePoolValidator)
                .isUp();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND.name()));
    }

    @Test
    public void testVmDuringSnaoshot() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT)).when(snapshotsValidator)
                .vmNotDuringSnapshot(any(Guid.class));
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT.name()));
    }

    @Test
    public void testVmInPreview() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW)).when(snapshotsValidator)
                .vmNotInPreview(any(Guid.class));
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW.name()));
    }

    @Test
    public void testVmDuringMigration() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS)).when(vmValidator)
                .vmNotDuringMigration();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS.name()));
    }

    @Test
    public void testSaveMemoryPciPassthroughFailure() {
        cmd.getParameters().setSaveMemory(true);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES))
                .when(vmValidator)
                .vmNotHavingPciPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertThat(cmd.getReturnValue().getValidationMessages(),
                hasItem(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES.name()));
    }

    @Test
    public void testNoMemoryPciPassthroughSuccess() {
        cmd.getParameters().setSaveMemory(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES))
                .when(vmValidator)
                .vmNotHavingPciPassthroughDevices();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertTrue(cmd.validate());
        assertThat(cmd.getReturnValue().getValidationMessages(), is(empty()));
    }

    @Test
    public void testVmRunningStateless() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS)).when(vmValidator)
                .vmNotRunningStateless();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS.name()));
    }

    @Test
    public void testLiveSnapshotWhenNoPluggedDiskSnapshot() {
        doReturn(true).when(cmd).isLiveSnapshotApplicable();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertTrue(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .isEmpty());
    }

    @Test
    public void testVmIllegal() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL)).when(vmValidator)
                .vmNotIlegal();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL.name()));
    }

    @Test
    public void testVmLocked() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED)).when(vmValidator)
                .vmNotLocked();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED.name()));
    }

    @Test
    public void testPositiveValidateWithDisks() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        assertTrue(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testImagesExceededNumberOfVolumesInChain() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAXIMUM_LIMIT_OF_VOLUMES_IN_CHAIN)).when(diskImagesValidator)
                .diskImagesHaveNotExceededMaxNumberOfVolumesInImageChain();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_MAXIMUM_LIMIT_OF_VOLUMES_IN_CHAIN.name()));
    }

    @Test
    public void testImagesLocked() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)).when(diskImagesValidator)
                .diskImagesNotLocked();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED.name()));
    }

    @Test
    public void testImagesIllegal() {
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksListForChecks();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).when(diskImagesValidator)
                .diskImagesNotIllegal();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL.name()));
    }

    @Test
    public void testImagesDoesNotExist() {
        DiskImage diskImage1 = getNewDiskImage();
        DiskImage diskImage2 = getNewDiskImage();

        List<DiskImage> diskImagesFromParams = new ArrayList<>();
        diskImagesFromParams.addAll(Arrays.asList(diskImage1, diskImage2));
        cmd.getParameters().setDisks(diskImagesFromParams);

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST)).when(diskImagesValidator)
                .diskImagesNotExist();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST);
    }

    @Test
    public void testAllDomainsExistAndActive() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST)).when(multipleStorageDomainsValidator)
                .allDomainsExistAndActive();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.name()));
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
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.name()));
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksFailure() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        cmd.getParameters().setSaveMemory(true);
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainIdForVmMemory(eq(Collections.emptyList()));
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).when(multipleStorageDomainsValidator)
                .allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), anyList());
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), anyList());
    }

    @Test
    public void testAllDomainsHaveSpaceForAllDisksSuccess() {
        doReturn(Collections.emptyList()).when(cmd).getDisksList();
        cmd.getParameters().setSaveMemory(true);
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainIdForVmMemory(eq(Collections.emptyList()));
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        verify(multipleStorageDomainsValidator).allDomainsHaveSpaceForAllDisks(eq(Collections.emptyList()), anyList());
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

    private static DiskImage getNewDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        return diskImage;
    }
}
