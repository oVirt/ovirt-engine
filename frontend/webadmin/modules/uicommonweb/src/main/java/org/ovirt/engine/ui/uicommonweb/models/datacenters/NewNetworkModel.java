package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewNetworkModel extends NetworkModel {

    private ListModel<NetworkClusterModel> privateNetworkClusterList;

    public NewNetworkModel(ListModel sourceListModel) {
        super(sourceListModel);
        setNetworkClusterList(new ListModel());
        init();
    }

    public ListModel<NetworkClusterModel> getNetworkClusterList() {
        return privateNetworkClusterList;
    }

    public void setNetworkClusterList(ListModel<NetworkClusterModel> value) {
        privateNetworkClusterList = value;
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        setHelpTag(HelpTag.new_logical_network);
        setHashName("new_logical_network"); //$NON-NLS-1$

        initMtu();
    }

    @Override
    public void syncWithBackend() {
        super.syncWithBackend();
        // Get dc- cluster list
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(NewNetworkModel.this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        onGetClusterList((ArrayList<Cluster>) ReturnValue);
                    }
                }), getSelectedDc().getId());
    }

    protected void onGetClusterList(ArrayList<Cluster> clusterList) {
        // Cluster list
        List<NetworkClusterModel> items = new ArrayList<>();
        for (Cluster cluster : clusterList) {
            items.add(createNetworkClusterModel(cluster));
        }
        getNetworkClusterList().setItems(items);
    }

    protected NetworkClusterModel createNetworkClusterModel(Cluster cluster) {
        NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
        networkClusterModel.setAttached(true);
        networkClusterModel.setRequired(!getExport().getEntity());

        return networkClusterModel;
    }

    @Override
    protected void initMtu() {
        getMtuSelector().setSelectedItem(MtuSelector.defaultMtu);
        getMtu().setEntity(null);
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
    }

    @Override
    protected void selectExternalProvider() {
        getExternalProviders().setSelectedItem(Linq.firstOrNull(getExternalProviders().getItems()));
    }

    @Override
    protected void onExportChanged() {
        boolean externalNetwork = getExport().getEntity();
        getExternalProviders().setIsChangeable(externalNetwork);
        getIsVmNetwork().setIsChangeable(!externalNetwork && isSupportBridgesReportByVDSM()
                && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        if (externalNetwork) {
            getIsVmNetwork().setEntity(true);
        }

        Iterable<NetworkClusterModel> networkClusters = getNetworkClusterList().getItems();
        if (networkClusters != null) {
            for (NetworkClusterModel networkCluster : networkClusters) {
                networkCluster.setIsChangeable(!externalNetwork);
                networkCluster.setAttached(!externalNetwork);
                networkCluster.setRequired(!externalNetwork);
            }
        }

        super.onExportChanged();
    }

    @Override
    protected void executeSave() {
        final AddNetworkStoragePoolParameters parameters =
                new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork());
        parameters.setVnicProfileRequired(false);

        // New network
        if (getExport().getEntity()) {
            Provider externalProvider = getExternalProviders().getSelectedItem();
            ProviderNetwork providerNetwork = new ProviderNetwork();
            providerNetwork.setProviderId(externalProvider.getId());
            getNetwork().setProvidedBy(providerNetwork);

            Frontend.getInstance().runAction(VdcActionType.AddNetworkOnProvider,
                    parameters, addNetworkOnProviderCallback(), null);
        } else {
            Frontend.getInstance().runAction(VdcActionType.AddNetwork,
                    parameters, addNetworkCallback(), null);
        }
    }

    private IFrontendActionAsyncCallback addNetworkCallback() {
        return new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
                postAddNetwork(result1.getReturnValue());
            }
        };
    }

    private IFrontendActionAsyncCallback addNetworkOnProviderCallback() {
        return new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
                postAddNetworkOnProvider(result1.getReturnValue());
            }
        };
    }

    private void postAddNetwork(VdcReturnValueBase retVal) {
        if (isActionSucceeded(retVal)) {
            postSaveAction((Guid) retVal.getActionReturnValue(), null);
        } else {
            failedPostSaveAction();
        }
    }

    private void postAddNetworkOnProvider(VdcReturnValueBase retVal) {
        if (isActionSucceeded(retVal)) {
            Network network = (Network) retVal.getActionReturnValue();
            postSaveAction(network.getId(), network.getProvidedBy());
        } else {
            failedPostSaveAction();
        }
    }

    private boolean isActionSucceeded(VdcReturnValueBase retVal) {
        return retVal != null && retVal.getSucceeded();
    }

    private void failedPostSaveAction() {
        super.postSaveAction(null, false);
    }

    private void postSaveAction(Guid id, ProviderNetwork providedBy) {
        super.postSaveAction(id, true);
        attachNetworkToClusters(id);

        if (getExport().getEntity() && getCreateSubnet().getEntity() && providedBy != null) {
            getSubnetModel().setExternalNetwork(providedBy);
            getSubnetModel().flush();

            Frontend.getInstance().runAction(VdcActionType.AddSubnetToProvider,
                    new AddExternalSubnetParameters(getSubnetModel().getSubnet(), providedBy.getProviderId(), providedBy.getExternalId()));
        }
    }

    private void attachNetworkToClusters(Guid networkGuid) {
        final Guid networkId = getNetwork().getId() == null ? networkGuid : getNetwork().getId();
        final List<NetworkCluster> networkAttachments = new ArrayList<>();

        for (NetworkClusterModel networkClusterModel : getClustersToAttach()) {
            // Init default NetworkCluster values (required, display, status)
            NetworkCluster networkCluster = new NetworkCluster();
            networkCluster.setNetworkId(networkId);
            networkCluster.setClusterId(networkClusterModel.getEntity().getId());
            networkCluster.setRequired(networkClusterModel.isRequired());
            networkAttachments.add(networkCluster);
        }

        if (!networkAttachments.isEmpty()) {
            Frontend.getInstance().runAction(
                    VdcActionType.ManageNetworkClusters,
                    new ManageNetworkClustersParameters(networkAttachments));
        }
    }

    @Override
    protected boolean isManagement() {
        return false;
    }

    public ArrayList<NetworkClusterModel> getClustersToAttach() {
        ArrayList<NetworkClusterModel> clusterToAttach = new ArrayList<>();

        for (NetworkClusterModel networkClusterModel : getNetworkClusterList().getItems()) {
            if (networkClusterModel.isAttached()) {
                clusterToAttach.add(networkClusterModel);
            }
        }
        return clusterToAttach;
    }
}
