package org.ovirt.engine.ui.uicommonweb.models.storage;

import static org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel.messages;

import java.util.logging.Logger;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;

/**
 * The Java and JavaScript (JSNI) code to perform an image upload lives here.
 *
 * The general upload flow is as follows:
 * - The view calls UploadImageModel.onUpload()
 * - onUpload() does some initialization and runs Upload[Disk]ImageCommand
 * - Upload[Disk]ImageCommand initializes and returns to us
 * - The engine command creates an ImageUpload entity to track the execution
 *   state and returns success to the model.  Meanwhile, it uses CoCo callbacks
 *   to continue execution for the duration of the upload, using a state
 *   machine driven primarily by the entity state.
 * - The model starts polling the engine, sending UploadImageStatusCommand,
 *   and uses a callback on the response to drive its own state machine driven
 *   by the entity state.
 * - The engine command creates the disk, starts the upload session with vdsm
 *   and vdsm-imaged, and updates the entity phase to TRANSFERRING.
 * - The model responds by initiating the upload, passing control to the
 *   JavaScript code.  The progress of the JS is shared with the Java in a
 *   variable called uploadState.
 * - (Meanwhile, the model continues to poll and may receive and error from
 *    engine, a request to pause the upload, etc which would result in the
 *    model requesting the JS to stop by changing uploadState.)
 * - (Also meanwhile, the engine command continues its callbacks which will
 *    periodically poll vdsm->vdsm-imaged for the upload progress and update
 *    the entity.  The UI can then retrieve the progress by refreshing the
 *    disk list, as the disks show upload progress via a database view that
 *    joins with the image_upload entity.)
 * - The JS will start the upload, sending data to the proxy and imaged.  When
 *   complete or on error, it sets the uploadState, signalling to the model's
 *   callbacks that it can now take control of the upload.
 * - Upon JS completion, if the request was to pause, the model stops, leaving
 *   the command to poll for a resumption request.  If there was an error or
 *   successful completion, the model updates the entity state as appropriate
 *   to FINALIZING_[SUCCESS|FAILURE].
 * - The engine command will respond to a FINALIZING_* state and finalize the
 *   upload, setting the entity state to FINISHED_[SUCCESS|FAILURE].
 */
public class UploadImageHandler {

    private static final Logger log = Logger.getLogger(UploadImageHandler.class.getName());

    private static final int POLLING_DELAY_MS = 4000;
    private static final int MAX_FAILED_POLL_ATTEMPTS = 3;

    private Guid commandId;
    private long bytesSent;
    private long bytesEndOffset;
    private String progressStr;
    private String proxyLocation;
    private UploadState uploadState;
    private boolean continuePolling;
    private AuditLogType auditLogType;
    private int failedPollAttempts;
    private int failedFinalizationAttempts;
    private Guid imageTicketId;
    private Guid vdsId;
    private Guid diskId;
    private Element fileUploadElement;
    private Guid storageDomainId;

    private Event<EventArgs> uploadFinishedEvent =
            new Event<>("UploadFinished", UploadImageHandler.class); //$NON-NLS-1$

