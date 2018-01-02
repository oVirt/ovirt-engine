package org.ovirt.engine.core.bll.storage.disk.image;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
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

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageTransferDao;

@RunWith(MockitoJUnitRunner.class)
public class TransferImageCommandTest extends BaseCommandTest{

    @Mock
    ImageTransferDao imageTransferDao;

    @Mock
    DiskDao diskDao;

    @Mock
    ImageTransferUpdater imageTransferUpdater;

    @Spy
    @InjectMocks
    protected TransferImageCommand<? extends TransferImageParameters> transferImageCommand = spyCommand();

    @Before
    public void setUp() {
        initCommand();
    }

    protected TransferDiskImageCommand spyCommand() {
        return new TransferDiskImageCommand(new TransferDiskImageParameters(), null);
    }

    protected void initCommand() {
        doNothing().when(transferImageCommand).createImage();
        doNothing().when(transferImageCommand).persistCommand(any(), anyBoolean());
        doNothing().when(transferImageCommand).lockImage();
        doReturn(true).when(transferImageCommand).startImageTransferSession();
        doReturn(null).when(imageTransferUpdater).updateEntity(any(), any(), anyBoolean());
    }

    protected void initSuppliedImage(TransferImageCommand<? extends TransferImageParameters> command) {
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

    /*****************
     Command execution
     *****************/
    @Test
    public void testCreatingImageIfNotSupplied() {
        transferImageCommand.executeCommand();

        // Make sure an image is created.
        verify(transferImageCommand, times(1)).createImage();

        // Make sure that a transfer session won't start yet.
        verify(transferImageCommand, never()).handleImageIsReadyForTransfer();
    }

    @Test
    public void testNotCreatingImageIfSupplied() {
        Guid suppliedImageId = Guid.newGuid();
        doNothing().when(transferImageCommand).handleImageIsReadyForTransfer();
        transferImageCommand.getParameters().setImageId(suppliedImageId);
        transferImageCommand.executeCommand();

        // Make sure no image is created if an image Guid is supplied.
        verify(transferImageCommand, never()).createImage();

        // Make sure that a transfer session will start.
        verify(transferImageCommand, times(1)).handleImageIsReadyForTransfer();
    }

    @Test
    public void testFailsDownloadExecutionWithoutImage() {
        transferImageCommand.getParameters().setTransferType(TransferType.Download);
        transferImageCommand.executeCommand();

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

        assertTrue(transferImageCommand.getParameters().getStorageDomainId().equals(readyImage.getStorageIds().get(0)));
        assertTrue(transferImageCommand.getParameters().getTransferSize() == readyImage.getActualSizeInBytes());
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
     * Other
     *********/
    @Test
    public void testUploadIsDefaultTransferType() {
        assertEquals(transferImageCommand.getParameters().getTransferType(), TransferType.Upload);
    }
}
