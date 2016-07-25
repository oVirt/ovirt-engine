package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.UploadDiskImageParameters;
import org.ovirt.engine.core.common.action.UploadImageStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferUpdates;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ReadOnlyDiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;

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
 *   and vdsm-imaged, creates a signed ticket for us to send to the proxy, and
 *   updates the entity phase to TRANSFERRING.
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
public class UploadImageModel extends Model implements ICommandTarget {

    private static final Logger log = LoggerFactory.getLogger(UploadImageModel.class);

    private static final int POLLING_DELAY_MS = 4000;
    private static final int MAX_FAILED_POLL_ATTEMPTS = 3;

    private static UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static UIMessages messages = ConstantsManager.getInstance().getMessages();
    private static EventDefinition selectedItemChangedEventDefinition;

    static {
        selectedItemChangedEventDefinition = new EventDefinition("SelectedItemChanged", ListModel.class); //$NON-NLS-1$
    }

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

    private List<EntityModel> entities;
    private EntityModel<Boolean> imageSourceLocalEnabled;
    private EntityModel<String> imagePath;
    private EntityModel<String> imageUri;
    private ListModel<VolumeFormat> volumeFormat;
    private AbstractDiskModel diskModel;

    private UICommand okCommand;
    private UICommand cancelCommand;

    private boolean isResumeUpload;
    private Guid commandId;
    private String transferToken;
    private String imageId;
    private String vdsId;

    private Element imageFileUploadElement;
    private boolean browserSupportsUpload;
    private int failedPollAttempts;
    private int failedFinalizationAttempts;

    // The following are shared by the Java and JS (JSNI) code
    private long bytesSent;
    private String progressStr;
    private String errorMessage;
    private UploadState uploadState;
    private boolean continuePolling;


