package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class ClusterHostListModel extends HostListModel<Cluster> {

    @Inject
    public ClusterHostListModel(final HostGeneralModel hostGeneralModel,
            final HostGlusterSwiftListModel hostGlusterSwiftListModel, final HostBricksListModel hostBricksListModel,
            final HostVmListModel hostVmListModel, final HostEventListModel hostEventListModel,
            final HostInterfaceListModel hostInterfaceListModel, final HostDeviceListModel hostDeviceListModel,
            final HostHardwareGeneralModel hostHardwareGeneralModel, final HostHooksListModel hostHooksListModel,
            final PermissionListModel<VDS> permissionListModel,
            final HostGlusterStorageDevicesListModel gusterStorageDeviceListModel) {
        super(hostGeneralModel, hostGlusterSwiftListModel, hostBricksListModel, hostVmListModel, hostEventListModel,
                hostInterfaceListModel,
                hostDeviceListModel,
                hostHardwareGeneralModel,
                hostHooksListModel,
                permissionListModel,
                gusterStorageDeviceListModel);
        setUpdateMomPolicyCommand(new UICommand("updateMomPolicyCommand", this)); //$NON-NLS-1$
        getUpdateMomPolicyCommand().setAvailableInModes(ApplicationMode.VirtOnly);
    }

    private UICommand updateMomPolicyCommand;

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            setSearchString("hosts: cluster=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VDS);
        tempVar.setRefresh(getIsQueryFirstTime());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    public UICommand getUpdateMomPolicyCommand() {
        return updateMomPolicyCommand;
    }

    public void setUpdateMomPolicyCommand(UICommand updateMomPolicyCommand) {
        this.updateMomPolicyCommand = updateMomPolicyCommand;
    }

    private void updateActionAvailability() {
        getUpdateMomPolicyCommand().setIsAvailable(true);
        ArrayList<VDS> items =
                getSelectedItems() != null ? Linq.<VDS> cast(getSelectedItems()) : new ArrayList<VDS>();
        boolean allHostRunning = !items.isEmpty();

        for (VDS vds : items) {
            if (vds.getStatus() != VDSStatus.Up) {
                allHostRunning = false;
                break;
            }
        }
        getUpdateMomPolicyCommand().setIsExecutionAllowed(allHostRunning);
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command.equals(getUpdateMomPolicyCommand())) {
            updateMomPolicy();
        }
    }

    private void updateMomPolicy() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (VDS vds : getSelectedItems()) {
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.UpdateMomPolicy, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }
}
