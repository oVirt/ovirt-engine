package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.queries.IdAndBooleanQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageRegisterDiskImageListModel extends SearchableListModel<StorageDomain, Disk> {
    private UICommand registerCommand;
    private UICommand removeCommand;

    public UICommand getRegisterCommand() {
        return registerCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRegisterCommand(UICommand value) {
        registerCommand = value;
    }
    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public StorageRegisterDiskImageListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksImportTitle());
        setHashName("disk_image_register"); //$NON-NLS-1$
        setRegisterCommand(new UICommand("ImportDiskImage", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("RemoveUnregisteredDiskImage", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
        if (getEntity() != null) {
            updateActionAvailability();
        }
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
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        super.search();
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        IdQueryParameters parameters = new IdAndBooleanQueryParameters(getEntity().getId(), true);
        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetUnregisteredDisksFromDB, parameters,
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<UnregisteredDisk> unregisteredDisks = returnValue.getReturnValue();
                    Collections.sort(unregisteredDisks, Comparator.comparing(UnregisteredDisk::getDiskAlias));
                    ArrayList<Disk> diskItems = new ArrayList<>();
                    for (UnregisteredDisk unregisteredDisk : unregisteredDisks) {
                        diskItems.add(unregisteredDisk.getDiskImage());
                    }
                    setItems(diskItems);
                }));
    }

    private void updateActionAvailability() {
        List<Disk> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<Disk>();
        boolean isExecutionAllowed = disks.size() > 0 && getEntity().getStatus() == StorageDomainStatus.Active;
        getRegisterCommand().setIsExecutionAllowed(isExecutionAllowed);
        getRemoveCommand().setIsExecutionAllowed(isExecutionAllowed);
    }

    private void register() {
        if (getWindow() != null) {
            return;
        }

        final RegisterDiskModel registerDiskModel = new RegisterDiskModel();
        registerDiskModel.setTargetAvailable(false);
        setWindow(registerDiskModel);

        // noinspection unchecked
        registerDiskModel.setEntity(this);
        registerDiskModel.init();
        registerDiskModel.setTitle(ConstantsManager.getInstance().getConstants().importDisksTitle());
        registerDiskModel.setHelpTag(HelpTag.import_disks);
        registerDiskModel.setHashName("import_disks"); //$NON-NLS-1$

        registerDiskModel.startProgress();
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery<>(dataCenter -> {
            registerDiskModel.setQuotaEnforcementType(dataCenter.getQuotaEnforcementType());
            registerDiskModel.setDisks(DiskModel.disksToDiskModelList(getSelectedItems()));
            registerDiskModel.updateStorageDomain(getEntity());
            registerDiskModel.stopProgress();
        }), getEntity().getStoragePoolId());
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel window = new ConfirmationModel();
        setWindow(window);
        window.setTitle(ConstantsManager.getInstance().getConstants().removeUnregisteredDisksTitle());
        window.setMessage(ConstantsManager.getInstance().getConstants().removeConfirmationPopupMessage());
        window.setHelpTag(HelpTag.remove_unregistered_disk);
        window.setHashName("remove_unregistered_disks"); //$NON-NLS-1$

        List<String> items = getSelectedItems().stream()
                .filter(disk -> !Guid.isNullOrEmpty(disk.getId()))
                .map(Disk::getName)
                .collect(Collectors.toList());

        window.setItems(items);

        UICommand onRemoveUiCommand = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        window.getCommands().add(onRemoveUiCommand);
        UICommand cancelUiCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        window.getCommands().add(cancelUiCommand);
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }
        model.startProgress();

        List<ActionParametersBase> removeDiskParameters = getSelectedItems()
                .stream()
                .map(item -> {
                    RemoveDiskParameters params = new RemoveDiskParameters(item.getId(), getEntity().getId());
                    params.setUnregisteredDisk(true);
                    return params;
                })
                .collect(Collectors.toList());

        Frontend.getInstance().runMultipleAction(
                ActionType.RemoveDisk,
                removeDiskParameters,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRegisterCommand()) {
            register();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageRegisterDiskImageListModel"; //$NON-NLS-1$
    }
}
