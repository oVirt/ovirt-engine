package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
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

    private UICommand syncAllNetworksCommand;

    public UICommand getSyncAllNetworksCommand() {
        return syncAllNetworksCommand;
    }

    public void setSyncAllNetworksCommand(UICommand value) {
        this.syncAllNetworksCommand = value;
    }

    public ClusterNetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().logicalNetworksTitle());
        setHelpTag(HelpTag.logical_networks);
        setHashName("logical_networks"); //$NON-NLS-1$

        setManageCommand(new UICommand("Manage", this)); //$NON-NLS-1$
        setSetAsDisplayCommand(new UICommand("SetAsDisplay", this)); //$NON-NLS-1$
        setNewNetworkCommand(new UICommand("New", this)); //$NON-NLS-1$
        setSyncAllNetworksCommand(new UICommand("SyncAllClusterNetworks", this)); //$NON-NLS-1$
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

        Guid clusterId = getEntity().getId();
        IdQueryParameters tempVar = new IdQueryParameters(clusterId);
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetAllNetworksByClusterId, tempVar, new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
            final List<Network> newItems = returnValue.getReturnValue();
            Collections.sort(newItems,
                    Comparator.comparing((Network n) -> n.getCluster().isManagement()).reversed()
                        .thenComparing(Network::getName, new LexoNumericComparator())
                    );
            for (Network network : newItems) {
                network.getCluster().setId(new NetworkClusterId(clusterId, network.getId()));
            }
            setItems(newItems);
        }));
    }

    public void setAsDisplay() {
        final Network network = (Network) Cloner.clone(getSelectedItem());
        network.getCluster().setDisplay(true);

        final NetworkClusterParameters networkClusterParameters = new NetworkClusterParameters(network.getCluster());

        Frontend.getInstance().runAction(ActionType.UpdateNetworkOnCluster, networkClusterParameters);
    }

    public void manage() {
        if (getWindow() != null) {
            return;
        }

        Guid storagePoolId =
                (getEntity().getStoragePoolId() != null) ? getEntity().getStoragePoolId() : Guid.Empty;

        // fetch the list of DC Networks
        AsyncDataProvider.getInstance().getNetworkList(new AsyncQuery<>(dcNetworks -> {
            ClusterNetworkManageModel networkToManage = createNetworkList(dcNetworks);
            setWindow(networkToManage);
            networkToManage.setTitle(ConstantsManager.getInstance().getConstants().assignDetachNetworksTitle());
            networkToManage.setHelpTag(HelpTag.assign_networks);
            networkToManage.setHashName("assign_networks"); //$NON-NLS-1$
        }), storagePoolId);
    }

    private ClusterNetworkManageModel createNetworkList(List<Network> dcNetworks) {
        final List<ClusterNetworkModel> networkList = new ArrayList<>();
        final List<Network> clusterNetworks = new ArrayList<>(getItems());
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

        Collections.sort(networkList,
                Comparator.comparing(ClusterNetworkModel::isManagement).reversed()
                        .thenComparing(ClusterNetworkModel::getNetworkName, new LexoNumericComparator()));

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

        updateActionAvailabilitySyncAllNetworks();
    }

    private void updateActionAvailabilitySyncAllNetworks() {
        if (getEntity() != null) {
            AsyncDataProvider.getInstance().getClusterNetworkSyncStatus(new AsyncQuery<>(isOutOfSync ->
                getSyncAllNetworksCommand().setIsExecutionAllowed(isOutOfSync)), getEntity().getId());
        }
    }

    private void syncAllNetworks() {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().syncAllClusterNetworkConfirmationDialogTitle());
        model.setHelpTag(HelpTag.sync_all_cluster_networks);
        model.setHashName("sync_all_cluster_networks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToSyncAllClusterNetworksMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSyncAllClusterNetworksConfirm", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onSyncAllClusterNetworksConfirm(){
        ConfirmationModel model = (ConfirmationModel) getWindow();
        if (model.getProgress() != null) {
            return;
        }
        getWindow().startProgress();
        Frontend.getInstance().runAction(ActionType.SyncAllClusterNetworks,
                new ClusterParametersBase(getEntity().getId()),
                result -> {
                    getWindow().stopProgress();
                    cancel();
                });
    }


    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        final ClusterNewNetworkModel networkModel = new ClusterNewNetworkModel(this, getEntity());
        setWindow(networkModel);

        // Set selected dc
        if (getEntity().getStoragePoolId() != null) {
            AsyncDataProvider.getInstance().getDataCenterById(networkModel.asyncQuery(dataCenter -> {
                networkModel.getDataCenters().setItems(Arrays.asList(dataCenter));
                networkModel.getDataCenters().setSelectedItem(dataCenter);
            }), getEntity().getStoragePoolId());
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
        } else if (command == getSyncAllNetworksCommand()) {
            syncAllNetworks();
        } else if ("OnSyncAllClusterNetworksConfirm".equals(command.getName())) { //$NON-NLS-1$
            onSyncAllClusterNetworksConfirm();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "ClusterNetworkListModel"; //$NON-NLS-1$
    }

}
