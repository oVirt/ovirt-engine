package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageDiskListModel extends SearchableListModel<StorageDomain, DiskImage> {

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

    private UICommand downloadCommand;

    public UICommand getDownloadCommand() {
        return downloadCommand;
    }

    private void setDownloadCommand(UICommand value) {
        downloadCommand = value;
    }

    private UICommand moveCommand;

    public UICommand getMoveCommand() {
        return moveCommand;
    }

    private void setMoveCommand(UICommand value) {
        moveCommand = value;
    }

    private UICommand copyCommand;

    public UICommand getCopyCommand() {
        return copyCommand;
    }

    private void setCopyCommand(UICommand value) {
        copyCommand = value;
    }

    public StorageDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHelpTag(HelpTag.disks);
        setHashName("disks"); //$NON-NLS-1$

        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setUploadCommand(new UICommand("Upload", this)); //$NON-NLS-1$
        setCancelUploadCommand(new UICommand("CancelUpload", this)); //$NON-NLS-1$
        setPauseUploadCommand(new UICommand("PauseUpload", this)); //$NON-NLS-1$
        setResumeUploadCommand(new UICommand("ResumeUpload", this)); //$NON-NLS-1$
        setDownloadCommand(new UICommand("Download", this)); //$NON-NLS-1$

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
        } else {
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

        Frontend.getInstance().runQuery(QueryType.GetAllDisksByStorageDomainId, parameters, new SetItemsAsyncQuery());
    }

    private void updateActionAvailability() {
        List<DiskImage> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<>();

        getRemoveCommand().setIsExecutionAllowed(disks.size() > 0 && isRemoveCommandAvailable(disks));
        getUploadCommand().setIsExecutionAllowed(isUploadCommandAvailable());
        getCancelUploadCommand().setIsExecutionAllowed(UploadImageModel.isCancelAllowed(disks));
        getPauseUploadCommand().setIsExecutionAllowed(UploadImageModel.isPauseAllowed(disks));
        getResumeUploadCommand().setIsExecutionAllowed(UploadImageModel.isResumeAllowed(disks));
        getDownloadCommand().setIsExecutionAllowed(DownloadImageHandler.isDownloadAllowed(disks));
        DiskOperationsHelper.updateMoveAndCopyCommandAvailability(disks, getMoveCommand(), getCopyCommand());
    }

    private boolean isRemoveCommandAvailable(List<DiskImage> disks) {
        return disks.stream().noneMatch(d -> d.getImageStatus() == ImageStatus.LOCKED ||
                (d.isOvfStore() && d.getImageStatus() != ImageStatus.ILLEGAL));
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

        List<DiskModel> items = new ArrayList<>();
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
        List<ActionParametersBase> paramerterList = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;
            ActionParametersBase parameters = new RemoveDiskParameters(disk.getId(), getEntity().getId());
            paramerterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveDisk, paramerterList,
                result -> {
                    StorageDiskListModel localModel = (StorageDiskListModel) result.getState();
                    localModel.stopProgress();
                    cancel();
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
                getSelectedItem());
    }

    private void cancelUpload() {
        UploadImageModel.showCancelUploadDialog(this, HelpTag.cancel_upload_image_to_domain, getSelectedItems());
    }

    private void onCancelUpload() {
        UploadImageModel.onCancelUpload((ConfirmationModel) getWindow(), getSelectedItems());
    }

    private void pauseUpload() {
        if (getWindow() != null) {
            return;
        }

        UploadImageModel.pauseUploads(getSelectedItems());
    }

    private void download() {
        DownloadImageManager.getInstance().startDownload(getSelectedItems());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("Upload".equals(command.getName())) { //$NON-NLS-1$
            upload();
        } else if ("CancelUpload".equals(command.getName())) { //$NON-NLS-1$
            cancelUpload();
        } else if ("OnCancelUpload".equals(command.getName())) { //$NON-NLS-1$
            onCancelUpload();
        } else if ("PauseUpload".equals(command.getName())) { //$NON-NLS-1$
            pauseUpload();
        } else if ("ResumeUpload".equals(command.getName())) { //$NON-NLS-1$
            resumeUpload();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getDownloadCommand()) {
            download();
        } else if (command == getMoveCommand()) {
            DiskOperationsHelper.move(this, getSelectedItems());
        } else if (command == getCopyCommand()) {
            DiskOperationsHelper.copy(this, getSelectedItems());
        }
    }

    @Override
    protected String getListName() {
        return "StorageDiskListModel"; //$NON-NLS-1$
    }
}
