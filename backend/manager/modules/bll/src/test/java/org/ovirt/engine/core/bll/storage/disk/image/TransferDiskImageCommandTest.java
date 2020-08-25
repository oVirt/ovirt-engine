package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferBackend;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransferDiskImageCommandTest extends BaseCommandTest {

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private ImageTransferDao imageTransferDao;

    @Mock
    private VmBackupDao vmBackupDao;

    @Spy
    @InjectMocks
    private  TransferDiskImageCommand<TransferDiskImageParameters> transferImageCommand =
            new TransferDiskImageCommand<>(new TransferDiskImageParameters(), null);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
            MockConfigDescriptor.of(ConfigValues.TransferImageClientInactivityTimeoutInSeconds, 600),
            MockConfigDescriptor.of(ConfigValues.ImageTransferProxyEnabled, Boolean.TRUE));
    }

    @BeforeEach
    public void setUp() {
        doNothing().when(transferImageCommand).createImage();
        doNothing().when(transferImageCommand).persistCommand(any(), anyBoolean());
        doNothing().when(transferImageCommand).lockImage();
        doNothing().when(transferImageCommand).startImageTransferSession();
    }

    private void initializeSuppliedImage() {
        Guid imageId = Guid.newGuid();
        transferImageCommand.getParameters().setImageId(imageId);

        DiskImage diskImage = new DiskImage();
        diskImage.setActive(true);
        diskImage.setImageId(imageId);
        diskImage.setStorageIds(Collections.singletonList(Guid.newGuid()));
        diskImage.setStorageTypes(Collections.singletonList(StorageType.NFS));
        doReturn(diskImage).when(diskImageDao).get(any());

        doReturn(diskValidator).when(transferImageCommand).getDiskValidator(any());
        doReturn(diskImagesValidator).when(transferImageCommand).getDiskImagesValidator(any());
        doReturn(storageDomainValidator).when(transferImageCommand).getStorageDomainValidator(any());
    }

    private DiskImage initReadyImageForUpload() {
        Guid imageId = Guid.newGuid();
        Guid sdId = Guid.newGuid();

        DiskImage readyImage = new DiskImage();
        readyImage.setImageId(imageId);
        readyImage.setStorageIds(Collections.singletonList(sdId));
        readyImage.setStorageTypes(Collections.singletonList(StorageType.NFS));
        readyImage.setVolumeFormat(VolumeFormat.COW);
        readyImage.setSize(1024L);
        readyImage.setApparentSizeInBytes(1127L);
        readyImage.setActualSizeInBytes(1127L);

        doReturn(readyImage).when(transferImageCommand).getDiskImage();
        return readyImage;
    }

    /************
     * Validation
     ************/
    @Test
    public void testValidationCallOnCreateImage() {
        doReturn(true).when(transferImageCommand).validateCreateImage();
        transferImageCommand.validate();
        verify(transferImageCommand, times(1)).validateCreateImage();
    }

    @Test
    public void testValidationCallOnSuppliedImage() {
        Guid imageId = Guid.newGuid();
        transferImageCommand.getParameters().setImageId(imageId);
        doReturn(true).when(transferImageCommand).validateImageTransfer();

        transferImageCommand.validate();
        verify(transferImageCommand, times(1)).validateImageTransfer();
    }

    @Test
    public void testFailOnDownloadWithoutImage() {
        transferImageCommand.getParameters().setTransferType(TransferType.Download);
        ValidateTestUtils.runAndAssertValidateFailure(transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_NOT_SPECIFIED_FOR_DOWNLOAD);
    }

    @Test
    public void validate() {
        initializeSuppliedImage();
        assertTrue(transferImageCommand.validate());
    }

    @Test
    public void validateCantUploadLockedImage() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED, ""))
                .when(diskImagesValidator)
                .diskImagesNotLocked();

        transferImageCommand.validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for a locked image.",
                transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void validateCantUploadDiskAttached() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_PLUGGED_TO_NON_DOWN_VMS, ""))
                .when(diskValidator)
                .isDiskPluggedToAnyNonDownVm(false);

        transferImageCommand.validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for an image that is attached to any VMs.",
                transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_DISK_PLUGGED_TO_NON_DOWN_VMS);
    }

    @Test
    public void validateCantUploadDiskNotExists() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST, ""))
                .when(diskValidator)
                .isDiskExists();

        transferImageCommand.validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for image that doesn't exist.",
                transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateCantUploadIllegalImage() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL, ""))
                .when(diskImagesValidator)
                .diskImagesNotIllegal();

        transferImageCommand.validate();
        ValidateTestUtils.assertValidationMessages(
                "Can't start a transfer for an illegal image.",
                transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    @Test
    public void validateCantUploadToNonActiveDomain() {
        initializeSuppliedImage();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2, ""))
                .when(storageDomainValidator)
                .isDomainExistAndActive();

        ValidateTestUtils.runAndAssertValidateFailure(
                "Can't start a transfer to a non-active storage domain.",
                transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    /*****************
     Command execution
     *****************/
    @Test
    public void testCreatingImageIfNotSupplied() {
        transferImageCommand.getParameters().setVolumeFormat(VolumeFormat.COW);
        transferImageCommand.executeCommand();

        // Make sure an image is created.
        verify(transferImageCommand, times(1)).createImage();

        // Make sure that a transfer session won't start yet.
        verify(transferImageCommand, never()).handleImageIsReadyForTransfer();
    }

    @Test
    public void testNotCreatingImageIfSupplied() {
        initializeSuppliedImage();
        doNothing().when(transferImageCommand).handleImageIsReadyForTransfer();
        transferImageCommand.executeCommand();

        // Make sure no image is created if an image Guid is supplied.
        verify(transferImageCommand, never()).createImage();

        // Make sure that a transfer session will start.
        verify(transferImageCommand, times(1)).handleImageIsReadyForTransfer();
    }

    @Test
    public void testFailsDownloadExecutionWithoutImage() {
        transferImageCommand.getParameters().setTransferType(TransferType.Download);

        ValidateTestUtils.runAndAssertValidateFailure(transferImageCommand,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_NOT_SPECIFIED_FOR_DOWNLOAD);
    }

    /*********************************
     * Handling ready image to upload
     ********************************/
    @Test
    public void testParamsUpdated() {
        DiskImage readyImage = initReadyImageForUpload();

        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getStorageDomainId(), readyImage.getStorageIds().get(0));
    }

    @Test
    public void testCommandPersistedWithParamUpdates() {
        DiskImage readyImage = initReadyImageForUpload();

        TransferDiskImageParameters params = spy(new TransferDiskImageParameters());
        doReturn(params).when(transferImageCommand).getParameters();

        transferImageCommand.handleImageIsReadyForTransfer();

        // Verify that persistCommand is being called after each of the params changes.
        InOrder inOrder = inOrder(params, transferImageCommand);
        inOrder.verify(params).setStorageDomainId(any());
        inOrder.verify(transferImageCommand).persistCommand(any(), anyBoolean());

        inOrder = inOrder(params, transferImageCommand);
        inOrder.verify(params).setTransferSize(anyLong());
        inOrder.verify(transferImageCommand).persistCommand(any(), anyBoolean());
    }

    /**********
     * Transfer Size
     *********/
    @Test
    public void testTransferSizeNBD() {
        DiskImage readyImage = initReadyImageForUpload();

        transferImageCommand.getParameters().setVolumeFormat(VolumeFormat.RAW);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getTransferBackend(), ImageTransferBackend.NBD);
        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getSize());
    }

    @Test
    public void testTransferSizeDownloadRAW() {
        DiskImage readyImage = initReadyImageForUpload();
        readyImage.setVolumeFormat(VolumeFormat.RAW);

        transferImageCommand.getParameters().setTransferType(TransferType.Download);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getSize());
    }

    @Test
    public void testTransferSizeDownloadCOW() {
        DiskImage readyImage = initReadyImageForUpload();
        readyImage.setVolumeFormat(VolumeFormat.COW);

        doReturn(readyImage.getApparentSizeInBytes()).when(transferImageCommand).getImageApparentSize(readyImage);

        transferImageCommand.getParameters().setTransferType(TransferType.Download);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getApparentSizeInBytes());
    }

    @Test
    public void testTransferSizeUploadFromUI() {
        DiskImage readyImage = initReadyImageForUpload();

        transferImageCommand.getParameters().setTransferType(TransferType.Upload);
        transferImageCommand.getParameters().setTransferSize(readyImage.getSize());
        transferImageCommand.getParameters().setTransferClientType(TransferClientType.TRANSFER_VIA_BROWSER);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getSize());
    }

    @Test
    public void testTransferSizeUploadRAW() {
        DiskImage readyImage = initReadyImageForUpload();
        readyImage.setVolumeFormat(VolumeFormat.RAW);

        transferImageCommand.getParameters().setTransferType(TransferType.Upload);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getSize());
    }

    @Test
    public void testTransferSizeUploadBlockCOW() {
        DiskImage readyImage = initReadyImageForUpload();
        readyImage.setVolumeFormat(VolumeFormat.COW);
        readyImage.setStorageTypes(Collections.singletonList(StorageType.ISCSI));

        transferImageCommand.getParameters().setTransferType(TransferType.Upload);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertEquals(transferImageCommand.getParameters().getTransferSize(), readyImage.getActualSizeInBytes());
    }

    @Test
    public void testTransferSizeUploadFileCOW() {
        DiskImage readyImage = initReadyImageForUpload();
        readyImage.setVolumeFormat(VolumeFormat.COW);
        readyImage.setStorageTypes(Collections.singletonList(StorageType.NFS));
        readyImage.setSize(65536L);

        transferImageCommand.getParameters().setTransferType(TransferType.Upload);
        transferImageCommand.handleImageIsReadyForTransfer();

        assertTrue(transferImageCommand.getParameters().getTransferSize() >= 327680);
    }

    /**********
     * Other
     *********/
    @Test
    public void testUploadIsDefaultTransferType() {
        assertEquals(TransferType.Upload, transferImageCommand.getParameters().getTransferType());
    }

    @Test
    public void testPermissionSubjectOnProvidedImage() {
        initializeSuppliedImage();
        assertEquals(transferImageCommand.getPermissionCheckSubjects().get(0),
                new PermissionSubject(transferImageCommand.getParameters().getImageGroupID(),
                        VdcObjectType.Disk,
                        ActionGroup.EDIT_DISK_PROPERTIES));
    }

    @Test
    public void testPermissionSubjectOnNewImage() {
        assertEquals(transferImageCommand.getPermissionCheckSubjects().get(0),
                new PermissionSubject(transferImageCommand.getParameters().getImageId(),
                        VdcObjectType.Storage,
                        ActionGroup.CREATE_DISK));
    }
}
