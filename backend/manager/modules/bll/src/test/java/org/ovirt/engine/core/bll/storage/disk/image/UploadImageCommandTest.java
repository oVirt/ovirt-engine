package org.ovirt.engine.core.bll.storage.disk.image;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.UploadDiskImageParameters;
import org.ovirt.engine.core.common.action.UploadImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageTransferDao;

@RunWith(MockitoJUnitRunner.class)
public class UploadImageCommandTest extends BaseCommandTest{

    @Mock
    ImageTransferDao imageTransferDao;

    @Mock
    DiskDao diskDao;

    @Mock
    ImageTransferUpdater imageTransferUpdater;

    @Spy @InjectMocks
    protected UploadImageCommand<? extends UploadImageParameters> uploadImageCommand = spyCommand();

    @Before
    public void setUp() {
        initCommand();
    }

    protected UploadDiskImageCommand spyCommand() {
        return new UploadDiskImageCommand(new UploadDiskImageParameters(), null);
    }

    protected void initCommand() {
        doNothing().when(uploadImageCommand).createImage();
        doNothing().when(uploadImageCommand).persistCommand(any(), anyBoolean());
        doNothing().when(uploadImageCommand).lockImage();
        doReturn(true).when(uploadImageCommand).startImageTransferSession();
        doReturn(null).when(imageTransferUpdater).updateEntity(any(), any(), anyBoolean());
    }

    protected void initSuppliedImage(UploadImageCommand<? extends UploadImageParameters> command) {
        Guid imageId = Guid.newGuid();
        command.getParameters().setImageId(imageId);
    }

    private DiskImage initReadyImageForUpload() {
        Guid imageId = Guid.newGuid();
        Guid sdId = Guid.newGuid();

        ArrayList<Guid> sdList = new ArrayList<>();
        sdList.add(sdId);

        DiskImage readyImage = new DiskImage();
        readyImage.setImageId(imageId);
        readyImage.setStorageIds(sdList);
        readyImage.setSize(1024L);

        doReturn(readyImage).when(diskDao).get(readyImage.getImageId());
        return readyImage;
    }

    /************
     * Validation
     ************/
    @Test
    public void testValidationCallOnCreateImage() {
        doReturn(true).when(uploadImageCommand).validateCreateImage();
        uploadImageCommand.validate();
        verify(uploadImageCommand, times(1)).validateCreateImage();
    }

    @Test
    public void testValidationCallOnSuppliedImage() {
        Guid imageId = Guid.newGuid();
        uploadImageCommand.getParameters().setImageId(imageId);
        doReturn(true).when(uploadImageCommand).validateUploadToImage(imageId);

        uploadImageCommand.validate();
        verify(uploadImageCommand, times(1)).validateUploadToImage(imageId);
    }

    /*****************
     Command execution
     *****************/
    @Test
    public void testCreatingImageIfNotSupplied() {
        uploadImageCommand.executeCommand();

        // Make sure an image is created.
        verify(uploadImageCommand, times(1)).createImage();

        // Make sure that a transfer session won't start yet.
        verify(uploadImageCommand, never()).handleImageIsReadyForUpload(any());
    }

    @Test
    public void testNotCreatingImageIfSupplied() {
        Guid suppliedImageId = Guid.newGuid();
        doNothing().when(uploadImageCommand).handleImageIsReadyForUpload(suppliedImageId);
        uploadImageCommand.getParameters().setImageId(suppliedImageId);
        uploadImageCommand.executeCommand();

        // Make sure no image is created if an image Guid is supplied.
        verify(uploadImageCommand, never()).createImage();

        // Make sure that a transfer session will start.
        verify(uploadImageCommand, times(1)).handleImageIsReadyForUpload(suppliedImageId);
    }

    /*********************************
     * Handling ready image to upload
     ********************************/
    @Test
    public void testParamsUpdated() {
        DiskImage readyImage = initReadyImageForUpload();

        uploadImageCommand.handleImageIsReadyForUpload(readyImage.getImageId());

        assertTrue(uploadImageCommand.getParameters().getImageId().equals(readyImage.getImageId()));
        assertTrue(uploadImageCommand.getParameters().getStorageDomainId().equals(readyImage.getStorageIds().get(0)));
        assertTrue(uploadImageCommand.getParameters().getUploadSize() == readyImage.getSize());
    }

    @Test
    public void testCommandPersistedWithParamUpdates() {
        DiskImage readyImage = initReadyImageForUpload();

        UploadDiskImageParameters params = mock(UploadDiskImageParameters.class);
        doReturn(params).when(uploadImageCommand).getParameters();

        uploadImageCommand.handleImageIsReadyForUpload(readyImage.getImageId());

        // Verify that persistCommand is being called after each of the params changes.
        InOrder inOrder = inOrder(params, uploadImageCommand);
        inOrder.verify(params).setStorageDomainId(any());
        inOrder.verify(uploadImageCommand).persistCommand(any(), anyBoolean());

        inOrder = inOrder(params, uploadImageCommand);
        inOrder.verify(params).setImageId(any());
        inOrder.verify(uploadImageCommand).persistCommand(any(), anyBoolean());

        inOrder = inOrder(params, uploadImageCommand);
        inOrder.verify(params).setUploadSize(anyLong());
        inOrder.verify(uploadImageCommand).persistCommand(any(), anyBoolean());
    }
}
