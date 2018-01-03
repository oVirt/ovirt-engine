package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
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
import org.ovirt.engine.ui.uicommonweb.models.storage.DownloadImageHandler;
import org.ovirt.engine.ui.uicommonweb.models.storage.DownloadImageManager;
import org.ovirt.engine.ui.uicommonweb.models.storage.ExportRepoImageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.CopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.MoveDiskModel;
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

    private UICommand privateScanAlignmentCommand;

    public UICommand getScanAlignmentCommand() {
        return privateScanAlignmentCommand;
    }

    private void setScanAlignmentCommand(UICommand value) {
        privateScanAlignmentCommand = value;
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

    private UICommand stopDownloadCommand;

    public UICommand getStopDownloadCommand() {
        return stopDownloadCommand;
    }

    private void setStopDownloadCommand(UICommand value) {
        stopDownloadCommand = value;
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
        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$
        setScanAlignmentCommand(new UICommand("Check Alignment", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setUploadCommand(new UICommand("Upload", this)); //$NON-NLS-1$
        setCancelUploadCommand(new UICommand("CancelUpload", this)); //$NON-NLS-1$
        setPauseUploadCommand(new UICommand("PauseUpload", this)); //$NON-NLS-1$
        setResumeUploadCommand(new UICommand("ResumeUpload", this)); //$NON-NLS-1$
        setDownloadCommand(new UICommand("Download", this)); //$NON-NLS-1$
        setStopDownloadCommand(new UICommand("StopDownload", this)); //$NON-NLS-1$

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

            vmListModel.setIsAvailable(disk.getVmEntityType() == null || !disk.getVmEntityType().isTemplateType());
            templateListModel.setIsAvailable(disk.getVmEntityType() != null && disk.getVmEntityType().isTemplateType());
            storageListModel.setIsAvailable(disk.getDiskStorageType() == DiskStorageType.IMAGE ||
                    disk.getDiskStorageType() == DiskStorageType.CINDER);
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

    }

    private void move() {


        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) asImages(getSelectedItems());

        if (disks == null || getWindow() != null) {
            return;
        }

        MoveDiskModel model = new MoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().moveDisksTitle());
        model.setHelpTag(HelpTag.move_disks);
        model.setHashName("move_disks"); //$NON-NLS-1$
        model.setIsSourceStorageDomainNameAvailable(true);
        model.setEntity(this);
        model.init(disks);
        model.startProgress();
    }

    private void scanAlignment() {
        ArrayList<ActionParametersBase> parameterList = new ArrayList<>();

        for (Disk disk : getSelectedItems()) {
            parameterList.add(new GetDiskAlignmentParameters(disk.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.GetDiskAlignment, parameterList,
                result -> {
                },
                this);
    }

    private void export() {
        @SuppressWarnings("unchecked")
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) asImages(getSelectedItems());

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
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) asImages(getSelectedItems());

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

    private void copy() {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) asImages(getSelectedItems());

        if (disks == null || getWindow() != null) {
            return;
        }

        CopyDiskModel model = new CopyDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().copyDisksTitle());
        model.setHelpTag(HelpTag.copy_disks);
        model.setHashName("copy_disks"); //$NON-NLS-1$
        model.setEntity(this);
        model.init(disks);
        model.startProgress();
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

        selectNextItem();
        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveDisk, parameterList,
                result -> {
                    DiskListModel localModel = (DiskListModel) result.getState();
                    if (result.getReturnValue().stream().anyMatch(rv -> !rv.isValid())) {
                        restorePreviousSelectedItem();
                    }
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

    private void stopDownload() {
        DownloadImageManager.getInstance().stopDownload(getSelectedDiskImages());
    }

    private List<DiskImage> getSelectedDiskImages() {
        return getSelectedItems().stream().map(disk -> (DiskImage) disk).collect(Collectors.toList());
    }

    private void updateActionAvailability() {
        Disk disk = getSelectedItem();
        ArrayList<Disk> disks = getSelectedItems() != null ? (ArrayList<Disk>) getSelectedItems() : null;
        boolean shouldAllowEdit = true;
        if (disk != null) {
            shouldAllowEdit = !disk.isOvfStore() && !isDiskLocked(disk);
        }

        getNewCommand().setIsExecutionAllowed(true);
        getEditCommand().setIsExecutionAllowed(disk != null && disks != null && disks.size() == 1 && shouldAllowEdit);
        getRemoveCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isRemoveCommandAvailable());
        getScanAlignmentCommand().setIsExecutionAllowed(
                disks != null && disks.size() > 0 && isScanAlignmentCommandAvailable());
        getExportCommand().setIsExecutionAllowed(isExportCommandAvailable());
        updateCopyAndMoveCommandAvailability(disks);

        getCancelUploadCommand().setIsExecutionAllowed(UploadImageModel.isCancelAllowed(disks));
        getPauseUploadCommand().setIsExecutionAllowed(UploadImageModel.isPauseAllowed(disks));
        getResumeUploadCommand().setIsExecutionAllowed(UploadImageModel.isResumeAllowed(disks));
        getDownloadCommand().setIsExecutionAllowed(DownloadImageHandler.isDownloadAllowed(disks));
        getStopDownloadCommand().setIsExecutionAllowed(DownloadImageHandler.isStopDownloadAllowed(disks));
    }

    private boolean isDiskLocked(Disk disk) {
        switch (disk.getDiskStorageType()) {
            case IMAGE:
                return ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;
            case CINDER:
                return ((CinderDisk) disk).getImageStatus() == ImageStatus.LOCKED;
        }
        return false;
    }

    private void updateCopyAndMoveCommandAvailability(List<Disk> disks) {
        boolean isCopyAllowed = true;
        boolean isMoveAllowed = true;

        if (disks == null || disks.isEmpty() || disks.get(0).getDiskStorageType() != DiskStorageType.IMAGE) {
            disableMoveAndCopyCommands();
            return;
        }

        Guid datacenterId = ((DiskImage) disks.get(0)).getStoragePoolId();


        boolean foundTemplateDisk = false;
        boolean foundVmDisk = false;
        boolean foundUnattachedDisk = false;

        for (Disk disk : disks) {
            if ((!isCopyAllowed && !isMoveAllowed) || disk.getDiskStorageType() != DiskStorageType.IMAGE) {
                disableMoveAndCopyCommands();
                return;
            }

            DiskImage diskImage = (DiskImage) disk;
            if (diskImage.getImageStatus() != ImageStatus.OK || !datacenterId.equals(diskImage.getStoragePoolId()) || diskImage.isOvfStore()) {
                disableMoveAndCopyCommands();
                return;
            }
            VmEntityType vmEntityType = disk.getVmEntityType();
            if (vmEntityType == null) {
                foundUnattachedDisk = true;
            } else if (vmEntityType.isTemplateType()) {
                foundTemplateDisk = true;
            } else if (vmEntityType.isVmType()) {
                foundVmDisk = true;
            }

            if (foundTemplateDisk && (foundUnattachedDisk || foundVmDisk)) {
                isCopyAllowed = false;
            }

            if (vmEntityType != null && vmEntityType.isTemplateType()) {
                isMoveAllowed = false;
            }
        }

        getCopyCommand().setIsExecutionAllowed(isCopyAllowed);
        getMoveCommand().setIsExecutionAllowed(isMoveAllowed);
    }

    private void disableMoveAndCopyCommands() {
        getCopyCommand().setIsExecutionAllowed(false);
        getMoveCommand().setIsExecutionAllowed(false);
    }

    private boolean isRemoveCommandAvailable() {
        List<Disk> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<>();

        for (Disk disk : disks) {
            // check if the disk is template disk
            if (disk.getVmEntityType() != null && disk.getVmEntityType().isTemplateType()) {
                return false;
            }

            if (disk.getDiskStorageType() == DiskStorageType.IMAGE || disk.getDiskStorageType() == DiskStorageType.CINDER) {
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

    private boolean isScanAlignmentCommandAvailable() {
        List<Disk> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<>();

        for (Disk disk : disks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    ((DiskImage) disk).getImageStatus() != ImageStatus.OK) {
                return false;
            }
        }

        return true;
    }

    private boolean isExportCommandAvailable() {
        List<DiskImage> disks = asImages(getSelectedItems());

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
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getMoveCommand()) {
            move();
        }
        else if (command == getCopyCommand()) {
            copy();
        }
        else if (command == getScanAlignmentCommand()) {
            scanAlignment();
        }
        else if (command == getExportCommand()) {
            export();
        }
        else if (RemoveDiskModel.CANCEL_REMOVE.equals(command.getName()) || "Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        }
        else if (RemoveDiskModel.ON_REMOVE.equals(command.getName())) {
            onRemove();
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
        else if (command == getUploadCommand()) {
            upload();
        }
        else if (command == getCancelUploadCommand()) {
            cancelUpload();
        }
        else if ("OnCancelUpload".equals(command.getName())) { //$NON-NLS-1$
            onCancelUpload();
        }
        else if (command == getPauseUploadCommand()) {
            pauseUpload();
        }
        else if (command == getResumeUploadCommand()) {
            resumeUpload();
        }
        else if (command == getDownloadCommand()) {
            download();
        }
        else if (command == getStopDownloadCommand()) {
            stopDownload();
        }
    }

    private List<DiskImage> asImages(List<Disk> disks) {
        if (disks == null) {
            return null;
        }

        List<DiskImage> res = new ArrayList<>();
        for (Disk disk : disks) {
            if (disk.getDiskStorageType().isInternal()) {
                res.add((DiskImage) disk);
            }
        }

        return res;
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
