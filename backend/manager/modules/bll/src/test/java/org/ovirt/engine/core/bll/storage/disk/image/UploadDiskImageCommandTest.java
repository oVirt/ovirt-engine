package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.action.UploadDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class UploadDiskImageCommandTest extends UploadImageCommandTest{

    @Mock
    DiskValidator diskValidator;

    @Mock
    DiskImagesValidator diskImagesValidator;

    @Override
    protected UploadDiskImageCommand spyCommand() {
        return new UploadDiskImageCommand(new UploadDiskImageParameters(), null);
    }

    @Before
    public void setUp() {
        initCommand();
    }

    protected void initializeSuppliedImage() {
        super.initSuppliedImage(uploadImageCommand);

        DiskImage diskImage = new DiskImage();
        doReturn(diskImage).when(diskDao).get(any());

        doReturn(diskValidator).when(getCommand()).getDiskValidator(any());
        doReturn(diskImagesValidator).when(getCommand()).getDiskImagesValidator(any());

        // Set validators return
        doReturn(ValidationResult.VALID)
                .when(diskValidator)
                .isDiskAttachedToAnyVm();
        doReturn(ValidationResult.VALID)
                .when(diskValidator)
                .isDiskExists();
        doReturn(ValidationResult.VALID)
                .when(diskImagesValidator)
                .diskImagesNotLocked();
        doReturn(ValidationResult.VALID)
                .when(diskImagesValidator)
                .diskImagesNotIllegal();
    }

    @Test
    public void validate() {
        initializeSuppliedImage();
        assertTrue(getCommand().validate());
    }

    @Test
    public void validateCantUploadLockedImage() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED, ""))
                .when(diskImagesValidator)
                .diskImagesNotLocked();

        getCommand().validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for a locked image.",
                getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void validateCantUploadDiskAttached() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_ATTACHED_TO_VMS, ""))
                .when(diskValidator)
                .isDiskAttachedToAnyVm();

        getCommand().validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for an image that is attached to any VMs.",
                getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_DISK_ATTACHED_TO_VMS);
    }

    @Test
    public void validateCantUploadDiskNotExists() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST, ""))
                .when(diskValidator)
                .isDiskExists();

        getCommand().validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for image that doesn't exist.",
                getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateCantUploadIllegalImage() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL, ""))
                .when(diskImagesValidator)
                .diskImagesNotIllegal();

        getCommand().validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for an illegal image.",
                getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    private UploadDiskImageCommand getCommand() {
        return (UploadDiskImageCommand) uploadImageCommand;
    }
}
