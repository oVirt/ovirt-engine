package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class SystemTreeModel extends SearchableListModel<Void, SystemTreeItemModel> implements IFrontendMultipleQueryAsyncCallback {

    public static final EventDefinition resetRequestedEventDefinition;
    public static final EventDefinition beforeItemsChangedEventDefinition;
    private Event<EventArgs> privateResetRequestedEvent;
    private Event<EventArgs> privateBeforeItemsChangedEvent;

    public Event<EventArgs> getResetRequestedEvent() {
        return privateResetRequestedEvent;
    }

    private void setResetRequestedEvent(Event<EventArgs> value) {
        privateResetRequestedEvent = value;
    }

    public Event<EventArgs> getBeforeItemsChangedEvent() {
        return privateBeforeItemsChangedEvent;
    }

    private void setBeforeItemsChangedEvent(Event<EventArgs> value) {
        privateBeforeItemsChangedEvent = value;
    }

    private UICommand privateResetCommand;

    public UICommand getResetCommand() {
        return privateResetCommand;
    }

    private void setResetCommand(UICommand value) {
        privateResetCommand = value;
    }

    private UICommand privateExpandAllCommand;

    public UICommand getExpandAllCommand() {
        return privateExpandAllCommand;
    }

    private void setExpandAllCommand(UICommand value) {
        privateExpandAllCommand = value;
    }

    private UICommand privateCollapseAllCommand;

    public UICommand getCollapseAllCommand() {
        return privateCollapseAllCommand;
    }

    private void setCollapseAllCommand(UICommand value) {
        privateCollapseAllCommand = value;
    }

    @Override
    public ArrayList<SystemTreeItemModel> getItems() {
        return (ArrayList<SystemTreeItemModel>) super.getItems();
    }

    @Override
    public void setItems(Collection<SystemTreeItemModel> value) {
        getBeforeItemsChangedEvent().raise(this, EventArgs.EMPTY);
        super.setItems(value);
    }

    public void setItems(ArrayList<SystemTreeItemModel> value) {
        if (items != value) {
            itemsChanging(value, items);
            items = value;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    private List<StoragePool> privateDataCenters;

    public List<StoragePool> getDataCenters() {
        return privateDataCenters;
    }

    public void setDataCenters(List<StoragePool> value) {
        privateDataCenters = value;
    }

    private List<Provider> privateProviders;

    private List<Provider> getProviders() {
        return privateProviders;
    }

    private void setProviders(List<Provider> value) {
        privateProviders = value;
    }

    private List<Erratum> privateErrata;

    public List<Erratum> getErrata() {
        return privateErrata;
    }

    public void setErrata(List<Erratum> value) {
        privateErrata = value;
    }

    private HashMap<Guid, ArrayList<Cluster>> privateClusterMap;

    public HashMap<Guid, ArrayList<Cluster>> getClusterMap() {
        return privateClusterMap;
    }

    public void setClusterMap(HashMap<Guid, ArrayList<Cluster>> value) {
        privateClusterMap = value;
    }

    private Map<Guid, List<Network>> networkMap;

    public Map<Guid, List<Network>> getNetworkMap() {
        return networkMap;
    }

    public void setNetworkMap(Map<Guid, List<Network>> value) {
        networkMap = value;
    }

    private HashMap<Guid, ArrayList<VDS>> privateHostMap;

    public HashMap<Guid, ArrayList<VDS>> getHostMap() {
        return privateHostMap;
    }

    public void setHostMap(HashMap<Guid, ArrayList<VDS>> value) {
        privateHostMap = value;
    }

    private HashMap<Guid, ArrayList<GlusterVolumeEntity>> privateVolumeMap;

    public HashMap<Guid, ArrayList<GlusterVolumeEntity>> getVolumeMap() {
        return privateVolumeMap;
    }

    public void setVolumeMap(HashMap<Guid, ArrayList<GlusterVolumeEntity>> value) {
        privateVolumeMap = value;
    }

    private Map<Guid, SystemTreeItemModel> treeItemById;

    public SystemTreeItemModel getItemById(Guid id) {
        return treeItemById.get(id);
    }

    static {
        resetRequestedEventDefinition = new EventDefinition("ResetRequested", SystemTreeModel.class); //$NON-NLS-1$
        beforeItemsChangedEventDefinition = new EventDefinition("BeforeItemsChanged", //$NON-NLS-1$
                SystemTreeModel.class);
    }

    public SystemTreeModel() {
        setResetRequestedEvent(new Event<>(resetRequestedEventDefinition));
        setBeforeItemsChangedEvent(new Event<>(beforeItemsChangedEventDefinition));

        setResetCommand(new UICommand("Reset", this)); //$NON-NLS-1$
        setExpandAllCommand(new UICommand("ExpandAll", this)); //$NON-NLS-1$
        setCollapseAllCommand(new UICommand("CollapseAll", this)); //$NON-NLS-1$

        setIsTimerDisabled(true);

        setItems(new ArrayList<SystemTreeItemModel>());
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        doDataCenterSearch();
        doClusterSearch();
        doHostSearch();
        doVolumeSearch();
        doProviderSearch();
        //Stop the timer if it is running. syncSearch only gets called by either a manual refresh and the timer
        //shouldn't run during that, or during a fast forward, and that restarts the timer each cycle.
        getTimer().stop();
    }

    /**
     * Create and run the query for all data centers.
     */
    private void doDataCenterSearch() {
        final AsyncQuery dcQuery = new AsyncQuery();
        dcQuery.setModel(this);
        dcQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object result) {
                final SystemTreeModel systemTreeModel = (SystemTreeModel) model;
                systemTreeModel.setDataCenters((List<StoragePool>) result);
                //These need to be here so we can get data center ids for use in the queries.
                doNetworksSearch();
            }
        };
        AsyncDataProvider.getInstance().getDataCenterList(dcQuery, false);
    }

    /**
     * Create and run the query for all clusters.
     */
    private void doClusterSearch() {
        AsyncQuery clusterQuery = new AsyncQuery();
        clusterQuery.setModel(this);
        clusterQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object result) {
                SystemTreeModel systemTreeModel = (SystemTreeModel) model;
                List<Cluster> clusters = (List<Cluster>) result;

                systemTreeModel.setClusterMap(new HashMap<Guid, ArrayList<Cluster>>());
                for (Cluster cluster : clusters) {
                    if (cluster.getStoragePoolId() != null) {
                        Guid key = cluster.getStoragePoolId();
                        if (!systemTreeModel.getClusterMap().containsKey(key)) {
                            systemTreeModel.getClusterMap().put(key, new ArrayList<Cluster>());
                        }
                        List<Cluster> list = systemTreeModel.getClusterMap().get(key);
                        list.add(cluster);
                    }
                }
            }
        };
        AsyncDataProvider.getInstance().getClusterList(clusterQuery, false);
    }

    /**
     * Create and run the query for all hosts.
     */
    private void doHostSearch() {
        AsyncQuery hostQuery = new AsyncQuery();
        hostQuery.setModel(this);
        hostQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object result) {
                SystemTreeModel systemTreeModel = (SystemTreeModel) model;
                List<VDS> hosts = (List<VDS>) result;
                systemTreeModel.setHostMap(new HashMap<Guid, ArrayList<VDS>>());
                for (VDS host : hosts) {
                    Guid key = host.getClusterId();
                    if (!systemTreeModel.getHostMap().containsKey(key)) {
                        systemTreeModel.getHostMap().put(key, new ArrayList<VDS>());
                    }
                    List<VDS> list = systemTreeModel.getHostMap().get(key);
                    list.add(host);
                }
            }
        };
        AsyncDataProvider.getInstance().getHostList(hostQuery, false);
    }

    /**
     * Create and run the query for all volumes.
     */
    private void doVolumeSearch() {
        AsyncQuery volumeQuery = new AsyncQuery();
        volumeQuery.setModel(this);
        volumeQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object result) {
                final SystemTreeModel systemTreeModel = (SystemTreeModel) model;
                List<GlusterVolumeEntity> volumes = (List<GlusterVolumeEntity>) result;
                systemTreeModel.setVolumeMap(new HashMap<Guid, ArrayList<GlusterVolumeEntity>>());

                for (GlusterVolumeEntity volume : volumes) {
                    Guid key = volume.getClusterId();
                    if (!systemTreeModel.getVolumeMap().containsKey(key)) {
                        systemTreeModel.getVolumeMap().put(key, new ArrayList<GlusterVolumeEntity>());
                    }
                    List<GlusterVolumeEntity> list = systemTreeModel.getVolumeMap().get(key);
                    list.add(volume);
                }
            }
        };
        AsyncDataProvider.getInstance().getVolumeList(volumeQuery, null, false);
    }

    /**
     * Create and run the query for all networks.
     */
    private void doNetworksSearch() {
        // Networks
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        ArrayList<VdcQueryParametersBase> queryParamList = new ArrayList<>();

        for (StoragePool dataCenter : getDataCenters()) {
            queryTypeList.add(VdcQueryType.GetAllNetworks);
            queryParamList.add(new IdQueryParameters(dataCenter.getId()));
        }

        Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamList,
                new IFrontendMultipleQueryAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {

                setNetworkMap(new HashMap<Guid, List<Network>>());

                List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
                List<Network> dcNetworkList;
                Guid dcId;

                for (int i = 0; i < returnValueList.size(); i++) {
                    VdcQueryReturnValue returnValue = returnValueList.get(i);
                    if (returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
                        dcNetworkList = returnValue.getReturnValue();
                        dcId = getDataCenters().get(i).getId();
                        getNetworkMap().put(dcId, dcNetworkList);
                    }
                }
                doStorageSearch();
            }
        });
    }

    /**
     * Create and run the query for all storage domains.
     */
    private void doStorageSearch() {
        // Storages
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        ArrayList<VdcQueryParametersBase> queryParamList = new ArrayList<>();

        for (StoragePool dataCenter : getDataCenters()) {
            queryTypeList.add(VdcQueryType.GetStorageDomainsByStoragePoolId);
            queryParamList.add(new IdQueryParameters(dataCenter.getId()));
        }
        if ((ApplicationModeHelper.getUiMode().getValue() & ApplicationMode.VirtOnly.getValue()) == 0) {
            FrontendMultipleQueryAsyncResult dummyResult = new FrontendMultipleQueryAsyncResult();
            VdcQueryReturnValue value = new VdcQueryReturnValue();
            value.setSucceeded(true);
            dummyResult.getReturnValues().add(value);
            SystemTreeModel.this.executed(dummyResult);
        } else {
            Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamList, this);
        }
    }

    /**
     * Create and run the query for all providers.
     */
    private void doProviderSearch() {
        AsyncQuery providersQuery = new AsyncQuery();
        providersQuery.setModel(this);
        providersQuery.asyncCallback = new INewAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object returnValue) {
                setProviders((List<Provider>) returnValue);
            }
        };
        AsyncDataProvider.getInstance().getAllProviders(providersQuery, false);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getResetCommand()) {
            reset();
        }
        else if (command == getExpandAllCommand()) {
            expandAll();
        }
        else if (command == getCollapseAllCommand()) {
            collapseAll();
        }
    }

    private void collapseAll() {
        setIsExpandedRecursively(false, getItems().get(0));
    }

    private void expandAll() {
        setIsExpandedRecursively(true, getItems().get(0));
    }

    private void setIsExpandedRecursively(boolean value, SystemTreeItemModel root) {
        root.setIsExpanded(value);

        for (SystemTreeItemModel model : root.getChildren()) {
            setIsExpandedRecursively(value, model);
        }
    }

    private void reset() {
        getResetRequestedEvent().raise(this, EventArgs.EMPTY);
    }

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result) {
        List<StorageDomain> storageDomains = null;
        int count = -1;
        treeItemById = new HashMap<>();

        // Build tree items.
        SystemTreeItemModel systemItem = new SystemTreeItemModel();
        systemItem.setType(SystemTreeItemType.System);
        systemItem.setIsSelected(true);
        systemItem.setTitle(ConstantsManager.getInstance().getConstants().systemTitle());

        // Add Data Centers node under System
        SystemTreeItemModel dataCentersItem = new SystemTreeItemModel();
        dataCentersItem.setType(SystemTreeItemType.DataCenters);
        dataCentersItem.setApplicationMode(ApplicationMode.VirtOnly);
        dataCentersItem.setTitle(ConstantsManager.getInstance().getConstants().dataCentersTitle());
        systemItem.addChild(dataCentersItem);

        // Populate everything under Data Centers
        for (VdcQueryReturnValue returnValue : result.getReturnValues()) {
            ++count;
            if (!returnValue.getSucceeded()) {
                continue;
            }
            storageDomains = returnValue.getReturnValue();

            SystemTreeItemModel dataCenterItem = new SystemTreeItemModel();
            dataCenterItem.setType(SystemTreeItemType.DataCenter);
            dataCenterItem.setApplicationMode(ApplicationMode.VirtOnly);
            StoragePool dataCenter = getDataCenters().get(count);
            dataCenterItem.setTitle(dataCenter.getName());
            dataCenterItem.setEntity(dataCenter);
            dataCentersItem.addChild(dataCenterItem);
            treeItemById.put(dataCenter.getId(), dataCenterItem);

            SystemTreeItemModel storagesItem = new SystemTreeItemModel();
            storagesItem.setType(SystemTreeItemType.Storages);
            storagesItem.setApplicationMode(ApplicationMode.VirtOnly);
            storagesItem.setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
            storagesItem.setEntity(dataCenter);
            dataCenterItem.addChild(storagesItem);

            if (storageDomains != null && storageDomains.size() > 0) {
                // sort by name first
                Collections.sort(storageDomains, new LexoNumericNameableComparator<>());
                for (StorageDomain storage : storageDomains) {
                    SystemTreeItemModel storageItem = new SystemTreeItemModel();
                    storageItem.setType(SystemTreeItemType.Storage);
                    storageItem.setApplicationMode(ApplicationMode.VirtOnly);
                    storageItem.setTitle(storage.getStorageName());
                    storageItem.setEntity(storage);
                    storagesItem.addChild(storageItem);
                    treeItemById.put(storage.getId(), storageItem);
                }
            }

            SystemTreeItemModel networksItem = new SystemTreeItemModel();
            networksItem.setType(SystemTreeItemType.Networks);
            networksItem.setApplicationMode(ApplicationMode.VirtOnly);
            networksItem.setTitle(ConstantsManager.getInstance().getConstants().networksTitle());
            networksItem.setEntity(dataCenter);
            dataCenterItem.addChild(networksItem);

            List<Network> dcNetworks = getNetworkMap().get(dataCenter.getId());
            if (dcNetworks != null) {
                // sort by name first
                Collections.sort(dcNetworks, new NameableComparator());
                for (Network network : dcNetworks) {
                    SystemTreeItemModel networkItem = new SystemTreeItemModel();
                    networkItem.setType(SystemTreeItemType.Network);
                    networkItem.setApplicationMode(ApplicationMode.VirtOnly);
                    networkItem.setTitle(network.getName());
                    networkItem.setEntity(network);
                    networksItem.addChild(networkItem);
                    treeItemById.put(network.getId(), networkItem);
                }
            }

            SystemTreeItemModel templatesItem = new SystemTreeItemModel();
            templatesItem.setType(SystemTreeItemType.Templates);
            templatesItem.setApplicationMode(ApplicationMode.VirtOnly);
            templatesItem.setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
            templatesItem.setEntity(dataCenter);
            dataCenterItem.addChild(templatesItem);

            SystemTreeItemModel clustersItem = new SystemTreeItemModel();
            clustersItem.setType(SystemTreeItemType.Clusters);
            clustersItem.setTitle(ConstantsManager.getInstance().getConstants().clustersTitle());
            clustersItem.setEntity(dataCenter);
            dataCenterItem.addChild(clustersItem);

            if (getClusterMap().containsKey(dataCenter.getId())) {
                List<Cluster> clusters = getClusterMap().get(dataCenter.getId());
                Collections.sort(clusters, new LexoNumericNameableComparator<>());
                for (Cluster cluster : clusters) {
                    SystemTreeItemModel clusterItem = new SystemTreeItemModel();
                    clusterItem.setType(cluster.supportsGlusterService() ? SystemTreeItemType.Cluster_Gluster
                            : SystemTreeItemType.Cluster);
                    clusterItem.setTitle(cluster.getName());
                    clusterItem.setEntity(cluster);
                    clustersItem.addChild(clusterItem);
                    treeItemById.put(cluster.getId(), clusterItem);

                    SystemTreeItemModel hostsItem = new SystemTreeItemModel();
                    hostsItem.setType(SystemTreeItemType.Hosts);
                    hostsItem.setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
                    hostsItem.setEntity(cluster);
                    clusterItem.addChild(hostsItem);

                    if (getHostMap().containsKey(cluster.getId())) {
                        for (VDS host : getHostMap().get(cluster.getId())) {
                            SystemTreeItemModel hostItem = new SystemTreeItemModel();
                            hostItem.setType(SystemTreeItemType.Host);
                            hostItem.setTitle(host.getName());
                            hostItem.setEntity(host);
                            hostsItem.addChild(hostItem);
                            treeItemById.put(host.getId(), hostItem);
                        }
                    }
                    if (cluster.supportsGlusterService()) {
                        SystemTreeItemModel volumesItem = new SystemTreeItemModel();
                        volumesItem.setType(SystemTreeItemType.Volumes);
                        volumesItem.setApplicationMode(ApplicationMode.GlusterOnly);
                        volumesItem.setTitle(ConstantsManager.getInstance().getConstants().volumesTitle());
                        volumesItem.setEntity(cluster);
                        clusterItem.addChild(volumesItem);

                        if (getVolumeMap().containsKey(cluster.getId())) {
                            for (GlusterVolumeEntity volume : getVolumeMap().get(cluster.getId())) {
                                SystemTreeItemModel volumeItem = new SystemTreeItemModel();
                                volumeItem.setType(SystemTreeItemType.Volume);
                                volumeItem.setApplicationMode(ApplicationMode.GlusterOnly);
                                volumeItem.setTitle(volume.getName());
                                volumeItem.setEntity(volume);
                                volumesItem.addChild(volumeItem);
                                treeItemById.put(volume.getId(), volumeItem);
                            }
                        }
                    }

                    if (cluster.supportsVirtService()) {
                        SystemTreeItemModel vmsItem = new SystemTreeItemModel();
                        vmsItem.setType(SystemTreeItemType.VMs);
                        vmsItem.setApplicationMode(ApplicationMode.VirtOnly);
                        vmsItem.setTitle(ConstantsManager.getInstance().getConstants().vmsTitle());
                        vmsItem.setEntity(cluster);
                        clusterItem.addChild(vmsItem);
                    }
                }
            }
        }

        // Add Providers node under System
        SystemTreeItemModel providersItem = new SystemTreeItemModel();
        providersItem.setType(SystemTreeItemType.Providers);
        providersItem.setApplicationMode(ApplicationMode.VirtOnly);
        providersItem.setTitle(ConstantsManager.getInstance().getConstants().externalProvidersTitle());
        systemItem.addChild(providersItem);

        // Populate with providers
        for (Provider provider : getProviders()) {
            SystemTreeItemModel providerItem = new SystemTreeItemModel();
            providerItem.setType(SystemTreeItemType.Provider);
            providerItem.setApplicationMode(ApplicationMode.VirtOnly);
            providerItem.setTitle(provider.getName());
            providerItem.setEntity(provider);
            providersItem.addChild(providerItem);
            treeItemById.put(provider.getId(), providerItem);
        }

        // add Errata node under System
        SystemTreeItemModel errataItem = new SystemTreeItemModel();
        errataItem.setType(SystemTreeItemType.Errata);
        errataItem.setApplicationMode(ApplicationMode.AllModes);
        errataItem.setTitle(ConstantsManager.getInstance().getConstants().errata());
        systemItem.addChild(errataItem);

        //Add sessions node under System
        SystemTreeItemModel sessionsItem = new SystemTreeItemModel();
        sessionsItem.setType(SystemTreeItemType.Sessions);
        sessionsItem.setApplicationMode(ApplicationMode.AllModes);
        sessionsItem.setTitle(ConstantsManager.getInstance().getConstants().activeUserSessionsTitle());
        systemItem.addChild(sessionsItem);

        if (!ApplicationModeHelper.getUiMode().equals(ApplicationMode.AllModes)) {
            ApplicationModeHelper.filterSystemTreeByApplictionMode(systemItem);
        }
        List<SystemTreeItemModel> newItems =
                new ArrayList<>(Arrays.asList(new SystemTreeItemModel[] { systemItem }));
        if (items == null || items.size() == 0 || !newItems.get(0).equals(items.toArray()[0], true)) {
            setItems(newItems);
        }
    }

    @Override
    protected SystemTreeItemModel determineSelectedItems(List<SystemTreeItemModel> newItems,
            SystemTreeItemModel lastSelectedItem, List<SystemTreeItemModel> lastSelectedItems) {
        SystemTreeItemModel newSelectedItem = null;
        for (SystemTreeItemModel newItem : newItems) {
            newSelectedItem = findNode(newItem, lastSelectedItem);
            // Search for selected item
            if (newSelectedItem == null) {
                // Search for selected items
                for (SystemTreeItemModel item : lastSelectedItems) {
                    if (newItem.equals(item)) {
                        selectedItems.add(newItem);
                    }
                }
            }
        }
        return newSelectedItem;
    }

    public SystemTreeItemModel findNode(SystemTreeItemModel root, SystemTreeItemModel match) {
        SystemTreeItemModel result = null;
        if (root != null && match != null) {
            if (root.equals(match)) {
                result = root; //match found.
            } else {
                if (root.getChildren().size() > 0) {
                    for (int i = 0; i < root.getChildren().size(); i++) {
                        result = findNode(root.getChildren().get(i), match);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected String getListName() {
        return "SystemTreeModel"; //$NON-NLS-1$
    }

    @Override
    protected boolean refreshOnInactiveTimer() {
        return true;
    }
}
