package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ReadOnlyDiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class UploadImageModel extends Model implements ICommandTarget {

    private static final Logger log = Logger.getLogger(UploadImageModel.class.getName());
    private static UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static UIMessages messages = ConstantsManager.getInstance().getMessages();

    private EntityModel<Boolean> imageSourceLocalEnabled;
    private EntityModel<String> imagePath;
    private AbstractDiskModel diskModel;

    private UICommand okCommand;
    private UICommand cancelCommand;
    private UICommand testCommand;

    private boolean isResumeUpload;
    private Element imageFileUploadElement;
    private boolean browserSupportsUpload;
    private String proxyLocation;
    private ImageInfoModel imageInfoModel;
    private EntityModel<Response> testResponse;

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

    public UICommand getTestCommand() {
        return testCommand;
    }

    private void setTestCommand(UICommand value) {
        testCommand = value;
    }

    public boolean getIsResumeUpload() {
        return isResumeUpload;
    }

    public void setIsResumeUpload(boolean isResumeUpload) {
        this.isResumeUpload = isResumeUpload;
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

    public String getProxyLocation() {
        return proxyLocation;
    }

    public void setProxyLocation(String proxyLocation) {
        this.proxyLocation = proxyLocation;
    }

    public EntityModel<Response> getTestResponse() {
        return testResponse;
    }

    public void setTestResponse(EntityModel<Response> testResponse) {
        this.testResponse = testResponse;
    }

    public UploadImageModel(final Guid limitToStorageDomainId, final DiskImage resumeUploadDisk) {
        if (resumeUploadDisk == null) {
            setDiskModel(new NewDiskModel() {
                @Override
                public void initialize() {
                    super.initialize();

                    getStorageDomain().setIsChangeable(limitToStorageDomainId == null);
                    getDataCenter().setIsChangeable(limitToStorageDomainId == null);
                    getStorageType().setIsChangeable(false);
                    getSize().setIsChangeable(false);
                }

                @Override
                protected void updateStorageDomains(final StoragePool datacenter) {
                    if (limitToStorageDomainId == null) {
                        super.updateStorageDomains(datacenter);
                    } else {
                        AsyncDataProvider backend = AsyncDataProvider.getInstance();
                        backend.getStorageDomainById(
                                new AsyncQuery<>(storageDomain -> {
                                    getStorageDomain().setSelectedItem(storageDomain);
                                    backend.getDataCentersByStorageDomain(new AsyncQuery<>(storagePools -> {
                                        getDataCenter().setSelectedItem(Linq.firstOrNull(storagePools));
                                    }), limitToStorageDomainId);
                                }),
                                limitToStorageDomainId);
                    }
                }

                @Override
                public int getMinimumDiskSize() {
                    return getVirtualSizeInGB();
                }

                @Override
                protected boolean performUpdateHosts() {
                    return true;
                }
            });
        } else {
            setDiskModel(new ReadOnlyDiskModel() {
                @Override
                protected boolean performUpdateHosts() {
                    return true;
                }
            });
            getDiskModel().setDisk(resumeUploadDisk);
            getDiskModel().getDiskInterface().setIsAvailable(false);
            setIsResumeUpload(true);
        }

        setImageSourceLocalEnabled(new EntityModel<>());
        getImageSourceLocalEnabled().setEntity(true);
        getImageSourceLocalEnabled().getEntityChangedEvent().addListener(this);

        setImagePath(new EntityModel<>());

        setBrowserSupportsUpload(browserSupportsUploadAPIs());

        setOkCommand(UICommand.createDefaultOkUiCommand("Ok", this)); //$NON-NLS-1$
        getOkCommand().setIsExecutionAllowed(true);
        getCommands().add(getOkCommand());

        setTestCommand(new UICommand("OnTest", this)); //$NON-NLS-1$
        setTestResponse(new EntityModel<>());

        getDiskModel().getStorageDomain().getSelectedItemChangedEvent().addListener(this);
        getDiskModel().getVolumeType().setIsAvailable(false);

        getDiskModel().getHost().setIsAvailable(true);
        getDiskModel().getHost().getSelectedItemChangedEvent().addListener(this);

        imageInfoModel = new ImageInfoModel();
    }

    @Override
    public void initialize() {
        getDiskModel().initialize();
        imageInfoModel.getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!(getDiskModel() instanceof NewDiskModel)) {
                // Setting attributes is relevant only for a new transfer;
                // resume should use the existing attributes.
                return;
            }
            if (imageInfoModel.getContentType() == DiskContentType.ISO) {
                getDiskModel().getAlias().setEntity(imageInfoModel.getFileName());
                getDiskModel().getDescription().setEntity(imageInfoModel.getFileName());
            } else {
                getDiskModel().getAlias().setEntity(null);
                getDiskModel().getDescription().setEntity(null);
            }
            getDiskModel().getSize().setEntity(getVirtualSizeInGB());
        });
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (getOkCommand().equals(command)) {
            onUpload();
        } else if (getTestCommand().equals(command)) {
            onTest();
        }
    }

    public void onUpload() {

        if (flush()) {
            if (getProgress() != null) {
                return;
            }

            if (!isResumeUpload) {
                setConnection(createRequest(), false);
                initiateNewUpload();
            } else {
                initiateResumeUpload();
            }
        }
    }

    private void handleConnectionError(boolean isTest, Throwable ex) {
        if (isTest) {
            getTestResponse().getEntityChangedEvent().raise(this, EventArgs.EMPTY);
            log.severe("Connection to ovirt-imageio-image has failed:" + ex.getMessage()); //$NON-NLS-1$
        } else {
            log.info("Failed to initiate ovirt-imageio session for image uploading:" + ex.getMessage()); //$NON-NLS-1$
        }
    }

    private void setConnection(RequestBuilder requestBuilder, boolean isTest) {
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable ex) {
                    handleConnectionError(isTest, ex);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    try {
                        getTestResponse().setEntity(response);
                    } catch (IllegalArgumentException ex) {
                        handleConnectionError(isTest, ex);
                    }
                }
            });
        } catch (RequestException ex) {
            handleConnectionError(isTest, ex);
        }
    }

    private void onTest() {
        setConnection(createRequest(), true);
    }

    private RequestBuilder createRequest() {
        String url = AsyncDataProvider.getInstance().getImageioProxyUri() + "/info/"; //$NON-NLS-1$
        return new RequestBuilder(RequestBuilder.GET, url);
    }

    public boolean flush() {
        if (validate()) {
            diskModel.flush();
            DiskImage diskImage = (DiskImage) getDiskModel().getDisk();
            diskImage.setSize(getVirtualSize());
            diskImage.setActualSizeInBytes(imageInfoModel.getActualSize());
            diskImage.setVolumeFormat(diskModel.getIsIncrementalBackup().getEntity() ?
                    VolumeFormat.COW : getImageInfoModel().getFormat());
            diskImage.setVolumeType(AsyncDataProvider.getInstance().getVolumeType(
                    diskImage.getVolumeFormat(),
                    getDiskModel().getStorageDomain().getSelectedItem().getStorageType(), null, null));
            diskImage.setContentType(getImageInfoModel().getContentType());
            return true;
        } else {
            setIsValid(false);
        }
        return false;
    }

    public boolean validate() {
        boolean uploadImageIsValid;

        setIsValid(true);
        getInvalidityReasons().clear();
        getImageInfoModel().getInvalidityReasons().clear();

        if (getImageSourceLocalEnabled().getEntity()) {
            getImagePath().validateEntity(new IValidation[] { value -> {
                ValidationResult result = new ValidationResult();
                if (value == null || StringHelper.isNullOrEmpty((String) value)) {
                    result.setSuccess(false);
                    result.getReasons().add(constants.emptyImagePath());
                }
                return result;
            } });

            StorageFormatType storageFormatType = getDiskModel().getStorageDomain().getSelectedItem().getStorageFormat();
            uploadImageIsValid = getImagePath().getIsValid() && getImageInfoModel().validate(storageFormatType, imageInfoModel.getActualSize());

            getInvalidityReasons().addAll(getImagePath().getInvalidityReasons());
            getInvalidityReasons().addAll(getImageInfoModel().getInvalidityReasons());
        } else {
            // TODO remote/download
            uploadImageIsValid = false;
        }

        return uploadImageIsValid && diskModel.validate();
    }

    private void initiateNewUpload() {
        UploadImageManager.getInstance().startUpload(getImageFileUploadElement(), createInitParams(),
                getProxyLocation());

        // Close dialog
        getCancelCommand().execute();
    }

    private void initiateResumeUpload() {
        TransferImageStatusParameters parameters = new TransferImageStatusParameters();
        parameters.setDiskId(getDiskModel().getDisk().getId());
        parameters.setStorageDomainId(getDiskModel().getStorageDomain().getSelectedItem().getId());
        startProgress();
        final UploadImageModel model = this;
        UploadImageManager.getInstance().resumeUpload(getImageFileUploadElement(), parameters, getProxyLocation(),
            new AsyncQuery<>(errorMessage -> {
                model.stopProgress();
                if (errorMessage != null) {
                    model.setMessage(errorMessage);
                } else {
                    // Close dialog
                    model.getCancelCommand().execute();
                }
            }));
    }

    private void initiateSilentResumeUpload() {
        TransferImageStatusParameters parameters = new TransferImageStatusParameters();
        parameters.setDiskId(getDiskModel().getDisk().getId());
        UploadImageManager.getInstance().resumeUpload(null, parameters, getProxyLocation(),
                new AsyncQuery<>(errorMessage -> {
                    if (errorMessage != null) {
                        getLogger().error(errorMessage, null);
                    }
                }));
    }

    private TransferDiskImageParameters createInitParams() {
        Disk newDisk = diskModel.getDisk();
        AddDiskParameters diskParameters = new AddDiskParameters(newDisk);

        if (diskModel.getDiskStorageType().getEntity() == DiskStorageType.IMAGE) {
            diskParameters.setStorageDomainId(getDiskModel().getStorageDomain().getSelectedItem().getId());
        }

        TransferDiskImageParameters parameters = new TransferDiskImageParameters(
                diskParameters.getStorageDomainId(),
                diskParameters);
        parameters.setTransferSize(imageInfoModel.getActualSize());
        parameters.setVolumeFormat(imageInfoModel.getFormat());
        parameters.setVdsId(getDiskModel().getHost().getSelectedItem().getId());
        parameters.setTransferClientType(TransferClientType.TRANSFER_VIA_BROWSER);

        return parameters;
    }

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

        // Silently resume upload if handler already exists in UploadImageManager
        if (resumeUploadDisk != null &&
                UploadImageManager.getInstance().isUploadImageHandlerExists(resumeUploadDisk.getId())) {
            model.initiateSilentResumeUpload();
            return;
        }

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
     * @param disks List of selected images
     * @param <T> Model which implements ICommandTarget
     */
    public static <T extends Model & ICommandTarget> void showCancelUploadDialog(
            T parent,
            HelpTag helptag,
            List<? extends Disk> disks) {
        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().uploadImageCancelTitle());
        model.setHelpTag(helptag);
        model.setHashName("cancel_upload_image"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().uploadImageCancelConfirmationMessage());
        parent.setWindow(model);

        ArrayList<String> items = new ArrayList<>();
        for (Disk disk : disks) {
            items.add(disk.getDiskAlias());
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

    public static void onCancelUpload(ConfirmationModel model, List<? extends Disk> disks) {
        if (model.getProgress() != null) {
            return;
        }

        model.startProgress(null);

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Disk disk : disks) {
            ImageTransfer updates = new ImageTransfer();
            updates.setPhase(ImageTransferPhase.CANCELLED_USER);
            TransferImageStatusParameters parameters = new TransferImageStatusParameters();
            parameters.setUpdates(updates);
            parameters.setDiskId(disk.getId());
            list.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(ActionType.TransferImageStatus, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    localModel.getCancelCommand().execute(); //parent.cancel();
                }, model);
    }

    public static void pauseUploads(List<? extends Disk> disks) {
                ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Disk disk : disks) {
            ImageTransfer updates = new ImageTransfer();
            updates.setPhase(ImageTransferPhase.PAUSED_USER);
            TransferImageStatusParameters parameters = new TransferImageStatusParameters();
            parameters.setUpdates(updates);
            parameters.setDiskId(disk.getId());
            list.add(parameters);
        }
        Frontend.getInstance().runMultipleAction(ActionType.TransferImageStatus, list);
    }

    public static boolean isCancelAllowed(List<? extends Disk> disks) {
        if (disks == null || disks.isEmpty()) {
            return false;
        }
        for (Disk disk : disks) {
            if (!(disk instanceof DiskImage)
                    || disk.getTransferType() != TransferType.Upload
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
                    || disk.getTransferType() != TransferType.Upload
                    || disk.getImageTransferPhase() == null
                    || !disk.getImageTransferPhase().canBePaused()
                    || isImageUploadViaAPI((DiskImage) disk)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isResumeAllowed(List<? extends Disk> disks) {
        return disks != null
                && disks.size() == 1
                && disks.get(0) instanceof DiskImage
                && disks.get(0).getTransferType() == TransferType.Upload
                && disks.get(0).getImageTransferPhase() != null
                && disks.get(0).getImageTransferPhase().isPaused()
                && !isImageUploadViaAPI((DiskImage) disks.get(0));
    }

    private static boolean isImageUploadViaAPI(DiskImage diskImage) {
        return diskImage.getImageTransferPhase() == ImageTransferPhase.TRANSFERRING
                && diskImage.getImageTransferBytesTotal() == 0;
    }

    public ImageInfoModel getImageInfoModel() {
        return imageInfoModel;
    }

    private long getVirtualSize() {
        return imageInfoModel.getVirtualSize();
    }

    private int getVirtualSizeInGB() {
        return (int) Math.ceil(getVirtualSize() / Math.pow(1024, 3));
    }

    public static native boolean browserSupportsUploadAPIs() /*-{
        return window.File != null && window.FileReader != null && window.Blob != null;
    }-*/;
}
