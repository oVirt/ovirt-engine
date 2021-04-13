package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DiskOperationsHelper;
import org.ovirt.engine.ui.uicommonweb.models.storage.DownloadImageHandler;
import org.ovirt.engine.ui.uicommonweb.models.storage.DownloadImageManager;
import org.ovirt.engine.ui.uicommonweb.models.storage.ExportRepoImageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveDiskModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class DiskListModel extends ListWithSimpleDetailsModel<Void, Disk> {

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private UICommand privateMoveCommand;

    public UICommand getMoveCommand() {
        return privateMoveCommand;
    }

    private void setMoveCommand(UICommand value) {
        privateMoveCommand = value;
    }

    private UICommand exportCommand;

    public UICommand getExportCommand() {
        return exportCommand;
    }

    public void setExportCommand(UICommand exportCommand) {
        this.exportCommand = exportCommand;
    }

    private UICommand privateChangeQuotaCommand;

    public UICommand getChangeQuotaCommand() {
        return privateChangeQuotaCommand;
    }

    private void setChangeQuotaCommand(UICommand value) {
        privateChangeQuotaCommand = value;
    }

    private UICommand refreshLUNCommand;

    public UICommand getRefreshLUNCommand() {
        return refreshLUNCommand;
    }

    private void setRefreshLUNCommand(UICommand value) {
        refreshLUNCommand = value;
    }


    private UICommand privateCopyCommand;

    public UICommand getCopyCommand() {
        return privateCopyCommand;
    }

    private void setCopyCommand(UICommand value) {
        privateCopyCommand = value;
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

    private EntityModel<DiskStorageType> diskViewType;

    public EntityModel<DiskStorageType> getDiskViewType() {
        return diskViewType;
    }

    public void setDiskViewType(EntityModel<DiskStorageType> diskViewType) {
        this.diskViewType = diskViewType;
    }

    private EntityModel<DiskContentType> diskContentType;

    public EntityModel<DiskContentType> getDiskContentType() {
        return diskContentType;
    }

    public void setDiskContentType(EntityModel<DiskContentType> diskContentType) {
        this.diskContentType = diskContentType;
    }

    private final DiskVmListModel vmListModel;
    private final DiskTemplateListModel templateListModel;
    private final DiskStorageListModel storageListModel;
    private final DiskGeneralModel generalModel;
    private final PermissionListModel<Disk> permissionListModel;

    @Inject
    public DiskListModel(final DiskVmListModel diskVmListModel, final DiskTemplateListModel diskTemplateListModel,
            final DiskStorageListModel diskStorageListModel, final DiskGeneralModel diskGeneralModel,
            final PermissionListModel<Disk> permissionListModel) {
        this.vmListModel = diskVmListModel;
        this.templateListModel = diskTemplateListModel;
        this.storageListModel = diskStorageListModel;
        this.generalModel = diskGeneralModel;
        this.permissionListModel = permissionListModel;
        setDetailList();

        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setApplicationPlace(WebAdminApplicationPlaces.diskMainPlace);

        setDefaultSearchString(SearchStringMapping.DISKS_DEFAULT_SEARCH + ": disk_type = image"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.DISK_OBJ_NAME, SearchObjects.DISK_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setChangeQuotaCommand(new UICommand("changeQuota", this)); //$NON-NLS-1$
        setRefreshLUNCommand(new UICommand("refreshLUN", this)); //$NON-NLS-1$
        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setUploadCommand(new UICommand("Upload", this)); //$NON-NLS-1$
        setCancelUploadCommand(new UICommand("CancelUpload", this)); //$NON-NLS-1$
        setPauseUploadCommand(new UICommand("PauseUpload", this)); //$NON-NLS-1$
        setResumeUploadCommand(new UICommand("ResumeUpload", this)); //$NON-NLS-1$
        setDownloadCommand(new UICommand("Download", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        setDiskViewType(new EntityModel<>());
        setDiskContentType(new EntityModel<>());
    }

    private void setDetailList() {
        vmListModel.setIsAvailable(false);
        templateListModel.setIsAvailable(false);
        storageListModel.setIsAvailable(false);

        List<HasEntity<? extends Disk>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(vmListModel);
        list.add(templateListModel);
        list.add(storageListModel);
        list.add(permissionListModel);
        // TODO: find better type bound for list
        setDetailModels((List) list);
    }


    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.Disk, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    @Override
    public void setItems(Collection<Disk> disks) {
        if (disks == null) {
            super.setItems(null);
            return;
        }

        super.setItems(disks);
    }

    @Override
    protected void updateDetailsAvailability() {
        if (getSelectedItem() != null) {
            Disk disk = getSelectedItem();

            vmListModel.setIsAvailable(!isTemplateDisk(disk));
            templateListModel.setIsAvailable(isTemplateDisk(disk));
            storageListModel.setIsAvailable(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        }
    }

    public void cancel() {
        setWindow(null);
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

    private void newEntity() {
        NewDiskModel model = new NewDiskModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().newVirtualDiskTitle());
        model.setHelpTag(HelpTag.new_virtual_disk);
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.setSourceModel(this);
        model.initialize();
    }

    private void edit() {
        final Disk disk = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        EditDiskModel model = new EditDiskModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().editVirtualDiskTitle());
        model.setHelpTag(HelpTag.edit_virtual_disk);
        model.setHashName("edit_virtual_disk"); //$NON-NLS-1$
        model.setDisk(disk);
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.setSourceModel(this);
        model.initialize();
    }

    private void export() {
        @SuppressWarnings("unchecked")
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) DiskOperationsHelper.asDiskImages(getSelectedItems());

        if (disks == null || getWindow() != null) {
            return;
        }

        ExportRepoImageModel model = new ExportRepoImageModel();
        setWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().exportImagesTitle());
        model.setHelpTag(HelpTag.export_disks);
        model.setHashName("export_disks"); //$NON-NLS-1$
        model.setEntity(this);
        model.init(disks);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$

        model.setCancelCommand(cancelCommand);
        model.getCommands().add(cancelCommand);
    }

    private void changeQuota() {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) DiskOperationsHelper.asDiskImages(getSelectedItems());

        if (disks == null || getWindow() != null) {
            return;
        }

        ChangeQuotaModel model = new ChangeQuotaModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignQuotaForDisk());
        model.setHelpTag(HelpTag.change_quota_disks);
        model.setHashName("change_quota_disks"); //$NON-NLS-1$
        model.startProgress();
        model.init(disks);

        UICommand command = UICommand.createDefaultOkUiCommand("onChangeQuota", this); //$NON-NLS-1$
        model.getCommands().add(command);
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onChangeQuota() {
        ChangeQuotaModel model = (ChangeQuotaModel) getWindow();
        ArrayList<ActionParametersBase> paramerterList = new ArrayList<>();

        for (ChangeQuotaItemModel item : model.getItems()) {
            DiskImage disk = item.getEntity();
            ActionParametersBase parameters =
                    new ChangeQuotaParameters(item.getQuota().getSelectedItem().getId(),
                            disk.getId(),
                            item.getStorageDomainId(),
                            disk.getStoragePoolId());
            paramerterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.ChangeQuotaForDisk, paramerterList,
                result -> {
                    DiskListModel localModel = (DiskListModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                },
                this);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveDiskModel model = new RemoveDiskModel();
        setWindow(model);
        model.initialize(null, getSelectedItems(), this);
        model.getLatch().setIsAvailable(false);
    }

    private void onRemove() {
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        ArrayList<ActionParametersBase> parameterList = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            Disk disk = (Disk) item;
            ActionParametersBase parameters = new RemoveDiskParameters(disk.getId());
            parameterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveDisk, parameterList,
                result -> {
                    DiskListModel localModel = (DiskListModel) result.getState();
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
                HelpTag.upload_disk_image,
                null,
                null);
    }

    private void resumeUpload() {
        if (getSelectedItem() == null || getWindow() != null) {
            return;
        }

        UploadImageModel.showUploadDialog(
                this,
                HelpTag.resume_upload_image,
                null,
                (DiskImage) getSelectedItem());
    }

    private void cancelUpload() {
        UploadImageModel.showCancelUploadDialog(this, HelpTag.cancel_upload_image, getSelectedItems());
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
        DownloadImageManager.getInstance().startDownload(getSelectedDiskImages());
    }

    private void refreshLUN() {
        Set<Guid> lunIds = getSelectedItems().stream()
                .map(disk -> ((LunDisk) disk).getLun().getDiskId())
                .collect(Collectors.toSet());
        Frontend.getInstance().runAction(ActionType.SyncDirectLuns,
                    new SyncDirectLunsParameters(null, lunIds));
    }

    private List<DiskImage> getSelectedDiskImages() {
        return getSelectedItems().stream().map(disk -> (DiskImage) disk).collect(Collectors.toList());
    }

    private void updateActionAvailability() {
        Disk disk = getSelectedItem();
        ArrayList<Disk> disks = getSelectedItems() != null ? (ArrayList<Disk>) getSelectedItems() : null;
        boolean shouldAllowEdit = true;
        if (disk != null) {
            shouldAllowEdit = !disk.isOvfStore() && !isDiskLocked(disk) && !isTemplateDisk(disk);
        }

        getNewCommand().setIsExecutionAllowed(true);
        getEditCommand().setIsExecutionAllowed(disk != null && disks != null && disks.size() == 1 && shouldAllowEdit);
        getRemoveCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isRemoveCommandAvailable());
        getExportCommand().setIsExecutionAllowed(isExportCommandAvailable());
        DiskOperationsHelper.updateMoveAndCopyCommandAvailability(DiskOperationsHelper.asDiskImages(disks),
                getMoveCommand(), getCopyCommand());

        getCancelUploadCommand().setIsExecutionAllowed(UploadImageModel.isCancelAllowed(disks));
        getPauseUploadCommand().setIsExecutionAllowed(UploadImageModel.isPauseAllowed(disks));
        getResumeUploadCommand().setIsExecutionAllowed(UploadImageModel.isResumeAllowed(disks));
        getDownloadCommand().setIsExecutionAllowed(DownloadImageHandler.isDownloadAllowed(disks));
        getChangeQuotaCommand().setIsExecutionAllowed(isAssignQuotaAllowed());
        getRefreshLUNCommand().setIsExecutionAllowed(isRefreshLUNAllowed());
    }

    private boolean isAssignQuotaAllowed() {
        List<Disk> disks = getSelectedItems();
        return disks != null && !disks.isEmpty();
    }

    private boolean isRefreshLUNAllowed() {
        List<Disk> disks = getSelectedItems();
        if (disks == null || disks.size() == 0) {
            return false;
        }
        for (Disk disk : disks) {
            if (disk.getDiskStorageType() != DiskStorageType.LUN) {
                return false;
            }
        }
        return true;
    }

    private boolean isDiskLocked(Disk disk) {
        return disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;
    }

    private boolean isTemplateDisk(Disk disk) {
        return disk.getVmEntityType() != null && disk.getVmEntityType().isTemplateType();
    }

    private boolean isRemoveCommandAvailable() {
        List<Disk> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<>();

        for (Disk disk : disks) {
            // check if the disk is template disk
            if (isTemplateDisk(disk)) {
                return false;
            }

            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                ImageStatus imageStatus = ((DiskImage) disk).getImageStatus();
                if (imageStatus == ImageStatus.LOCKED) {
                    return false;
                }

                if (disk.isOvfStore() && imageStatus != ImageStatus.ILLEGAL) {
                    return false;
                }
            }


        }

        return true;
    }

    private boolean isExportCommandAvailable() {
        List<DiskImage> disks = DiskOperationsHelper.asDiskImages(getSelectedItems());

        if (disks == null || disks.isEmpty()) {
            return false;
        }

        for (Disk disk : disks) {
            if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
                return false;
            }

            DiskImage diskImage = (DiskImage) disk;

            if (diskImage.getImageStatus() != ImageStatus.OK || !diskImage.getParentId().equals(Guid.Empty)) {
                return false;
            }
        }

        return true;
    }

    private void cancelConfirm() {
        AbstractDiskModel model = (AbstractDiskModel) getWindow();
        SanStorageModelBase sanStorageModelBase = model.getSanStorageModelBase();
        sanStorageModelBase.setForce(false);
        setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getMoveCommand()) {
            DiskOperationsHelper.move(this, DiskOperationsHelper.asDiskImages(getSelectedItems()));
        } else if (command == getCopyCommand()) {
            DiskOperationsHelper.copy(this, DiskOperationsHelper.asDiskImages(getSelectedItems()));
        } else if (command == getExportCommand()) {
            export();
        } else if (RemoveDiskModel.CANCEL_REMOVE.equals(command.getName()) || "Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        } else if (RemoveDiskModel.ON_REMOVE.equals(command.getName())) {
            onRemove();
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        } else if (command == getUploadCommand()) {
            upload();
        } else if (command == getCancelUploadCommand()) {
            cancelUpload();
        } else if ("OnCancelUpload".equals(command.getName())) { //$NON-NLS-1$
            onCancelUpload();
        } else if (command == getPauseUploadCommand()) {
            pauseUpload();
        } else if (command == getResumeUploadCommand()) {
            resumeUpload();
        } else if (command == getDownloadCommand()) {
            download();
        } else if (command == getRefreshLUNCommand()) {
            refreshLUN();
        }
    }


    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("disk"); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "DiskListModel"; //$NON-NLS-1$
    }

    public DiskGeneralModel getGeneralModel() {
        return generalModel;
    }

    public DiskVmListModel getVmListModel() {
        return vmListModel;
    }

    public DiskTemplateListModel getTemplateListModel() {
        return templateListModel;
    }

    public DiskStorageListModel getStorageListModel() {
        return storageListModel;
    }

    public PermissionListModel<Disk> getPermissionListModel() {
        return permissionListModel;
    }

}
