package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class StorageDiskListModel extends SearchableListModel<StorageDomain, Object> {
    private UICommand removeCommand;

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    private UICommand privateUploadCommand;

    public UICommand getUploadCommand() {
        return privateUploadCommand;
    }

    private void setUploadCommand(UICommand value) {
        privateUploadCommand= value;
    }

    private UICommand privateCancelUploadCommand;

    public UICommand getCancelUploadCommand() {
        return privateCancelUploadCommand;
    }

    private void setCancelUploadCommand(UICommand value) {
        privateCancelUploadCommand = value;
    }

    private UICommand privatePauseUploadCommand;

    public UICommand getPauseUploadCommand() {
        return privatePauseUploadCommand;
    }

    private void setPauseUploadCommand(UICommand value) {
        privatePauseUploadCommand = value;
    }

    private UICommand privateResumeUploadCommand;

    public UICommand getResumeUploadCommand() {
        return privateResumeUploadCommand;
    }

    private void setResumeUploadCommand(UICommand value) {
        privateResumeUploadCommand = value;
    }

    public StorageDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHelpTag(HelpTag.disks);
        setHashName("disks"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setUploadCommand(new UICommand("Upload", this)); //$NON-NLS-1$
        setCancelUploadCommand(new UICommand("CancelUpload", this)); //$NON-NLS-1$
        setPauseUploadCommand(new UICommand("PauseUpload", this)); //$NON-NLS-1$
        setResumeUploadCommand(new UICommand("ResumeUpload", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
        else {
            setItems(null);
        }
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(VdcQueryType.GetAllDisksByStorageDomainId, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        StorageDiskListModel storageDiskListModel = (StorageDiskListModel) model;
                        storageDiskListModel.setItems((ArrayList) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                    }
                }));
    }

    private void updateActionAvailability() {
        ArrayList<DiskImage> disks = getSelectedItems() != null ?
                Linq.<DiskImage> cast(getSelectedItems()) : new ArrayList<DiskImage>();

        getRemoveCommand().setIsExecutionAllowed(disks.size() > 0 && isRemoveCommandAvailable(disks));
        getUploadCommand().setIsExecutionAllowed(isUploadCommandAvailable());
        getCancelUploadCommand().setIsExecutionAllowed(UploadImageModel.isCancelAllowed(disks));
        getPauseUploadCommand().setIsExecutionAllowed(UploadImageModel.isPauseAllowed(disks));
        getResumeUploadCommand().setIsExecutionAllowed(UploadImageModel.isResumeAllowed(disks));
    }

    private boolean isRemoveCommandAvailable(ArrayList<DiskImage> disks) {
        for (DiskImage disk : disks) {
            boolean isImageLocked = disk.getImageStatus() == ImageStatus.LOCKED;

            if (isImageLocked) {
                return false;
            }
        }

        return true;
    }

    private boolean isUploadCommandAvailable() {
        return getEntity() != null && Linq.isDataActiveStorageDomain(getEntity());
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveDiskModel model = new RemoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        model.setHelpTag(HelpTag.remove_disk);
        model.setHashName("remove_disk"); //$NON-NLS-1$

        model.getLatch().setIsAvailable(false);

        ArrayList<DiskModel> items = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;

            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);

            items.add(diskModel);
        }
        model.setItems(items);

        UICommand onRemoveCommand = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(onRemoveCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onRemove() {
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;
            VdcActionParametersBase parameters = new RemoveDiskParameters(disk.getId(), getEntity().getId());
            paramerterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        StorageDiskListModel localModel = (StorageDiskListModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                },
                this);
    }

    private void upload() {
        if (getWindow() != null) {
            return;
        }

        UploadImageModel.showUploadDialog(
                this,
                HelpTag.upload_disk_image_to_domain,
                getEntity().getId(),
                null);
    }

    private void resumeUpload() {
        if (getSelectedItem() == null || getWindow() != null) {
            return;
        }

        UploadImageModel.showUploadDialog(
                this,
                HelpTag.resume_upload_image_to_domain,
                getEntity().getId(),
                (DiskImage) getSelectedItem());
    }

    private void cancelUpload() {
        UploadImageModel.showCancelUploadDialog(
                this,
                HelpTag.cancel_upload_image_to_domain,
                Linq.<DiskImage> cast(getSelectedItems()));
    }

    private void onCancelUpload() {
        UploadImageModel.onCancelUpload(
                (ConfirmationModel) getWindow(),
                Linq.<DiskImage> cast(getSelectedItems()));
    }

    private void pauseUpload() {
        if (getWindow() != null) {
            return;
        }

        UploadImageModel.pauseUploads(Linq.<DiskImage> cast(getSelectedItems()));
    }


    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("Upload".equals(command.getName())) { //$NON-NLS-1$
            upload();
        }
        else if ("CancelUpload".equals(command.getName())) { //$NON-NLS-1$
            cancelUpload();
        }
        else if ("OnCancelUpload".equals(command.getName())) { //$NON-NLS-1$
            onCancelUpload();
        }
        else if ("PauseUpload".equals(command.getName())) { //$NON-NLS-1$
            pauseUpload();
        }
        else if ("ResumeUpload".equals(command.getName())) { //$NON-NLS-1$
            resumeUpload();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageDiskListModel"; //$NON-NLS-1$
    }
}
