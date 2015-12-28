package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.NetworkInClusterComparator;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.ClusterNewNetworkModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterNetworkListModel extends SearchableListModel<Cluster, Network> {

    private UICommand privateNewNetworkCommand;

    public UICommand getNewNetworkCommand() {
        return privateNewNetworkCommand;
    }

    private void setNewNetworkCommand(UICommand value) {
        privateNewNetworkCommand = value;
    }

    private UICommand privateManageCommand;

    public UICommand getManageCommand() {
        return privateManageCommand;
    }

    private void setManageCommand(UICommand value) {
        privateManageCommand = value;
    }

    private UICommand privateSetAsDisplayCommand;

    public UICommand getSetAsDisplayCommand() {
        return privateSetAsDisplayCommand;
    }

    private void setSetAsDisplayCommand(UICommand value) {
        privateSetAsDisplayCommand = value;
    }

    public ClusterNetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().logicalNetworksTitle());
        setHelpTag(HelpTag.logical_networks);
        setHashName("logical_networks"); //$NON-NLS-1$

        setManageCommand(new UICommand("Manage", this)); //$NON-NLS-1$
        setSetAsDisplayCommand(new UICommand("SetAsDisplay", this)); //$NON-NLS-1$
        setNewNetworkCommand(new UICommand("New", this)); //$NON-NLS-1$
        getSetAsDisplayCommand().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
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

        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                final List<Network> newItems = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                Collections.sort(newItems, new NetworkInClusterComparator());
                for (Network network : newItems) {
                    network.getCluster().setId(new NetworkClusterId(getEntity().getId(), network.getId()));
                }
                setItems(newItems);
            }
        };

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllNetworksByClusterId, tempVar, _asyncQuery);
    }

    public void setAsDisplay() {
        final Network network = (Network) Cloner.clone(getSelectedItem());
        network.getCluster().setDisplay(true);

        final NetworkClusterParameters networkClusterParameters = new NetworkClusterParameters(network.getCluster());

        Frontend.getInstance().runAction(VdcActionType.UpdateNetworkOnCluster, networkClusterParameters);
    }

    public void manage() {
        if (getWindow() != null) {
            return;
        }

        Guid storagePoolId =
                (getEntity().getStoragePoolId() != null) ? getEntity().getStoragePoolId() : Guid.Empty;

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ClusterNetworkListModel clusterNetworkListModel = (ClusterNetworkListModel) model;
                final List<Network> dcNetworks = (List<Network>) result;

                ClusterNetworkManageModel networkToManage = createNetworkList(dcNetworks);
                clusterNetworkListModel.setWindow(networkToManage);
                networkToManage.setTitle(ConstantsManager.getInstance().getConstants().assignDetachNetworksTitle());
                networkToManage.setHelpTag(HelpTag.assign_networks);
                networkToManage.setHashName("assign_networks"); //$NON-NLS-1$
            }
        };
        // fetch the list of DC Networks
        AsyncDataProvider.getInstance().getNetworkList(_asyncQuery, storagePoolId);
    }

    private ClusterNetworkManageModel createNetworkList(List<Network> dcNetworks) {
        final List<ClusterNetworkModel> networkList = new ArrayList<>();
        final List<Network> clusterNetworks = Linq.cast(getItems());
        for (Network network : dcNetworks) {
            ClusterNetworkModel networkManageModel;
            int index = clusterNetworks.indexOf(network);
            if (index >= 0) {
                Network clusterNetwork = clusterNetworks.get(index);
                networkManageModel = new ClusterNetworkModel((Network) Cloner.clone(clusterNetwork));
            } else {
                networkManageModel = new ClusterNetworkModel((Network) Cloner.clone(network));
            }
            networkManageModel.setCluster((Cluster) Cloner.clone(getEntity()));
            networkList.add(networkManageModel);
        }

        Collections.sort(networkList, new Linq.ClusterNetworkModelComparator());

        ClusterNetworkManageModel listModel = new ClusterNetworkManageModel(this);
        listModel.setItems(networkList);

        return listModel;
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void entityChanging(Cluster newValue, Cluster oldValue) {
        getNewNetworkCommand().setIsExecutionAllowed(!isClusterDetached(newValue));
        getManageCommand().setIsExecutionAllowed(!isClusterDetached(newValue));
    }

    private boolean isClusterDetached(Cluster cluster) {
        return cluster == null || cluster.getStoragePoolId() == null;
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
        Network network = getSelectedItem();

        // CanRemove = SelectedItems != null && SelectedItems.Count > 0;
        getSetAsDisplayCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && network != null && !network.getCluster().isDisplay()
                && network.getCluster().getStatus() != NetworkStatus.NON_OPERATIONAL);
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        final ClusterNewNetworkModel networkModel = new ClusterNewNetworkModel(this, getEntity());
        setWindow(networkModel);

        // Set selected dc
        if (getEntity().getStoragePoolId() != null) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(networkModel);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result) {
                    final StoragePool dataCenter = (StoragePool) result;
                    networkModel.getDataCenters().setItems(Arrays.asList(dataCenter));
                    networkModel.getDataCenters().setSelectedItem(dataCenter);

                }
            };
            AsyncDataProvider.getInstance().getDataCenterById(_asyncQuery, getEntity().getStoragePoolId());
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getManageCommand()) {
            manage();
        } else if (command == getSetAsDisplayCommand()) {
            setAsDisplay();
        } else if ("New".equals(command.getName())) { //$NON-NLS-1$
            newEntity();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "ClusterNetworkListModel"; //$NON-NLS-1$
    }

}
