package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.CreateBrickParameters;
import org.ovirt.engine.core.common.action.gluster.SyncGlusterStorageDevicesParameter;
import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostGlusterStorageDevicesListModel extends SearchableListModel<VDS, StorageDevice> {
    public HostGlusterStorageDevicesListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageDevices());
        setHelpTag(HelpTag.gluster_storage_devices);
        setHashName("gluster_storage_devices"); //$NON-NLS-1$
        setSyncStorageDevicesCommand(new UICommand("sync", this)); //$NON-NLS-1$
        setCreateBrickCommand(new UICommand("createBrick", this)); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);
    }

    @Override
    public VDS getEntity() {
        return super.getEntity();
    }

    @Override
    public void setEntity(VDS value) {
        super.setEntity(value);
        updateActionAvailability();

    }

    private UICommand createBrickCommand;

    public UICommand getCreateBrickCommand() {
        return createBrickCommand;
    }

    public void setCreateBrickCommand(UICommand createBrickCommand) {
        this.createBrickCommand = createBrickCommand;
    }

    private UICommand syncStorageDevicesCommand;

    public UICommand getSyncStorageDevicesCommand() {
        return syncStorageDevicesCommand;
    }

    public void setSyncStorageDevicesCommand(UICommand syncStorageDevicesCommand) {
        this.syncStorageDevicesCommand = syncStorageDevicesCommand;
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDevices(new AsyncQuery<>(devices -> {
            Collections.sort(devices, Linq.StorageDeviceComparer);
            setItems(devices);
        }), getEntity().getId());

    }

    @Override
    protected String getListName() {
        return "HostGlusterStorageDevicesListModel"; //$NON-NLS-1$
    }

    private void createBrick() {
        if (getWindow() != null) {
            return;
        }

        VDS host = getEntity();
        if (host == null) {
            return;
        }

        final CreateBrickModel lvModel = new CreateBrickModel();
        lvModel.setTitle(ConstantsManager.getInstance().getConstants().createBrick());
        lvModel.setHelpTag(HelpTag.create_brick);
        lvModel.setHashName("create_brick"); //$NON-NLS-1$
        lvModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());
        setWindow(lvModel);
        lvModel.getRaidTypeList().setSelectedItem(RaidType.RAID6);
        List<StorageDevice> selectedDevices = getSelectedItems();
        lvModel.getStorageDevices().setItems(selectedDevices);
        lvModel.setSelectedDevices(selectedDevices);

        AsyncQuery<String> asyncQueryForDefaultMountPoint = lvModel.asyncQuery(defaultMountPoint -> {
            lvModel.stopProgress();
            lvModel.getDefaultMountFolder().setEntity(defaultMountPoint);
        });
        AsyncDataProvider.getInstance()
                .getConfigFromCache(new GetConfigurationValueParameters(ConfigValues.GlusterDefaultBrickMountPoint,
                        AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                        asyncQueryForDefaultMountPoint);
        AsyncDataProvider.getInstance().getStorageDevices(new AsyncQuery<>(
                returnValue -> {
                    if (returnValue != null) {
                        List<StorageDevice> storageDeviceList = new ArrayList<>();
                        storageDeviceList.add(null); //$NON-NLS-1$
                        for (StorageDevice storagedevice : returnValue) {
                            if (storagedevice.getCanCreateBrick() && !selectedDevices.contains(storagedevice)) {
                                storageDeviceList.add(storagedevice);
                            }
                        }
                        lvModel.getCacheDevicePathTypeList().setItems(storageDeviceList);
                    }
                }), host.getId());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onCreateBrick", this); //$NON-NLS-1$
        lvModel.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("closeWindow", this); //$NON-NLS-1$
        lvModel.getCommands().add(cancelCommand);
    }

    private void syncStorageDevices() {
        Frontend.getInstance()
                .runAction(ActionType.SyncStorageDevices,
                        new SyncGlusterStorageDevicesParameter(getEntity().getId()),
                        null,
                        true,
                        true);
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

    private void updateActionAvailability() {
        VDS vds = getEntity();
        if (vds != null && vds.getStatus() == VDSStatus.Up) {
            getSyncStorageDevicesCommand().setIsExecutionAllowed(true);
            getCreateBrickCommand().setIsExecutionAllowed(canCreateBrick());
        } else {
            getSyncStorageDevicesCommand().setIsExecutionAllowed(false);
            getCreateBrickCommand().setIsExecutionAllowed(false);
        }
    }

    private boolean canCreateBrick(){
        boolean canCreateBrick = false;
        List<StorageDevice> selectedDevices = getSelectedItems();
        if (selectedDevices != null) {
            for (StorageDevice device : selectedDevices) {
                if (device.getCanCreateBrick()) {
                    canCreateBrick = true;
                } else {
                    canCreateBrick = false;
                    break;
                }
            }
        }

        return canCreateBrick;

    }
    private void onCreateBrick() {
        CreateBrickModel lvModel = (CreateBrickModel) getWindow();
        if (lvModel == null) {
            return;
        }
        if (!lvModel.validate()) {
            return;
        }

        VDS host = getEntity();
        if (host == null) {
            return;
        }

        lvModel.startProgress();

        List<StorageDevice> selectedDevices = new ArrayList<>();
        for (StorageDevice device : lvModel.getStorageDevices().getSelectedItems()) {
            selectedDevices.add(device);
        }

        CreateBrickParameters parameters =
                new CreateBrickParameters(host.getId(),
                        lvModel.getLvName().getEntity(),
                        lvModel.getMountPoint().getEntity(),
                        lvModel.getRaidTypeList().getSelectedItem(),
                        lvModel.getNoOfPhysicalDisksInRaidVolume().getEntity(),
                        lvModel.getStripeSize().getEntity(),
                        selectedDevices,
                        lvModel.getCacheDevicePathTypeList().getSelectedItem(),
                        lvModel.getCacheModeTypeList().getSelectedItem(),
                        lvModel.getCacheSize().getEntity());

        Frontend.getInstance().runAction(ActionType.CreateBrick, parameters,
                result -> postCreateBrick(result.getReturnValue()), this);

    }

    private void postCreateBrick(ActionReturnValue returnValue) {
        CreateBrickModel model = (CreateBrickModel) getWindow();
        model.stopProgress();
        if (returnValue != null && returnValue.getSucceeded()) {
            setWindow(null);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getCreateBrickCommand())) {
            createBrick();
        } else if (command.getName().equals("onCreateBrick")) { //$NON-NLS-1$
            onCreateBrick();
        } else if (command.getName().equals("closeWindow")) { //$NON-NLS-1$
            setWindow(null);
        } else if (command.equals(getSyncStorageDevicesCommand())) {
            syncStorageDevices();
        }
    }

}