    public List<EntityModel> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityModel> entities) {
        if (entities != this.entities) {
            this.entities = entities;
            onPropertyChanged(new PropertyChangedEventArgs("UploadImageEntities")); //$NON-NLS-1$
        }
    }

    public EntityModel<Boolean> getImageSourceLocalEnabled() {
        return imageSourceLocalEnabled;
    }

    public void setImageSourceLocalEnabled(EntityModel<Boolean> imageSourceLocalEnabled) {
        this.imageSourceLocalEnabled = imageSourceLocalEnabled;
    }

    public EntityModel<String> getImagePath() {
        return imagePath;
    }

    public void setImagePath(EntityModel<String> imagePath) {
        this.imagePath = imagePath;
    }

    public EntityModel<String> getImageUri() {
        return imageUri;
    }

    public void setImageUri(EntityModel<String> imageUri) {
        this.imageUri = imageUri;
    }

    public ListModel<VolumeFormat> getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(ListModel<VolumeFormat> volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public AbstractDiskModel getDiskModel() {
        return diskModel;
    }

    public void setDiskModel(AbstractDiskModel diskModel) {
        this.diskModel = diskModel;
    }


    public UICommand getOkCommand() {
        return okCommand;
    }

    public void setOkCommand(UICommand okCommand) {
        this.okCommand = okCommand;
    }

    @Override
    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }


    public boolean getIsResumeUpload() {
        return isResumeUpload;
    }

    public void setIsResumeUpload(boolean isResumeUpload) {
        this.isResumeUpload = isResumeUpload;
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public String getTransferToken() {
        return transferToken;
    }

    public void setTransferToken(String transferToken) {
        this.transferToken = transferToken;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getVdsId() {
        return vdsId;
    }

    public void setVdsId(String vdsId) {
        this.vdsId = vdsId;
    }


    public Element getImageFileUploadElement() {
        return imageFileUploadElement;
    }

    public void setImageFileUploadElement(Element imageFileUploadElement) {
        this.imageFileUploadElement = imageFileUploadElement;
    }

    public boolean getBrowserSupportsUpload() {
        return browserSupportsUpload;
    }

    public void setBrowserSupportsUpload(boolean browserSupportsUpload) {
        this.browserSupportsUpload = browserSupportsUpload;
    }


    // The following are shared by the Java and JS (JSNI) code

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

    public String getProgressStr() {
        return progressStr;
    }

    private void setProgressStr(String progressStr) {
        this.progressStr = progressStr;
        onPropertyChanged(new PropertyChangedEventArgs("Progress") ); //$NON-NLS-1$
    }

    protected String getErrorMessage() {
        return errorMessage;
    }

    protected void setErrorMessage(String message) {
        errorMessage = message;
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

    private boolean getContinuePolling() {
        return continuePolling;
    }

    private void setContinuePolling(boolean value) {
        continuePolling = value;
    }


    public UploadImageModel(final Guid limitToStorageDomainId, final DiskImage resumeUploadDisk) {
        if (resumeUploadDisk == null) {
            setDiskModel(new NewDiskModel() {
                @Override
                public void initialize() {
                    super.initialize();

                    getStorageDomain().setIsChangeable(limitToStorageDomainId == null);
                    getDataCenter().setIsChangeable(limitToStorageDomainId == null);
                    getHost().setIsChangeable(false);
                    getStorageType().setIsChangeable(false);
                }

                @Override
                protected void updateStorageDomains(final StoragePool datacenter) {
                    if (limitToStorageDomainId == null) {
                        super.updateStorageDomains(datacenter);
                    } else {
                        AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                DiskModel diskModel = (DiskModel) target;
                                StorageDomain storageDomain = (StorageDomain) returnValue;
                                diskModel.getStorageDomain().setSelectedItem(storageDomain);
                            }
                        }), limitToStorageDomainId);
                    }
                }

                @Override
                public int getMinimumDiskSize() {
                    return (int) ((getImageSize() + SizeConverter.BYTES_IN_GB - 1) / SizeConverter.BYTES_IN_GB);
                }
            });
        } else {
            setDiskModel(new ReadOnlyDiskModel());
            getDiskModel().setDisk(resumeUploadDisk);
            getDiskModel().getDiskInterface().setIsAvailable(false);
            setIsResumeUpload(true);
        }

        setImageSourceLocalEnabled(new EntityModel<Boolean>());
        getImageSourceLocalEnabled().setEntity(true);
        getImageSourceLocalEnabled().getEntityChangedEvent().addListener(this);

        setImagePath(new EntityModel<String>());
        setImageUri(new EntityModel<String>());
        setVolumeFormat(new ListModel<VolumeFormat>());
        getVolumeFormat().setItems(AsyncDataProvider.getInstance().getVolumeFormats());

        setUploadState(UploadState.NEW);
        setProgressStr(""); //$NON-NLS-1$
        setErrorMessage(null);
        setBrowserSupportsUpload(browserSupportsUploadAPIs());

        setOkCommand(UICommand.createDefaultOkUiCommand("Ok", this)); //$NON-NLS-1$
        getOkCommand().setIsExecutionAllowed(true);
        getCommands().add(getOkCommand());

        getDiskModel().getStorageDomain().getSelectedItemChangedEvent().addListener(this);
        getDiskModel().getVolumeType().setIsAvailable(false);
    }

    @Override
    public void initialize() {
        getDiskModel().initialize();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (getOkCommand().equals(command)) {
            onUpload();
        }
    }

    public void onUpload() {
        if (flush()) {
            if (getProgress() != null) {
                return;
            }

            if (!isResumeUpload) {
                initiateNewUpload();
            } else {
                initiateResumeUpload();
            }
        }
    }

    public boolean flush() {
        if (validate()) {
            diskModel.flush();
            DiskImage diskImage = (DiskImage) getDiskModel().getDisk();
            diskImage.setVolumeFormat(getVolumeFormat().getSelectedItem());
            diskImage.setActualSizeInBytes(getImageSize());
            diskImage.setVolumeType(AsyncDataProvider.getInstance().getVolumeType(
                    getVolumeFormat().getSelectedItem(),
                    getDiskModel().getStorageDomain().getSelectedItem().getStorageType()));
            return true;
        } else {
            setIsValid(false);
        }
        return false;
    }

    public boolean validate() {
        boolean uploadImageIsValid;

        if (getImageSourceLocalEnabled().getEntity()) {
            getImagePath().validateEntity(new IValidation[] { new IValidation() {
                @Override
                public ValidationResult validate(Object value) {
                    ValidationResult result = new ValidationResult();
                    if (value == null || StringHelper.isNullOrEmpty((String) value)) {
                        result.setSuccess(false);
                        result.getReasons().add(ConstantsManager.getInstance().getConstants().emptyImagePath());
                    }
                    return result;
                }
            } });
            uploadImageIsValid = getImagePath().getIsValid();
            getInvalidityReasons().addAll(getImagePath().getInvalidityReasons());
        } else {
            // TODO remote/download
            uploadImageIsValid = false;
        }

        return uploadImageIsValid && diskModel.validate();
    }


    private void initiateNewUpload() {
        startProgress(null);
        setProgressStr("Initiating new upload"); //$NON-NLS-1$

        final UploadDiskImageParameters parameters = createInitParams();
        Frontend.getInstance().runAction(VdcActionType.UploadDiskImage, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        UploadImageModel model = (UploadImageModel) result.getState();

                        if (result.getReturnValue().getSucceeded()) {
                            setCommandId((Guid) result.getReturnValue().getActionReturnValue());
                            setBytesSent(0);
                            startStatusPolling();

                            // The dialog will be closed, but the model's upload code will continue in the background
                            model.stopProgress();
                            model.getCancelCommand().execute();
                        } else {
                            setProgressStr(messages.uploadImageFailedToStartMessage(result.getReturnValue().getDescription()));
                            model.stopProgress();
                        }
                    }
                }, this);
    }

    private UploadDiskImageParameters createInitParams() {
        Disk newDisk = diskModel.getDisk();
        AddDiskParameters diskParameters = new AddDiskParameters(newDisk);

        if (diskModel.getDiskStorageType().getEntity() == DiskStorageType.IMAGE ||
                diskModel.getDiskStorageType().getEntity() == DiskStorageType.CINDER) {
            diskParameters.setStorageDomainId(getDiskModel().getStorageDomain().getSelectedItem().getId());
        }

        UploadDiskImageParameters parameters = new UploadDiskImageParameters(
                diskParameters.getStorageDomainId(),
                AsyncDataProvider.getInstance().getUploadImageUiInactivityTimeoutInSeconds(),
                diskParameters);
        parameters.setUploadSize(getImageSize());

        return parameters;
    }

    private void initiateResumeUpload() {
        startProgress(null);
        setProgressStr("Resuming upload"); //$NON-NLS-1$

        final UploadImageStatusParameters parameters = new UploadImageStatusParameters();
        parameters.setDiskId(getDiskModel().getDisk().getId());

        Frontend.getInstance().runAction(VdcActionType.UploadImageStatus, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        initiateResumeUploadCheckStatus(result);
                    }
                }, this);
    }

    private void initiateResumeUploadCheckStatus(FrontendActionAsyncResult result) {
        UploadImageModel model = (UploadImageModel) result.getState();

        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
            ImageTransfer rv = result.getReturnValue().getActionReturnValue();
            if (rv.getBytesTotal() != getImageSize()) {
                setProgressStr(messages.uploadImageFailedToResumeSizeMessage(rv.getBytesTotal(), getImageSize()));
                model.stopProgress();
                return;
            }

            // Resumable uploads already have a command running on engine, so get its id and resume it.
            ImageTransferUpdates updates = new ImageTransferUpdates();
            updates.setPhase(ImageTransferPhase.RESUMING);

            final UploadImageStatusParameters parameters = new UploadImageStatusParameters(rv.getId());
            parameters.setUpdates(updates);

            Frontend.getInstance().runAction(VdcActionType.UploadImageStatus, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {
                            initiateResumeUploadStartTransfer(result);
                        }

                    }, model);
        } else {
            setProgressStr(messages.uploadImageFailedToResumeMessage(result.getReturnValue().getDescription()));
            model.stopProgress();
        }
    }

    private void initiateResumeUploadStartTransfer(FrontendActionAsyncResult result) {
        UploadImageModel model = (UploadImageModel) result.getState();

        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
            ImageTransfer rv = result.getReturnValue().getActionReturnValue();
            setCommandId(rv.getId());
            setBytesSent(rv.getBytesSent());
            startStatusPolling();

            // The dialog will be closed, but the model's upload code will continue in the background.
            model.stopProgress();
            // For debugging, removing the following line will keep the dialog open so that status set
            // by the various setProgressStr() calls will be visible.
            model.getCancelCommand().execute();
        } else {
            setProgressStr(messages.uploadImageFailedToResumeMessage(result.getReturnValue().getDescription()));
            model.stopProgress();
        }
    }


    private void startStatusPolling() {
        setContinuePolling(true);
        manageWindowClosingHandler(true);
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                log.info("Polling for status"); //$NON-NLS-1$
                UploadImageStatusParameters statusParameters = new UploadImageStatusParameters(getCommandId());

                // TODO: temp updates from UI until updates from VDSM are implemented
                ImageTransferUpdates updates = new ImageTransferUpdates();
                updates.setBytesSent(getBytesSent());
                updates.setMessage(getMessage() != null ? getMessage() : getProgressStr());
                statusParameters.setUpdates(updates);

                Frontend.getInstance().runAction(VdcActionType.UploadImageStatus, statusParameters,
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {
                                respondToPollStatus(result);
                            }
                        });
                if (!getContinuePolling()) {
                    manageWindowClosingHandler(false);
                }
                return getContinuePolling();
            }
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
                    setVdsId(rv.getVdsId().toString());
                    setImageId(rv.getDiskId().toString());
                    setTransferToken(rv.getImagedTicketId().toString());
                    String proxyURI = rv.getProxyUri();
                    String signedTicket = rv.getSignedTicket();

                    int chunkSizeKB = AsyncDataProvider.getInstance().getUploadImageChunkSizeKB();
                    int xhrTimeoutSec = AsyncDataProvider.getInstance().getUploadImageXhrTimeoutInSeconds();
                    int xhrRetryIntervalSec = AsyncDataProvider.getInstance().getUploadImageXhrRetryIntervalInSeconds();
                    int maxRetries = AsyncDataProvider.getInstance().getUploadImageXhrMaxRetries();

                    // Start upload task
                    setUploadState(UploadState.INITIALIZING);
                    setProgressStr("Uploading from byte " + getBytesSent()); //$NON-NLS-1$
                    startUpload(getImageFileUploadElement(), proxyURI,
                            getTransferToken(), getBytesSent(), signedTicket,
                            chunkSizeKB, xhrTimeoutSec, xhrRetryIntervalSec, maxRetries);
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
            case CANCELLED:
            case FINALIZING_SUCCESS:
            case FINALIZING_FAILURE:
            case FINISHED_SUCCESS:
            case FINISHED_FAILURE:
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

    void pollingFailed() {
        // Not sure what happened to the backend; we'll try a few times and then
        // stop polling.  If the job is running on the backend, it will then pause.
        if (++failedPollAttempts >= MAX_FAILED_POLL_ATTEMPTS) {
            log.error("Polling failed, stopping model execution"); //$NON-NLS-1$
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

        ImageTransferUpdates updates = new ImageTransferUpdates();
        UploadImageStatusParameters statusParameters = new UploadImageStatusParameters(getCommandId(), updates);

        if (getUploadState() == UploadState.SUCCESS) {
            setProgressStr("Finalizing success..."); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.FINALIZING_SUCCESS);
        }
        else if (getUploadState() == UploadState.CLIENT_ERROR) {
            setProgressStr("Pausing due to client error"); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.PAUSED_SYSTEM);
        }
        else {
            setProgressStr("Finalizing failure..."); //$NON-NLS-1$
            statusParameters.getUpdates().setPhase(ImageTransferPhase.FINALIZING_FAILURE);
        }

        log.info("Updating status to {}", statusParameters.getUpdates().getPhase()); //$NON-NLS-1$
        Frontend.getInstance().runAction(VdcActionType.UploadImageStatus, statusParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        if (!result.getReturnValue().getSucceeded()) {
                            if (++failedFinalizationAttempts < MAX_FAILED_POLL_ATTEMPTS) {
                                finalizeImageUpload();
                            } else {
                                setContinuePolling(false);
                                setProgressStr("Failed to update upload status on engine"); //$NON-NLS-1$
                            }
                        }
                    }
                });
    }


    /**
     * Ensures that a window closing warning is present when uploads are in progress.
     *
     * @param add True if starting an upload in this window; false if the window is no longer active in the upload process
     */
    private void manageWindowClosingHandler(boolean add) {
        int uploadCount = adjustTotalUploadCount(add ? 1 : -1);
        if (uploadCount == 1) {
            HandlerRegistration handlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
                @Override
                public void onWindowClosing(Window.ClosingEvent event) {
                    // If the window is closed, uploads will time out and pause
                    event.setMessage(constants.uploadImageLeaveWindowPopupWarning());
                }
            });
            storeHandlerReference(handlerRegistration);
        } else if (uploadCount == 0) {
            HandlerRegistration handlerRegistration = (HandlerRegistration) getHandlerReference();
            handlerRegistration.removeHandler();
        }
    }

    private native int adjustTotalUploadCount(int difference) /*-{
        if (typeof $wnd.uploadImageCount == 'undefined') {
            $wnd.uploadImageCount = difference;
        } else {
            $wnd.uploadImageCount += difference;
        }
        return $wnd.uploadImageCount;
    }-*/;

    private native void storeHandlerReference(Object handlerRegistration) /*-{
        $wnd.uploadImageHandler = handlerRegistration;
    }-*/;

    private native Object getHandlerReference() /*-{
        return $wnd.uploadImageHandler;
    }-*/;


    public static native boolean browserSupportsUploadAPIs() /*-{
        return window.File && window.FileReader && window.Blob ? true : false;
    }-*/;

    private native double getSizeOfImage(Element fileUploadElement) /*-{
        return !fileUploadElement.files.length ? 0 : fileUploadElement.files[0].size;
    }-*/;

    private long getImageSize() {
        return (long) getSizeOfImage(getImageFileUploadElement());
    }

    private void logDebug(String txt) {
        log.debug(txt);
    }

    private void logInfo(String txt) {
        log.info(txt);
    }

    private void logWarn(String txt) {
        log.warn(txt);
    }

    private void logError(String txt) {
        log.error(txt);
    }

    private native void startUpload(Element fileUploadElement, String proxyUri,
            String resourceId, double startByte, String signedTicket,
            int chunkSizeKB, int xhrTimeoutSec, int xhrRetryIntervalSec, int maxRetries) /*-{

        var bytesPerMB = 1024 * 1024;

        var self = this;
        var file;
        var startTime;
        var bytesSent;
        var bytesSentThisRequest;
        var bytesToSend;
        var xhr;
        var chunkErrorCount;

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
            DEBUG: function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::logDebug(Ljava/lang/String;)(t); },
            INFO:  function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::logInfo(Ljava/lang/String;)(t); },
            WARN:  function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::logWarn(Ljava/lang/String;)(t); },
            ERROR: function(t) { self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::logError(Ljava/lang/String;)(t); }
        };

        log.INFO("Starting upload to " + proxyUri
                + "\nWith imaged ticket: " + resourceId
                + "\nWith proxy ticket: " + signedTicket);
        setProgressStr("Transferring - init");

        doUpload(startByte);

        function doUpload(startByte) {
            log.INFO("doUpload: Starting at byte " + startByte);
            if (!fileUploadElement.files.length) {
                setUploadStateByString(UploadStates.CLIENT_ERROR);
                setErrorMessage('No file selected');
                return;
            }

            file = fileUploadElement.files[0];
            log.INFO('doUpload: Selected file: ' + file.name + ' (size: ' + file.size + ' bytes)');

            chunkErrorCount = 0;
            bytesSentThisRequest = 0;
            startTime = performance.now();
            bytesSent = startByte;
            setUploadStateByString(UploadStates.TRANSFERRING);
            sendChunk();
        }

        function sendChunk() {
            if (getUploadStateString() != UploadStates.TRANSFERRING) {
                finalizeUpload();
                return;
            }

            log.DEBUG('sendChunk: Sending from byte ' + bytesSent);
            bytesToSend = Math.min(file.size - bytesSent, chunkSizeKB * 1024);
            bytesSentThisRequest = 0;

            if (xhr == undefined) {
                log.DEBUG('sendChunk: Initializing xhr');
                xhr = new XMLHttpRequest();
                // The load event is triggered when xhr has uploaded all the data, whereas readystatechange is
                // triggered when the remote endpoint closes the connection.  We want the latter for transferring; see:
                // http://stackoverflow.com/questions/15418608/xmlhttprequest-level-2-determinate-if-upload-finished
                xhr.onreadystatechange = onStateChangeHandler;
                xhr.upload.addEventListener('progress', xhrProgress, false);
            }

            var address = proxyUri + '/' + resourceId;
            var contentRange = 'bytes ' + bytesSent + '-' + (bytesSent + bytesToSend - 1) + '/' + file.size;

            xhr.open('PUT', address);
            xhr.timeout = xhrTimeoutSec * 1000;  // Must be set after xhr.open()
            xhr.setRequestHeader('Cache-Control', 'no-cache');
            xhr.setRequestHeader('Pragma', 'no-cache');
            xhr.setRequestHeader('Content-Range', contentRange);
            xhr.setRequestHeader('Authorization', signedTicket);

            log.INFO('sendChunk: PUT ' + address + ' ' + contentRange);
            xhr.send(file.slice(bytesSent, bytesSent + bytesToSend, 'application/octet-stream'));
        }

        function onStateChangeHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200 || xhr.status == 204 || xhr.status == 206) {
                    log.DEBUG('xhrHandle: Status: ' + xhr.status
                            + ', text: ' + xhr.responseText
                            + ', response: ' + xhr.response);
                    bytesSent += getBytesFromResponse(bytesToSend);
                    if (getUploadStateString() == UploadStates.CLIENT_ERROR) {
                        finalizeUpload();
                    } else if (bytesSent < file.size) {
                        chunkErrorCount = 0;
                        setBytesSent(bytesSent);
                        sendChunk();
                    } else {
                        elapsed = (performance.now() - startTime) / 1000;
                        bytesPerSec = (elapsed > 0 ? file.size / elapsed : file.size);
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
            if (range != null) {
                // Parse the range header; the byte range x-y is inclusive.
                m = range.match(/bytes=(\d+)-(\d+)/i);
                log.DEBUG('getBytesFromResponse: ' + m);
                if (m != null) {
                    return m[2] - m[1] + 1;
                }
                log.ERROR('Invalid Range header from client');
                setErrorMessage('Transfer failed: invalid Range header received from proxy');
                setUploadStateByString(UploadStates.CLIENT_ERROR);
            }
            return bytesToSend;
        }

        function xhrProgress(e) {
            if (e.lengthComputable) {
                bytesSentThisRequest = e.loaded;
                updateProgress();
            }
            if (getUploadStateString() != UploadStates.TRANSFERRING) {
                xhr.abort();
                finalizeUpload();
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
                setErrorMessage('Transfer to proxy failed, code: ' + xhr.status + ', text: ' + xhr.responseText + ', response: ' + xhr.response);
                setUploadStateByString(UploadStates.CLIENT_ERROR);
                finalizeUpload();
            }
        }

        function updateProgress() {
            // This is mostly useful to track data within the JS; the engine will get updates through vdsm
            var bytes = bytesSent + bytesSentThisRequest;
            setBytesSent(bytes);
        }

        function finalizeUpload() {
            log.WARN('Finalizing upload with status ' + getUploadStateString());
            updateProgress();
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::finalizeImageUpload()();
        }

        function setProgressStr(txt) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::setProgressStr(Ljava/lang/String;)(txt);
        }

        function setErrorMessage(msg) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::setErrorMessage(Ljava/lang/String;)(msg);
        }

        function setBytesSent(bytes) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::setBytesSent(D)(bytes);
        }

        function getUploadStateString() {
            return self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::getUploadStateString()();
        }

        function setUploadStateByString(state) {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel::setUploadStateByString(Ljava/lang/String;)(state);
        }
    }-*/;


    /**
     * Build and display the Upload Image dialog.
     *
     * @param parent Parent model
     * @param helpTag Help tag (dependent upon location in UI)
     * @param limitToStorageDomainId Pre-selected storage domain, or null to not limit selection
     * @param resumeUploadDisk DiskImage corresponding to upload being resumed, or null for new upload
     */
    public static <T extends Model & ICommandTarget> void showUploadDialog(
            T parent,
            HelpTag helpTag,
            Guid limitToStorageDomainId,
            DiskImage resumeUploadDisk) {
        UploadImageModel model = new UploadImageModel(limitToStorageDomainId, resumeUploadDisk);
        model.setTitle(resumeUploadDisk == null
                ? ConstantsManager.getInstance().getConstants().uploadImageTitle()
                : ConstantsManager.getInstance().getConstants().uploadImageResumeTitle());
        model.setHelpTag(helpTag);
        model.setHashName("upload_disk_image"); //$NON-NLS-1$

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", parent); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);
        model.getCommands().add(cancelCommand);

        parent.setWindow(model);
        model.initialize();
    }

    /**
     * Display the cancellation dialog for Image Upload, showing the list of selected
     * items which will be cancelled upon confirmation.  The parent model must have an
     * "OnCancelUpload" UICommand handler defined, which should call onCancelUpload().
     *
     * @param parent Parent model
     * @param helptag Help tag (dependent upon location in UI)
     * @param images List of selected images
     * @param <T> Model which implements ICommandTarget
     */
    public static <T extends Model & ICommandTarget> void showCancelUploadDialog(
            T parent,
            HelpTag helptag,
            List<DiskImage> images) {
        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().uploadImageCancelTitle());
        model.setHelpTag(helptag);
        model.setHashName("cancel_upload_image"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().uploadImageCancelConfirmationMessage());
        parent.setWindow(model);

        ArrayList<String> items = new ArrayList<>();
        for (DiskImage image : images) {
            items.add(image.getDiskAlias());
        }
        model.setItems(items);

        UICommand okCommand = new UICommand("OnCancelUpload", parent); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", parent); //$NON-NLS-1$
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    public static void onCancelUpload(ConfirmationModel model, List<DiskImage> images) {
        if (model.getProgress() != null) {
            return;
        }

        model.startProgress(null);

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (DiskImage image : images) {
            ImageTransferUpdates updates = new ImageTransferUpdates();
            updates.setPhase(ImageTransferPhase.CANCELLED);
            UploadImageStatusParameters parameters = new UploadImageStatusParameters();
            parameters.setUpdates(updates);
            parameters.setDiskId(image.getId());
            list.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.UploadImageStatus, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        localModel.getCancelCommand().execute(); //parent.cancel();
                    }
                }, model);
    }

    public static void pauseUploads(List<DiskImage> images) {
                ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (DiskImage image : images) {
            ImageTransferUpdates updates = new ImageTransferUpdates();
            updates.setPhase(ImageTransferPhase.PAUSED_USER);
            UploadImageStatusParameters parameters = new UploadImageStatusParameters();
            parameters.setUpdates(updates);
            parameters.setDiskId(image.getId());
            list.add(parameters);
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.UploadImageStatus, list);
    }

    public static boolean isCancelAllowed(List<? extends Disk> disks) {
        if (disks == null || disks.isEmpty()) {
            return false;
        }
        for (Disk disk : disks) {
            if (!(disk instanceof DiskImage)
                    || disk.getImageTransferPhase() == null
                    || !disk.getImageTransferPhase().canBeCancelled()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPauseAllowed(List<? extends Disk> disks) {
        if (disks == null || disks.isEmpty()) {
            return false;
        }
        for (Disk disk : disks) {
            if (!(disk instanceof DiskImage)
                    || disk.getImageTransferPhase() == null
                    || !disk.getImageTransferPhase().canBePaused()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isResumeAllowed(List<? extends Disk> disks) {
        return disks != null
                && disks.size() == 1
                && disks.get(0) instanceof DiskImage
                && disks.get(0).getImageTransferPhase() != null
                && disks.get(0).getImageTransferPhase().canBeResumed();
    }

}
