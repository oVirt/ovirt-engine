package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class StorageSnapshotListModel extends SearchableListModel<StorageDomain, DiskImage> {
    private UICommand removeCommand;

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public StorageSnapshotListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().snapshotsTitle());
        setHelpTag(HelpTag.snapshots);
        setHashName("snapshots"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

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

        Frontend.getInstance().runQuery(VdcQueryType.GetAllDiskSnapshotsByStorageDomainId, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        StorageSnapshotListModel storageSnapshotListModel = (StorageSnapshotListModel) model;
                        ArrayList<DiskImage> newItems = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        Collections.sort(newItems, new Linq.DiskImageByActualSizeComparer());
                        Collections.reverse(newItems);
                        storageSnapshotListModel.setItems(newItems);
                    }
                }));
    }

    private void updateActionAvailability() {
        ArrayList<DiskImage> disks = getSelectedItems() != null ?
                Linq.<DiskImage> cast(getSelectedItems()) : new ArrayList<DiskImage>();

        getRemoveCommand().setIsExecutionAllowed(disks.size() > 0 && isRemoveCommandAvailable(disks));
    }

    private boolean isRemoveCommandAvailable(ArrayList<DiskImage> disks) {
        for (DiskImage disk : disks) {
            boolean isImageLocked = disk.getImageStatus() == ImageStatus.LOCKED;
            boolean isTemplateType = disk.getVmEntityType().isTemplateType();

            if (isImageLocked || isTemplateType) {
                return false;
            }
        }

        return true;
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        model.setHelpTag(HelpTag.remove_disk_snapshot);
        model.setHashName("remove_disk_snapshot"); //$NON-NLS-1$

        model.getLatch().setIsAvailable(false);

        ArrayList<String> items = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;
            items.add(ConstantsManager.getInstance().getMessages().diskSnapshotLabel(
                    disk.getDiskAlias(), disk.getVmSnapshotDescription()));
        }
        model.setItems(items);

        UICommand onRemoveCommand = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(onRemoveCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<>();

        Map<Guid, List<Guid>> diskImageIdsMap = groupImageIdsByDiskId(getSelectedItems());
        for (List<Guid> imageIds : diskImageIdsMap.values()) {
            RemoveDiskSnapshotsParameters parameters = new RemoveDiskSnapshotsParameters(new ArrayList<>(imageIds));
            paramerterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveDiskSnapshots, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        StorageSnapshotListModel localModel = (StorageSnapshotListModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                },
                this);
    }

    private Map<Guid, List<Guid>> groupImageIdsByDiskId(List<DiskImage> diskImages) {
        Map<Guid, List<Guid>> diskImagesMap = new HashMap<>();
        for (DiskImage diskImage: diskImages) {
            List<Guid> images = diskImagesMap.get(diskImage.getId());
            if (images != null) {
                images.add(diskImage.getImageId());
            }
            else {
                diskImagesMap.put(diskImage.getId(), new ArrayList(Arrays.asList(diskImage.getImageId())));
            }
        }
        return diskImagesMap;
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
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageSnapshotListModel"; //$NON-NLS-1$
    }
}