    // Note: Keep these in sync with the constants in the JavaScript below
    // as well as the stopAnyActiveJsUploadExecution() method!
    private enum UploadState {
        NEW,           // Model loaded but upload not started
        INITIALIZING,  // Upload triggered, transferring control to JS
        TRANSFERRING,  // JS active, blocks being sent to client
        SUCCESS,       // JS finished, success
        ENGINE_PAUSE,  // JS stopped due to pause by user or system sent from engine
        ENGINE_CANCEL, // JS stopped due to cancellation by engine
        CLIENT_ERROR,  // JS stopped due to error detected on the client (frontend/JS)
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setBytesSent(double bytesSent) {
        this.bytesSent = (long)bytesSent;
        setProgressStr("Sent " + bytesSent / SizeConverter.BYTES_IN_MB + "MB"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public long getBytesEndOffset() {
        return bytesEndOffset;
    }

    public void setBytesEndOffset(long bytesEndOffset) {
        this.bytesEndOffset = bytesEndOffset;
    }

    public String getProgressStr() {
        return progressStr;
    }

    private void setProgressStr(String progressStr) {
        this.progressStr = progressStr;
    }

    public boolean isContinuePolling() {
        return continuePolling;
    }

    private void setContinuePolling(boolean value) {
        continuePolling = value;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public String getUploadStateString() {
        return uploadState.name();
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }

    public void setUploadStateByString(String uploadState) {
        this.uploadState = UploadState.valueOf(uploadState);
    }

    public Guid getImageTicketId() {
        return imageTicketId;
    }

    public void setImageTicketId(Guid imageTicketId) {
        this.imageTicketId = imageTicketId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public Element getFileUploadElement() {
        return fileUploadElement;
    }

    public void setFileUploadElement(Element fileUploadElement) {
        this.fileUploadElement = fileUploadElement;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setAuditLogType(AuditLogType auditLogType) {
        this.auditLogType = auditLogType;
    }

    public Event<EventArgs> getUploadFinishedEvent() {
        return uploadFinishedEvent;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    /**
     * A single upload image handler
     *
     * @param fileUploadElement
     *            the file upload html element
     */
    public UploadImageHandler(Element fileUploadElement, String proxyLocation) {
        resetUploadState();
        setFileUploadElement(fileUploadElement);
        this.proxyLocation = proxyLocation;
    }

    public void resetUploadState() {
        setUploadState(UploadState.NEW);
    }

    /**
     * Start a new upload
     *
     * @param transferDiskImageParameters
     *            transfer parameters
     * @param startByte
     *            start offset
     * @param endByte
     *            end offset
     */
    public void start(TransferDiskImageParameters transferDiskImageParameters,
                      long startByte, long endByte) {
        Frontend.getInstance().runAction(ActionType.TransferDiskImage, transferDiskImageParameters,
                result -> {
                    if (result.getReturnValue().getSucceeded()) {
                        setCommandId(result.getReturnValue().getActionReturnValue());
                        setBytesSent(startByte);
                        setBytesEndOffset(endByte);
                        startStatusPolling();
                    } else {
                        setProgressStr(messages.uploadImageFailedToStartMessage(result.getReturnValue().getDescription()));
                    }
                }, this);
    }

    /**
     * Resume an existing upload
     *
     * @param transferImageStatusParameters
     *            transfer parameters
     * @param asyncQuery
     *            callback to invoke
     */
    public void resume(TransferImageStatusParameters transferImageStatusParameters, AsyncQuery<String> asyncQuery) {
        Frontend.getInstance().runAction(ActionType.TransferImageStatus, transferImageStatusParameters,
                this::initiateResumeUploadCheckStatus, asyncQuery);
    }

    private void startStatusPolling() {
        setContinuePolling(true);
        Scheduler.get().scheduleFixedDelay(() -> {
            log.info("Polling for status"); //$NON-NLS-1$
            TransferImageStatusParameters statusParameters = new TransferImageStatusParameters(getCommandId());
            statusParameters.setStorageDomainId(getStorageDomainId());

            ImageTransfer updates = new ImageTransfer();
            updates.setMessage(getProgressStr());
            statusParameters.setUpdates(updates);

            Frontend.getInstance().runAction(ActionType.TransferImageStatus, statusParameters,
                    this::respondToPollStatus);
            return isContinuePolling();
        }, POLLING_DELAY_MS);
    }

    private void respondToPollStatus(FrontendActionAsyncResult result) {
        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
            ImageTransfer rv = result.getReturnValue().getActionReturnValue();
            log.info("Upload phase: " + rv.getPhase().toString()); //$NON-NLS-1$

            switch (rv.getPhase()) {
                case UNKNOWN:
                    // The job may have failed and removed the entity
                    pollingFailed();
                    return;

                case INITIALIZING:
                case RESUMING:
                    break;

                case TRANSFERRING:
                    if (getUploadState() == UploadState.NEW) {
                        setVdsId(rv.getVdsId());
                        setDiskId(rv.getDiskId());
                        setImageTicketId(rv.getImagedTicketId());
                        String proxyURI = rv.getProxyUri();

                        int chunkSizeKB = AsyncDataProvider.getInstance().getUploadImageChunkSizeKB();
                        int xhrTimeoutSec = AsyncDataProvider.getInstance().getUploadImageXhrTimeoutInSeconds();
                        int xhrRetryIntervalSec = AsyncDataProvider.getInstance().getUploadImageXhrRetryIntervalInSeconds();
                        int maxRetries = AsyncDataProvider.getInstance().getUploadImageXhrMaxRetries();

                        // Start upload task
                        setUploadState(UploadState.INITIALIZING);
                        setProgressStr("Uploading from byte " + getBytesSent()); //$NON-NLS-1$
                        startUpload(getFileUploadElement(), proxyURI, getImageTicketId().toString(), getBytesSent(),
                                getBytesEndOffset(), chunkSizeKB, xhrTimeoutSec, xhrRetryIntervalSec, maxRetries);
                    }
                    break;

                case PAUSED_USER:
                case PAUSED_SYSTEM:
                    setContinuePolling(false);
                    setUploadState(UploadState.ENGINE_PAUSE);
                    break;

                // The frontend may not receive these; the backend code iterates over the cancelled and
                // finalizing states, and the image transfer entity is removed upon upload completion.
                // In this case, the default case is reached which does largely the same thing.
                case CANCELLED_SYSTEM:
                case CANCELLED_USER:
                case FINALIZING_SUCCESS:
                case FINALIZING_FAILURE:
                case FINALIZING_CLEANUP:
                case FINISHED_SUCCESS:
                case FINISHED_FAILURE:
                case FINISHED_CLEANUP:
                    log.info("Upload task terminating"); //$NON-NLS-1$
                    setContinuePolling(false);
                    stopJsUpload(UploadState.ENGINE_CANCEL);
                    break;

                default:
                    log.info("Unknown upload status from backend, job is likely complete"); //$NON-NLS-1$
                    setContinuePolling(false);
                    stopJsUpload(UploadState.CLIENT_ERROR);
                    break;
            }

            failedPollAttempts = 0;
        } else {
            log.info("No poll result for upload status"); //$NON-NLS-1$
            pollingFailed();
        }
    }

    private void pollingFailed() {
        // Not sure what happened to the backend; we'll try a few times and then
        // stop polling.  If the job is running on the backend, it will then pause.
        if (++failedPollAttempts >= MAX_FAILED_POLL_ATTEMPTS) {
            log.severe("Polling failed, stopping model execution"); //$NON-NLS-1$
            setContinuePolling(false);
            stopJsUpload(UploadState.CLIENT_ERROR);
        }
    }

    /**
     * Stop execution of the JavaScript upload routine if it is active.  If it
     * isn't active, the upload state used to control the JS flow is untouched.
     */
    private void stopJsUpload(UploadState newUploadState) {
        switch (getUploadState()) {
            case NEW:
            case INITIALIZING:
            case TRANSFERRING:
                setUploadState(newUploadState);
                break;
            default:
                break;
        }
    }

    private void finalizeImageUpload() {
        if (getUploadState() == UploadState.ENGINE_PAUSE) {
            log.info("Upload paused; stopping model execution"); //$NON-NLS-1$
            return;
        }

        ImageTransfer updates = new ImageTransfer();
        TransferImageStatusParameters statusParameters = new TransferImageStatusParameters(getCommandId(), updates);
        statusParameters.setStorageDomainId(getStorageDomainId());

        if (getUploadState() == UploadState.SUCCESS) {
            setProgressStr("Finalizing success..."); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.FINALIZING_SUCCESS);
            raiseUploadFinishedEvent(ImageTransferPhase.FINALIZING_SUCCESS);
        } else if (getUploadState() == UploadState.CLIENT_ERROR) {
            setProgressStr("Pausing due to client error"); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.PAUSED_SYSTEM);
            statusParameters.setDiskId(getDiskId());
            statusParameters.setAuditLogType(auditLogType);
            statusParameters.setProxyLocation(proxyLocation);
        } else {
            setProgressStr("Finalizing failure..."); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.FINALIZING_FAILURE);
            raiseUploadFinishedEvent(ImageTransferPhase.FINALIZING_FAILURE);
        }

        log.info("Updating status to " + statusParameters.getUpdates().getPhase()); //$NON-NLS-1$
        Frontend.getInstance().runAction(ActionType.TransferImageStatus, statusParameters,
                result -> {
                    if (!result.getReturnValue().getSucceeded()) {
                        if (++failedFinalizationAttempts < MAX_FAILED_POLL_ATTEMPTS) {
                            finalizeImageUpload();
                        } else {
                            setContinuePolling(false);
                            setProgressStr("Failed to update upload status on engine"); //$NON-NLS-1$
                        }
                    }
                });
    }

    private void raiseUploadFinishedEvent(ImageTransferPhase phase) {
        uploadFinishedEvent.raise(this, new ValueEventArgs<>(phase));
    }

    private void logDebug(String txt) {
        log.fine(txt);
    }

    private void logInfo(String txt) {
        log.info(txt);
    }

    private void logWarn(String txt) {
        log.warning(txt);
    }

    private void logError(String txt) {
        log.severe(txt);
    }

    private native void startUpload(Element fileUploadElement, String proxyUri, String resourceId, double startByte,
                                    double endByte, int chunkSizeKB, int xhrTimeoutSec, int xhrRetryIntervalSec,
                                    int maxRetries) /*-{

        var bytesPerMB = 1024 * 1024;

        var self = this;
        var file;
        var startTime;
        var bytesSent;
        var bytesSentThisRequest;
        var bytesToSend;
        var xhr;
        var chunkErrorCount;
        var fileName;

        var UploadStates = {
            NEW: "NEW",
            INITIALIZING: "INITIALIZING",
            TRANSFERRING: "TRANSFERRING",
            SUCCESS: "SUCCESS",
            ENGINE_PAUSE: "ENGINE_PAUSE",
            ENGINE_CANCEL: "ENGINE_CANCEL",
            CLIENT_ERROR: "CLIENT_ERROR"
        };

        var log = {
            DEBUG: function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::logDebug(Ljava/lang/String;)(t); },
            INFO:  function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::logInfo(Ljava/lang/String;)(t); },
            WARN:  function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::logWarn(Ljava/lang/String;)(t); },
            ERROR: function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::logError(Ljava/lang/String;)(t); }
        };

        log.INFO("Starting upload to " + proxyUri
            + "\nWith imaged ticket: " + resourceId);
        setProgressStr("Transferring - init");

        doUpload(startByte);

        function doUpload(startByte) {
            log.INFO("doUpload: Starting at byte " + startByte);
            if (!fileUploadElement.files.length) {
                setUploadStateByString(UploadStates.CLIENT_ERROR);
                setAuditLogMessage(@org.ovirt.engine.core.common.AuditLogType::UPLOAD_IMAGE_CLIENT_ERROR);
                return;
            }

            file = fileUploadElement.files[0];
            fileName = file.name;
            log.INFO('doUpload: Selected file: ' + file.name + ' (size: ' + file.size + ' bytes)');

            chunkErrorCount = 0;
            bytesSentThisRequest = 0;
            startTime = performance.now();
            bytesSent = startByte;
            setUploadStateByString(UploadStates.TRANSFERRING);
            sendChunk();
        }

        function sendChunk() {
            if (getUploadStateString() === UploadStates.NEW) {
                return;
            }
            if (getUploadStateString() !== UploadStates.TRANSFERRING) {
                finalizeUpload();
                return;
            }

            log.DEBUG('sendChunk: Sending from byte ' + bytesSent);
            bytesToSend = Math.min(endByte - bytesSent, chunkSizeKB * 1024);
            bytesSentThisRequest = 0;

            if (xhr === undefined) {
                log.DEBUG('sendChunk: Initializing xhr');
                xhr = new XMLHttpRequest();
                // The load event is triggered when xhr has uploaded all the data, whereas readystatechange is
                // triggered when the remote endpoint closes the connection.  We want the latter for transferring; see:
                // http://stackoverflow.com/questions/15418608/xmlhttprequest-level-2-determinate-if-upload-finished
                xhr.onreadystatechange = onStateChangeHandler;
                xhr.upload.addEventListener('progress', xhrProgress, false);
            }

            var address = proxyUri + '/' + resourceId;

            // On the last request we want to close the connection and flush.
            // This will close imageio backend so we can deactivate the volume
            // on block storage. On all other requests we want to avoid
            // flushing for better performance.
            if (bytesSent + bytesToSend === endByte) {
                address += '?close=y';
            } else {
                address += '?flush=n';
            }

            var contentRange = 'bytes ' + bytesSent + '-' + (bytesSent + bytesToSend - 1) + '/' + endByte;

            xhr.open('PUT', address);
            xhr.timeout = xhrTimeoutSec * 1000;  // Must be set after xhr.open()
            xhr.ontimeout = xhrTimeout;
            xhr.setRequestHeader('Cache-Control', 'no-cache');
            xhr.setRequestHeader('Pragma', 'no-cache');
            xhr.setRequestHeader('Content-Range', contentRange);

            log.INFO('sendChunk: PUT ' + address + ' ' + contentRange);
            xhr.send(file.slice(bytesSent, bytesSent + bytesToSend, 'application/octet-stream'));
        }

        function onStateChangeHandler() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200 || xhr.status === 204 || xhr.status === 206) {
                    log.DEBUG('xhrHandle: Status: ' + xhr.status
                        + ', text: ' + xhr.responseText
                        + ', response: ' + xhr.response);
                    bytesSent += getBytesFromResponse(bytesToSend);
                    if (file.size === 0) {
                        log.ERROR('Error reading selected file (' + fileName + '). ' + 'Perhaps it has been deleted?');
                        chunkErrorCount = maxRetries;
                        xhrError();
                        return;
                    }
                    if (getUploadStateString() === UploadStates.CLIENT_ERROR) {
                        finalizeUpload();
                    } else if (bytesSent < endByte) {
                        chunkErrorCount = 0;
                        setBytesSent(bytesSent);
                        sendChunk();
                    } else {
                        elapsed = (performance.now() - startTime) / 1000;
                        bytesPerSec = (elapsed > 0 ? endByte / elapsed : endByte);
                        log.INFO('xhrHandle: Finished transfer in ' + elapsed + ' seconds, '
                            + bytesPerSec / bytesPerMB + ' MB per second');
                        setUploadStateByString(UploadStates.SUCCESS);
                        finalizeUpload();
                    }
                } else {
                    log.ERROR('xhrHandle: Status: ' + xhr.status
                        + ', text: ' + xhr.responseText
                        + ', response: ' + xhr.response);
                    xhrError();
                }
            }
        }

        function getBytesFromResponse(bytesToSend) {
            range = xhr.getResponseHeader('Range');
            if (range !== null) {
                // Parse the range header; the byte range x-y is inclusive.
                m = range.match(/bytes=(\d+)-(\d+)/i);
                log.DEBUG('getBytesFromResponse: ' + m);
                if (m !== null) {
                    return m[2] - m[1] + 1;
                }
                log.ERROR('Invalid Range header from client');
                setUploadStateByString(UploadStates.CLIENT_ERROR);
            }
            return bytesToSend;
        }

        function xhrProgress(e) {
            if (e.lengthComputable) {
                bytesSentThisRequest = e.loaded;
                updateProgress();
            }
            if (getUploadStateString() !== UploadStates.TRANSFERRING) {
                xhr.abort();
            }
        }

        function xhrError() {
            log.ERROR('xhrError: ' + xhr.status + ' ' + xhr.statusText);
            if (chunkErrorCount < maxRetries) {
                chunkErrorCount++;
                log.WARN('xhrError: Retrying (attempt ' + chunkErrorCount + ' of ' + maxRetries + ')');
                bytesSentThisRequest = 0;
                updateProgress();
                setTimeout(sendChunk, xhrRetryIntervalSec * 1000);
            } else {
                log.ERROR('Transfer failed after ' + chunkErrorCount + '/' + maxRetries + ' errors');
                log.ERROR('Transfer to proxy failed, code: ' + xhr.status + ', text: ' + xhr.responseText + ', response: ' + xhr.response);
                setUploadStateByString(UploadStates.CLIENT_ERROR);
                setAuditLogMessage(@org.ovirt.engine.core.common.AuditLogType::UPLOAD_IMAGE_NETWORK_ERROR);
                finalizeUpload();
            }
        }

        function xhrTimeout(e) {
            log.ERROR('xhrTimeout: ' + xhr.status + ' ' + xhr.statusText);
            setUploadStateByString(UploadStates.CLIENT_ERROR);
            setAuditLogMessage(@org.ovirt.engine.core.common.AuditLogType::UPLOAD_IMAGE_XHR_TIMEOUT_ERROR);
            finalizeUpload();
        }

        function updateProgress() {
            // This is mostly useful to track data within the JS; the engine will get updates through vdsm
            var bytes = bytesSent + bytesSentThisRequest;
            setBytesSent(bytes);
        }

        function finalizeUpload() {
            log.WARN('Finalizing upload with status ' + getUploadStateString());
            updateProgress();
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::finalizeImageUpload()();
        }

        function setProgressStr(txt) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::setProgressStr(Ljava/lang/String;)(txt);
        }

        function setBytesSent(bytes) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::setBytesSent(D)(bytes);
        }

        function getUploadStateString() {
            return self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::getUploadStateString()();
        }

        function setUploadStateByString(state) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::setUploadStateByString(Ljava/lang/String;)(state);
        }

        function setAuditLogMessage(auditLogType) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageHandler::setAuditLogType(Lorg/ovirt/engine/core/common/AuditLogType;)(
                auditLogType
            );
        }
    }-*/;

    private void initiateResumeUploadCheckStatus(FrontendActionAsyncResult result) {
        AsyncQuery<String> asyncQuery = (AsyncQuery<String>) result.getState();

        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
            ImageTransfer rv = result.getReturnValue().getActionReturnValue();
            if (rv.getBytesTotal() != getImageSize()) {
                if (rv.getBytesTotal() == 0) {
                    // This upload was generated by the API.
                    asyncQuery.getAsyncCallback().onSuccess(
                            messages.uploadImageFailedToResumeUploadOriginatedInAPI());
                } else {
                    asyncQuery.getAsyncCallback().onSuccess(
                            messages.uploadImageFailedToResumeSizeMessage(rv.getBytesTotal(), getImageSize()));
                }
                return;
            }

            // Resumable uploads already have a command running on engine, so get its id and resume it.
            ImageTransfer updates = new ImageTransfer();
            updates.setPhase(ImageTransferPhase.RESUMING);

            TransferImageStatusParameters parameters = new TransferImageStatusParameters(rv.getId());
            parameters.setUpdates(updates);
            parameters.setStorageDomainId(getStorageDomainId());
            Frontend.getInstance().runAction(ActionType.TransferImageStatus, parameters,
                    this::initiateResumeUploadStartTransfer);
        } else {
            setProgressStr(messages.uploadImageFailedToResumeMessage(result.getReturnValue().getDescription()));
        }
        asyncQuery.getAsyncCallback().onSuccess(null);
    }

    private void initiateResumeUploadStartTransfer(FrontendActionAsyncResult result) {
        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
            ImageTransfer rv = result.getReturnValue().getActionReturnValue();
            setCommandId(rv.getId());
            setBytesSent(rv.getBytesSent());
            setBytesEndOffset(rv.getBytesTotal());
            startStatusPolling();
        } else {
            setProgressStr(messages.uploadImageFailedToResumeMessage(result.getReturnValue().getDescription()));
        }
    }

    private long getImageSize() {
        return (long) getSizeOfImage(getFileUploadElement());
    }

    private native double getSizeOfImage(Element fileUploadElement) /*-{
        return !fileUploadElement.files.length ? 0 : fileUploadElement.files[0].size;
    }-*/;
}
