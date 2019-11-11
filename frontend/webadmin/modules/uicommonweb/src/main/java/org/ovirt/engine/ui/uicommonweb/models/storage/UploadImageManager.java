package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

public class UploadImageManager {

    private static final Logger log = Logger.getLogger(UploadImageManager.class.getName());

    private static UploadImageManager instance;

    public static UploadImageManager getInstance() {
        if (instance == null) {
            instance = new UploadImageManager();
        }
        return instance;
    }

    private static UIConstants constants = ConstantsManager.getInstance().getConstants();

    private Set<UploadImageHandler> uploadImageHandlers = new HashSet<>();

    public UploadImageManager() {
        setWindowClosingHandler();
    }

    /**
     * Start a new upload and register to uploads list.
     *
     * @param fileUploadElement
     *            the file upload html element
     * @param transferDiskImageParameters
     *            transfer parameters
     */
    public void startUpload(Element fileUploadElement, TransferDiskImageParameters transferDiskImageParameters,
            String proxyLocation) {
        startUpload(fileUploadElement,
                transferDiskImageParameters,
                proxyLocation,
                0,
                transferDiskImageParameters.getTransferSize());
    }

    /**
     * Start a new upload by a specified range.
     *
     * @param fileUploadElement
     *            the file upload html element
     * @param transferDiskImageParameters
     *            transfer parameters
     * @param startByte
     *            start offset
     * @param endByte
     *            end offset
     */
    public void startUpload(Element fileUploadElement, TransferDiskImageParameters transferDiskImageParameters,
            String proxyLocation, long startByte, long endByte) {
        UploadImageHandler uploadImageHandler =
                createUploadImageHandler(fileUploadElement, proxyLocation, transferDiskImageParameters.getStorageDomainId());
        uploadImageHandlers.add(uploadImageHandler);
        uploadImageHandler.start(transferDiskImageParameters, startByte, endByte);
    }

    /**
     * Resume an existing upload.
     *
     * @param fileUploadElement
     *            the file upload html element
     * @param transferImageStatusParameters
     *            transfer parameters
     * @param asyncQuery
     *            callback to invoke
     */
    public void resumeUpload(Element fileUploadElement, TransferImageStatusParameters transferImageStatusParameters,
            String proxyLocation, AsyncQuery<String> asyncQuery) {
        Optional<UploadImageHandler> uploadImageHandlerOptional =
            getUploadImageHandler(transferImageStatusParameters.getDiskId());
        UploadImageHandler uploadImageHandler =
            uploadImageHandlerOptional.orElseGet(
                    () -> createUploadImageHandler(fileUploadElement, proxyLocation, transferImageStatusParameters.getStorageDomainId())
            );
        uploadImageHandler.resetUploadState();
        uploadImageHandlers.add(uploadImageHandler);
        uploadImageHandler.resume(transferImageStatusParameters, asyncQuery);
    }

    /**
     * Returns whether an UploadImageHandler exists by a specified diskId
     *
     * @param diskId
     *            upload disk ID
     * @return whether an UploadImageHandler exists
     */
    public boolean isUploadImageHandlerExists(Guid diskId) {
        Optional<UploadImageHandler> uploadImageHandlerOptional = getUploadImageHandler(diskId);
        return uploadImageHandlerOptional.isPresent();
    }

    private UploadImageHandler createUploadImageHandler(Element fileUploadElement, String proxyLocation, Guid storageDomainId) {
        final UploadImageHandler uploadImageHandler = new UploadImageHandler(fileUploadElement, proxyLocation);
        uploadImageHandler.setStorageDomainId(storageDomainId);
        uploadImageHandler.getUploadFinishedEvent().addListener((ev, sender, args) -> {
            uploadImageHandlers.remove(uploadImageHandler);
            log.info("Removed upload handler for disk: " //$NON-NLS-1$
                    + uploadImageHandler.getDiskId().toString());
        });
        return uploadImageHandler;
    }

    private Optional<UploadImageHandler> getUploadImageHandler(Guid diskId) {
        return uploadImageHandlers.stream().filter(
            uploadImageHandler -> diskId.equals(uploadImageHandler.getDiskId())).findFirst();
    }

    /**
     * Ensures that a window closing warning is present when uploads are in progress.
     */
    private void setWindowClosingHandler() {
        Window.addWindowClosingHandler(event -> {
            boolean isAnyPolling = uploadImageHandlers.stream().anyMatch(
                    UploadImageHandler::isContinuePolling);

            if (isAnyPolling) {
                // If the window is closed, uploads will time out and pause
                event.setMessage(constants.uploadImageLeaveWindowPopupWarning());
            }
        });
    }
}
