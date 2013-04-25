package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;

public class StorageDiskListModel extends SearchableListModel
{
    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    public StorageDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHashName("disks"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().Execute();
    }

    @Override
    public StorageDomain getEntity()
    {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
    {
        super.setEntity(value);
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

    public void Cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        StorageDomainQueryParametersBase parameters =
                new StorageDomainQueryParametersBase((getEntity()).getId());
        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetAllDisksByStorageDomainId, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        StorageDiskListModel storageDiskListModel = (StorageDiskListModel) model;
                        storageDiskListModel.setItems((ArrayList<DiskImage>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
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

            if (isImageLocked) {
                return false;
            }
        }

        return true;
    }

    private void Remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveDiskModel model = new RemoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        model.setHashName("remove_disk"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().disksMsg());

        model.getLatch().setIsAvailable(false);

        ArrayList<DiskModel> items = new ArrayList<DiskModel>();
        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;

            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);

            items.add(diskModel);
        }
        model.setItems(items);

        UICommand onRemoveCommand = new UICommand("OnRemove", this); //$NON-NLS-1$
        onRemoveCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        onRemoveCommand.setIsDefault(true);
        model.getCommands().add(onRemoveCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void OnRemove() {
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;
            VdcActionParametersBase parameters = new RemoveDiskParameters(disk.getId(), getEntity().getId());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        StorageDiskListModel localModel = (StorageDiskListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();
                    }
                },
                this);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            Remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) { //$NON-NLS-1$
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) { //$NON-NLS-1$
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageDiskListModel"; //$NON-NLS-1$
    }
}
