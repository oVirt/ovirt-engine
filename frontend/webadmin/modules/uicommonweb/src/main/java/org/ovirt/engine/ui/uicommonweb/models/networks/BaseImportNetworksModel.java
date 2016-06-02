package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.providers.ExternalNetwork;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;


public class BaseImportNetworksModel extends Model {

    private static final String CMD_IMPORT = "OnImport"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final SearchableListModel sourceListModel;

    private final StoragePool treeSelectedDc;

    private final ListModel<Provider<?>> providers = new ListModel<>();
    private final ListModel<ExternalNetwork> providerNetworks = new ListModel<>();
    private final ListModel<ExternalNetwork> importedNetworks = new ListModel<>();
    private final ListModel<String> errors = new ListModel<>();

    private final UICommand addImportCommand = new UICommand(null, this);
    private final UICommand cancelImportCommand = new UICommand(null, this);

    private Map<Guid, Collection<Cluster>> dcClusters;
    private final com.google.inject.Provider<CommonModel> commonModelProvider;

    public ListModel<ExternalNetwork> getProviderNetworks() {
        return providerNetworks;
    }

    public ListModel<ExternalNetwork> getImportedNetworks() {
        return importedNetworks;
    }

    public ListModel<Provider<?>> getProviders() {
        return providers;
    }

    public ListModel<String> getErrors() {
        return errors;
    }

    public UICommand getAddImportCommand() {
        return addImportCommand;
    }

    public UICommand getCancelImportCommand() {
        return cancelImportCommand;
    }

    public BaseImportNetworksModel(SearchableListModel sourceListModel,
            final com.google.inject.Provider<CommonModel> commonModelProvider) {
        this.commonModelProvider = commonModelProvider;
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().importNetworksTitle());
        setHelpTag(HelpTag.import_networks);
        setHashName("import_networks"); //$NON-NLS-1$

        UICommand importCommand = new UICommand(CMD_IMPORT, this);
        importCommand.setIsExecutionAllowed(false);
        importCommand.setTitle(ConstantsManager.getInstance().getConstants().importNetworksButton());
        importCommand.setIsDefault(true);
        getCommands().add(importCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand(CMD_CANCEL, this); //$NON-NLS-1$
        getCommands().add(cancelCommand);

        SystemTreeItemModel treeSelectedDcItem =
                SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter,
                        commonModelProvider.get().getSystemTree().getSelectedItem());
        treeSelectedDc = (treeSelectedDcItem == null) ? null : (StoragePool) treeSelectedDcItem.getEntity();

