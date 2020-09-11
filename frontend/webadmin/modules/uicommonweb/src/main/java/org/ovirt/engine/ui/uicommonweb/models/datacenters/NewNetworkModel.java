package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkWithSubnetParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.PortSecuritySelectorValue;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewNetworkModel extends NetworkModel {

    private ListModel<NetworkClusterModel> privateNetworkClusterList;

    public NewNetworkModel(SearchableListModel<?, ? extends Network> sourceListModel) {
        super(sourceListModel);
        setNetworkClusterList(new ListModel<NetworkClusterModel>());
        getIsVmNetwork().getEntityChangedEvent().addListener(
                (ev, sender, args) -> updatePortIsolationState());
        getPortIsolation().getEntityChangedEvent().addListener((ev, sender, args) -> updateClusterAttachments());
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
        getConnectedToPhysicalNetwork().setEntity(false);

        initMtu();
        initEnablePortSecurity();
    }

    @Override
    public void syncWithBackend() {
        super.syncWithBackend();
        // Get dc- cluster list
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(
                clusters -> onGetClusterList(clusters)), getSelectedDc().getId());
    }

    protected void onGetClusterList(List<Cluster> clusterList) {
        // Cluster list
        List<NetworkClusterModel> items = new ArrayList<>();
        for (Cluster cluster : clusterList) {
            items.add(createNetworkClusterModel(cluster));
        }
        getNetworkClusterList().setItems(items);
        selectExternalProviderBasedOnCluster();
    }

    protected NetworkClusterModel createNetworkClusterModel(Cluster cluster) {
        NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
        networkClusterModel.setAttached(true);
        networkClusterModel.setRequired(!getExternal().getEntity());

        return networkClusterModel;
    }

    @Override
    protected void initMtu() {
        getMtuSelector().setSelectedItem(MtuSelector.defaultMtu);
        getMtu().setEntity(null);
    }

    @Override
    protected void initEnablePortSecurity() {
        getPortSecuritySelector().setSelectedItem(PortSecuritySelectorValue.ENABLED);
    }

    @Override
    protected void updateMtuSelectorsChangeability() {
        setMtuSelectorsChangeability(true, null);
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
    }

    @Override
    protected void selectExternalProvider() {
        getExternalProviders().setSelectedItem(Linq.firstOrNull(getExternalProviders().getItems()));
    }

    private void selectExternalProviderBasedOnCluster() {
        ArrayList<NetworkClusterModel> clusters = getClustersToAttach();
        if (clusters != null && !clusters.isEmpty()) {
            NetworkClusterModel networkClusterModel =
                    clusters.stream().filter(model -> !model.getIsChangable()).findAny().orElse(clusters.get(0));
            ListModel<Provider<?>> providers = getExternalProviders();
            providers.getItems().stream()
                    .filter(provider -> networkClusterModel.getEntity().hasDefaultNetworkProviderId(provider.getId()))
                    .findFirst().ifPresent(providers::setSelectedItem);
        }
    }

    @Override
    protected void selectPhysicalDatacenterNetwork() {
        getDatacenterPhysicalNetwork().setSelectedItem(Linq.firstOrNull(getDatacenterPhysicalNetwork().getItems()));
    }

    @Override
    protected void onExportChanged() {
        boolean externalNetwork = getExternal().getEntity();
        getIsVmNetwork().setIsChangeable(!externalNetwork &&
                ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        if (externalNetwork) {
            getIsVmNetwork().setEntity(true);
        }

        updatePortIsolationState();

        super.onExportChanged();
    }


    private void updatePortIsolationState() {
        updatePortIsolationChangeabilityAndValue();
        updateClusterAttachments();
    }

   private void updatePortIsolationChangeabilityAndValue() {
        boolean portIsolationAllowed = getIsVmNetwork().getEntity() && !getExternal().getEntity();
        getPortIsolation().setIsChangeable(portIsolationAllowed);
        if (!portIsolationAllowed) {
            getPortIsolation().setEntity(false);
        }
    }

    private void updateClusterAttachments() {
        Iterable<NetworkClusterModel> networkClusters = getNetworkClusterList().getItems();
        if (networkClusters != null) {
            for (NetworkClusterModel networkClusterModel : networkClusters) {
                Cluster cluster = networkClusterModel.getEntity();
                boolean portIsolationSupported =
                        AsyncDataProvider.getInstance().getIsPortIsolationSupported(cluster.getCompatibilityVersion())
                                && cluster.getRequiredSwitchTypeForCluster() == SwitchType.LEGACY;
                boolean attached = portIsolationSupported || !getPortIsolation().getEntity();
                networkClusterModel.setAttached(attached);
                networkClusterModel.setRequired(attached && !getExternal().getEntity());
            }
        }
    }

    @Override
    protected void executeSave() {
        final AddNetworkWithSubnetParameters parameters =
            new AddNetworkWithSubnetParameters(getSelectedDc().getId(), getNetwork());
        parameters.setVnicProfileRequired(false);

        parameters.setNetworkClusterList(createNetworkAttachments());
        // New network
        if (getExternal().getEntity()) {
            Provider<?> externalProvider = getExternalProviders().getSelectedItem();
            ProviderNetwork providerNetwork = new ProviderNetwork();
            providerNetwork.setProviderId(externalProvider.getId());
            getNetwork().setProvidedBy(providerNetwork);
            getNetwork().getProvidedBy().setPortSecurityEnabled(getPortSecuritySelector().getSelectedItem().getValue());
            if (hasDefinedSubnet()) {
                getSubnetModel().flush();
                parameters.setExternalSubnet(getSubnetModel().getSubnet());
            }

            if (getConnectedToPhysicalNetwork().getEntity() && getUsePhysicalNetworkFromDatacenter().getEntity()) {
                Network network = getDatacenterPhysicalNetwork().getSelectedItem();
                providerNetwork.setPhysicalNetworkId(network.getId());
            } else if (getConnectedToPhysicalNetwork().getEntity() && getUsePhysicalNetworkFromCustom().getEntity()) {
                providerNetwork.setCustomPhysicalNetworkName(getCustomPhysicalNetwork().getEntity());
                providerNetwork.setExternalVlanId(getVLanTag().getEntity());
            }

            Frontend.getInstance().runAction(
                hasDefinedSubnet() ? ActionType.AddNetworkWithSubnetOnProvider : ActionType.AddNetworkOnProvider,
                parameters, addNetworkCallback(), null);
        } else {
            Frontend.getInstance().runAction(ActionType.AddNetwork,
                parameters, addNetworkCallback(), null);
        }
    }

    private IFrontendActionAsyncCallback addNetworkCallback() {
        return result -> postAddNetwork(result.getReturnValue());
    }

    private void postAddNetwork(ActionReturnValue retVal) {
        if (isActionSucceeded(retVal)) {
            postSaveAction(retVal.getActionReturnValue());
        } else {
            failedPostSaveAction();
        }
    }

    private boolean isActionSucceeded(ActionReturnValue retVal) {
        return retVal != null && retVal.getSucceeded();
    }

    private void failedPostSaveAction() {
        super.postSaveAction(null, false);
    }

    private void postSaveAction(Guid id) {
        super.postSaveAction(id, true);
    }

    private List<NetworkCluster> createNetworkAttachments() {
        List<NetworkCluster> networkAttachments = new ArrayList<>();
        getClustersToAttach().forEach(networkClusterModel -> {
            NetworkCluster networkCluster = new NetworkCluster();
            // Network id is added in the backend
            networkCluster.setClusterId(networkClusterModel.getEntity().getId());
            networkCluster.setRequired(networkClusterModel.isRequired());
            networkAttachments.add(networkCluster);
        });
        return networkAttachments;
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

    private boolean hasDefinedSubnet() {
        return getExternal().getEntity() && getCreateSubnet().getEntity() && getNetwork().isExternal();
    }
}
