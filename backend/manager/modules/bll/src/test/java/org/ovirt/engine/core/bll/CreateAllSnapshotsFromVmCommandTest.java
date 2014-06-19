package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;

/** A test case for the {@link CreateAllSnapshotsFromVmCommand} class. */
@RunWith(MockitoJUnitRunner.class)
public class CreateAllSnapshotsFromVmCommandTest {
    private CreateAllSnapshotsFromVmCommand<CreateAllSnapshotsFromVmParameters> cmd;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmDAO vmDao;

    @Mock
    private VmValidator vmValidator;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private VM vm;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private MultipleStorageDomainsValidator multipleStorageDomainsValidator;

    @Mock
    private StoragePoolValidator storagePoolValidator;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        CreateAllSnapshotsFromVmParameters params = new CreateAllSnapshotsFromVmParameters(Guid.newGuid(), "");
        cmd = spy(new CreateAllSnapshotsFromVmCommand<CreateAllSnapshotsFromVmParameters>(params));
        doReturn(true).when(vm).isManagedVm();
        doReturn(vm).when(cmd).getVm();
        doReturn(vmValidator).when(cmd).createVmValidator();
        doReturn(snapshotsValidator).when(cmd).createSnapshotValidator();
        doReturn(storagePoolValidator).when(cmd).createStoragePoolValidator();
        doReturn(diskImagesValidator).when(cmd).createDiskImageValidator(any(List.class));
        doReturn(multipleStorageDomainsValidator).when(cmd).createMultipleStorageDomainsValidator(any(List.class));
        doReturn(vmValidator).when(cmd).createVmValidator();
    }

    @Test
    public void testPositiveCanDoActionWithNoDisks() {
        setUpGeneralValidations();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        assertTrue(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().isEmpty());
    }

    @Test
    public void testVMIsNotValid() {
        setUpGeneralValidations();
        doReturn(Boolean.FALSE).when(cmd).validateVM(vmValidator);
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void testStoragePoolIsNotUp() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND)).when(storagePoolValidator)
                .isUp();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND.name()));
    }

    @Test
    public void testVmDuringSnaoshot() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT)).when(snapshotsValidator)
                .vmNotDuringSnapshot(any(Guid.class));
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT.name()));
    }

    @Test
    public void testVmInPreview() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW)).when(snapshotsValidator)
                .vmNotInPreview(any(Guid.class));
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW.name()));
    }

    @Test
    public void testVmDuringMigration() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS)).when(vmValidator)
                .vmNotDuringMigration();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS.name()));
    }

    @Test
    public void testVmRunningStateless() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS)).when(vmValidator)
                .vmNotRunningStateless();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS.name()));
    }

    @Test
    public void testLiveSnapshotWhenNoPluggedDiskSnapshot() {
        setUpGeneralValidations();
        doReturn(true).when(cmd).isLiveSnapshotApplicable();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertTrue(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .isEmpty());
    }

    @Test
    public void testVmIllegal() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL)).when(vmValidator)
                .vmNotIlegal();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL.name()));
    }

    @Test
    public void testVmLocked() {
        setUpGeneralValidations();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED)).when(vmValidator)
                .vmNotLocked();
        doReturn(getEmptyDiskList()).when(cmd).getDisksList();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED.name()));
    }

    @Test
    public void testPositiveCanDoActionWithDisks() {
        setUpGeneralValidations();
        setUpDiskValidations();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(Guid.newGuid()).when(cmd).getStorageDomainId();
        assertTrue(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().isEmpty());
    }

    @Test
    public void testImagesLocked() {
        setUpGeneralValidations();
        setUpDiskValidations();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED)).when(diskImagesValidator)
                .diskImagesNotLocked();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED.name()));
    }

    @Test
    public void testImagesIllegal() {
        setUpGeneralValidations();
        setUpDiskValidations();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL)).when(diskImagesValidator)
                .diskImagesNotIllegal();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_ILLEGAL.name()));
    }

    @Test
    public void testImagesDoesNotExist() {
        setUpGeneralValidations();
        setUpDiskValidations();

        DiskImage diskImage1 = getNewDiskImage();
        DiskImage diskImage2 = getNewDiskImage();

        List<DiskImage> diskImagesFromParams = new ArrayList<>();
        diskImagesFromParams.addAll(Arrays.asList(diskImage1, diskImage2));
        cmd.getParameters().setDisks(diskImagesFromParams);

        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_NOT_EXIST)).when(diskImagesValidator)
                .diskImagesNotExist();

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_NOT_EXIST);
    }

    @Test
    public void testAllDomainsExistAndActive() {
        setUpGeneralValidations();
        setUpDiskValidations();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST)).when(multipleStorageDomainsValidator)
                .allDomainsExistAndActive();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.name()));
    }

    @Test
    public void testAllDomainsWithinTheshold() {
        setUpGeneralValidations();
        setUpDiskValidations();
        doReturn(getNonEmptyDiskList()).when(cmd).getDisksList();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).when(multipleStorageDomainsValidator)
                .allDomainsExistAndActive();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.name()));
    }

    private void setUpDiskValidations() {
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotLocked();
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotIllegal();
        doReturn(ValidationResult.VALID).when(multipleStorageDomainsValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(multipleStorageDomainsValidator).allDomainsWithinThresholds();
    }

    private void setUpGeneralValidations() {
        doReturn(Boolean.TRUE).when(cmd).validateVM(vmValidator);
        doReturn(ValidationResult.VALID).when(storagePoolValidator).isUp();
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotDuringMigration();
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotRunningStateless();
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotIlegal();
        doReturn(ValidationResult.VALID).when(vmValidator).vmNotLocked();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
    }

    private static List<DiskImage> getEmptyDiskList() {
        List<DiskImage> diskList = new ArrayList<>();
        return diskList;
    }

    private static List<DiskImage> getNonEmptyDiskList() {
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage newDiskImage = new DiskImage();
        diskList.add(newDiskImage);
        return diskList;
    }

    private static DiskImage getNewDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        return diskImage;
    }
}