        providers.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                onProviderChosen();
            }
        });

        initProviderList();
    }

    protected void initProviderList() {
        startProgress();
        AsyncDataProvider.getInstance().getAllNetworkProviders(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                stopProgress();
                List<Provider<?>> providers = (List<Provider<?>>) returnValue;
                providers.add(0, null);
                getProviders().setItems(providers);
            }
        }));
    }

    private void onProviderChosen() {
        final Provider<?> provider = providers.getSelectedItem();
        if (provider == null) {
            return;
        }

        final List<StoragePool> dataCenters = new LinkedList<>();

        final AsyncQuery networkQuery = new AsyncQuery();
        networkQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                Map<Network, Set<Guid>> externalNetworkToDataCenters = (Map<Network, Set<Guid>>) returnValue;
                List<ExternalNetwork> items = new LinkedList<>();
                for (Map.Entry<Network, Set<Guid>> entry : externalNetworkToDataCenters.entrySet()) {
                    Network network = entry.getKey();
                    Set<Guid> attachedDataCenters = entry.getValue();

                    ExternalNetwork externalNetwork = new ExternalNetwork();
                    externalNetwork.setNetwork(network);
                    externalNetwork.setDisplayName(network.getName());
                    externalNetwork.setPublicUse(true);

                    List<StoragePool> availableDataCenters = new LinkedList<>();
                    for (StoragePool dc : dataCenters) {
                        if (!attachedDataCenters.contains(dc.getId())) {
                            availableDataCenters.add(dc);
                        }
                    }
                    externalNetwork.getDataCenters().setItems(availableDataCenters);
                    externalNetwork.getDataCenters().setSelectedItem(treeSelectedDc != null
                            && availableDataCenters.contains(treeSelectedDc) ? treeSelectedDc
                            : Linq.firstOrNull(availableDataCenters));

                    items.add(externalNetwork);
                }
                Collections.sort(items, new Linq.ExternalNetworkComparator());
                providerNetworks.setItems(items);
                importedNetworks.setItems(new LinkedList<ExternalNetwork>());

                stopProgress();
            }
        };

        final AsyncQuery dcQuery = new AsyncQuery();
        dcQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                dataCenters.addAll((Collection<StoragePool>) returnValue);
                Collections.sort(dataCenters, new NameableComparator());

                AsyncDataProvider.getInstance().getExternalNetworkMap(networkQuery, provider.getId());
            }
        };

        startProgress();
        AsyncDataProvider.getInstance().getDataCenterList(dcQuery);
    }

    private boolean validate() {
        String errorDuplicate = ConstantsManager.getInstance().getConstants().importDuplicateName();
        boolean valid = true;
        Collection<ExternalNetwork> networks = importedNetworks.getItems();
        List<String> errors = new ArrayList<>(networks.size());
        Map<String, Integer> nameToIndex = new HashMap<>();
        int i = 0;
        for (ExternalNetwork network : networks) {
            String networkName = network.getDisplayName();
            Integer encounteredIndex = nameToIndex.get(networkName);

            // if this name has been encountered, invalidate that entry; else store it for future invalidation
            if (encounteredIndex != null) {
                errors.set(encounteredIndex, errorDuplicate);
                valid = false;
            } else {
                nameToIndex.put(networkName, i);
            }

            // invalidate current entry
            errors.add(encounteredIndex == null ? null : errorDuplicate);
            ++i;
        }
        getErrors().setItems(errors);
        return valid;
    }

    public void cancel() {
        sourceListModel.setWindow(null);
    }

    public void onImport() {
        if (!validate()) {
            return;
        }

        List<VdcActionParametersBase> multipleActionParameters = new LinkedList<>();
        List<IFrontendActionAsyncCallback> callbacks = new LinkedList<>();
        dcClusters = new HashMap<>();

        for (final ExternalNetwork externalNetwork : importedNetworks.getItems()) {
            final Network network = externalNetwork.getNetwork();
            final Guid dcId = externalNetwork.getDataCenters().getSelectedItem().getId();
            network.setName(externalNetwork.getDisplayName());
            network.setDataCenterId(dcId);
            AddNetworkStoragePoolParameters params =
                    new AddNetworkStoragePoolParameters(dcId, network);
            params.setVnicProfileRequired(false);
            multipleActionParameters.add(params);
            callbacks.add(new IFrontendActionAsyncCallback() {

                @Override
                public void executed(FrontendActionAsyncResult result) {
                    VdcReturnValueBase returnValue = result.getReturnValue();
                    if (returnValue != null && returnValue.getSucceeded()) {
                        network.setId((Guid) returnValue.getActionReturnValue());

                        // Perform sequentially: first fetch clusters, then attach network, then create VNIC profile
                        fetchDcClusters(dcId, network, externalNetwork.isPublicUse());
                    }
                }
            });
        }

        Frontend.getInstance().runMultipleActions(VdcActionType.AddNetwork, multipleActionParameters, callbacks);
        cancel();
    }

    private void fetchDcClusters(final Guid dcId, final Network network, final boolean publicUse) {
        if (dcClusters.containsKey(dcId)) {
            attachNetworkToClusters(network, dcClusters.get(dcId), publicUse);
        } else {
            AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this, new INewAsyncCallback() {

                @Override
                public void onSuccess(Object model, Object returnValue) {
                    Collection<Cluster> clusters = (Collection<Cluster>) returnValue;
                    dcClusters.put(dcId, clusters);
                    attachNetworkToClusters(network, clusters, publicUse);
                }
            }), dcId);
        }
    }

    private void attachNetworkToClusters(final Network network, Collection<Cluster> clusters, final boolean publicUse) {
        List<NetworkCluster> networkAttachments = new LinkedList<>();
        for (Cluster cluster : clusters) {
            final NetworkCluster networkCluster = new NetworkCluster();
            networkCluster.setClusterId(cluster.getId());
            networkCluster.setNetworkId(network.getId());
            networkCluster.setRequired(false);
            networkAttachments.add(networkCluster);
        }

        Frontend.getInstance().runAction(
                VdcActionType.ManageNetworkClusters,
                new ManageNetworkClustersParameters(networkAttachments),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        addVnicProfile(network, publicUse);
                    }
                });
    }

    private void addVnicProfile(Network network, boolean publicUse) {
        VnicProfile vnicProfile = new VnicProfile();
        vnicProfile.setName(network.getName());
        vnicProfile.setNetworkId(network.getId());
        AddVnicProfileParameters parameters = new AddVnicProfileParameters(vnicProfile, true);
        parameters.setPublicUse(publicUse);
        Frontend.getInstance().runAction(VdcActionType.AddVnicProfile, parameters, new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                sourceListModel.getSearchCommand().execute();
            }
        });
    }

    private void addImport() {
        getDefaultCommand().setIsExecutionAllowed(true);
    }

    private void cancelImport() {
        Collection<ExternalNetwork> selectedNetworks = getImportedNetworks().getSelectedItems();
        Collection<ExternalNetwork> importedNetworks = getImportedNetworks().getItems();
        getDefaultCommand().setIsExecutionAllowed(selectedNetworks.size() < importedNetworks.size());

        for (ExternalNetwork externalNetwork : selectedNetworks) {
            externalNetwork.setDisplayName(externalNetwork.getNetwork().getName());
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CMD_IMPORT.equals(command.getName())) {
            onImport();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        } else if (getAddImportCommand().equals(command)) {
            addImport();
        } else if (getCancelImportCommand().equals(command)) {
            cancelImport();
        }
    }

}
