package org.ovirt.engine.ui.uicommonweb.models.gluster;


import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
        setAvailableInModes(ApplicationMode.GlusterOnly);
    }

    @Override
    public VDS getEntity() {
        return super.getEntity();
    }

    public void setEntity(VDS value)
    {
        super.setEntity(value);
        updateActionAvailability();

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
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);
        getSearchCommand().execute();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDevices(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<StorageDevice> devices = (List<StorageDevice>) returnValue;
                Collections.sort(devices, new Linq.StorageDeviceComparer());
                setItems(devices);
            }
        }), getEntity().getId());

    }

    @Override
    protected String getListName() {
        return "HostGlusterStorageDevicesListModel"; //$NON-NLS-1$
    }

    private void syncStorageDevices() {
        Frontend.getInstance().runAction(VdcActionType.SyncStorageDevices, new VdsActionParameters(getEntity().getId()));
    }

    private void updateActionAvailability() {
        VDS vds = getEntity();
        getSyncStorageDevicesCommand().setIsExecutionAllowed(vds.getStatus() == VDSStatus.Up);
    }
    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getSyncStorageDevicesCommand())) {
            syncStorageDevices();
        }
    }

}
