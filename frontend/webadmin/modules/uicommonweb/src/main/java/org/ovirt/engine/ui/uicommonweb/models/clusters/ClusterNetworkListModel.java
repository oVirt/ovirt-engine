package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.DisplayNetworkToVdsGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class ClusterNetworkListModel extends SearchableListModel
{

    private UICommand privateNewNetworkCommand;

    public UICommand getNewNetworkCommand()
    {
        return privateNewNetworkCommand;
    }

    private void setNewNetworkCommand(UICommand value)
    {
        privateNewNetworkCommand = value;
    }

    private UICommand privateManageCommand;

    public UICommand getManageCommand()
    {
        return privateManageCommand;
    }

    private void setManageCommand(UICommand value)
    {
        privateManageCommand = value;
    }

    private UICommand privateSetAsDisplayCommand;

    public UICommand getSetAsDisplayCommand()
    {
        return privateSetAsDisplayCommand;
    }

    private void setSetAsDisplayCommand(UICommand value)
    {
        privateSetAsDisplayCommand = value;
    }

    private Network displayNetwork = null;

    private final Comparator<ClusterNetworkManageModel> networkComparator =
            new Comparator<ClusterNetworkManageModel>() {
                @Override
                public int compare(ClusterNetworkManageModel o1, ClusterNetworkManageModel o2) {
                    // management first
                    return o1.isManagement() ? -1 : o1.getName().compareTo(o2.getName());
                }
            };

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    public ClusterNetworkListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().logicalNetworksTitle());
        setHashName("logical_networks"); //$NON-NLS-1$

        setManageCommand(new UICommand("Manage", this)); //$NON-NLS-1$
        setSetAsDisplayCommand(new UICommand("SetAsDisplay", this)); //$NON-NLS-1$
        setNewNetworkCommand(new UICommand("New", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                ArrayList<Network> newItems = (ArrayList<Network>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                Collections.sort(newItems, new Comparator<Network>() {
                    @Override
                    public int compare(Network o1, Network o2) {
                        // management first
                        return HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(o1.getname()) ? -1
                                : o1.getname().compareTo(o2.getname());
                    }
                });
                searchableListModel.setItems(newItems);
            }
        };

        VdsGroupQueryParamenters tempVar = new VdsGroupQueryParamenters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllNetworksByClusterId,
                new VdsGroupQueryParamenters(getEntity().getId())));
        setItems(getAsyncResult().getData());
    }

    public void SetAsDisplay()
    {
        Network network = (Network) getSelectedItem();

        Frontend.RunAction(VdcActionType.UpdateDisplayToVdsGroup, new DisplayNetworkToVdsGroupParameters(getEntity(),
                network,
                true));
    }

    public void Manage()
    {
        if (getWindow() != null)
        {
            return;
        }

        Guid storagePoolId =
                (getEntity().getstorage_pool_id() != null) ? getEntity().getstorage_pool_id().getValue() : NGuid.Empty;

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterNetworkListModel clusterNetworkListModel = (ClusterNetworkListModel) model;
                ArrayList<Network> dcNetworks = (ArrayList<Network>) result;
                ListModel networkToManage = createNetworkList(dcNetworks);
                clusterNetworkListModel.setWindow(networkToManage);
                networkToManage.setTitle(ConstantsManager.getInstance().getConstants().assignDetachNetworksTitle());
                networkToManage.setHashName("assign_networks"); //$NON-NLS-1$
            }
        };
        // fetch the list of DC Networks
        AsyncDataProvider.GetNetworkList(_asyncQuery, storagePoolId);
    }

    private ListModel createNetworkList(List<Network> dcNetworks) {
        List<ClusterNetworkManageModel> networkList = new ArrayList<ClusterNetworkManageModel>();
        java.util.ArrayList<Network> clusterNetworks = Linq.<Network> Cast(getItems());
        for (Network network : dcNetworks) {
            ClusterNetworkManageModel networkManageModel = new ClusterNetworkManageModel(network);
            int index = clusterNetworks.indexOf(network);
            if (index >= 0) {
                Network clusterNetwork = clusterNetworks.get(index);
                networkManageModel.setVmNetwork(clusterNetwork.isVmNetwork());

                // getCluster can return null if the networks is not attached
                if (clusterNetwork.getCluster() == null) {
                    // Init default network_cluster values
                    clusterNetwork.setCluster(new network_cluster());
                }
                networkManageModel.setRequired(clusterNetwork.getCluster().isRequired());
                networkManageModel.setDisplayNetwork(clusterNetwork.getCluster().getis_display());
                if (clusterNetwork.getCluster().getis_display()) {
                    displayNetwork = clusterNetwork;
                }
                networkManageModel.setAttached(true);
            } else {
                networkManageModel.setAttached(false);
            }
            networkList.add(networkManageModel);
        }

        Collections.sort(networkList, networkComparator);

        ListModel listModel = new ListModel();
        listModel.setItems(networkList);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        listModel.getCommands().add(cancelCommand);

        UICommand okCommand = new UICommand("OnManage", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        listModel.getCommands().add(0, okCommand);

        return listModel;
    }

    public void OnManage() {
        final ListModel windowModel = (ListModel) getWindow();

        List<ClusterNetworkManageModel> manageList = Linq.<ClusterNetworkManageModel> Cast(windowModel.getItems());
        List<Network> existingClusterNetworks = Linq.<Network> Cast(getItems());
        final ArrayList<VdcActionParametersBase> toAttach = new ArrayList<VdcActionParametersBase>();
        final ArrayList<VdcActionParametersBase> toDetach = new ArrayList<VdcActionParametersBase>();

        for (ClusterNetworkManageModel networkModel : manageList) {
            Network network = networkModel.getEntity();
            boolean contains = existingClusterNetworks.contains(network);

            boolean needsAttach = networkModel.isAttached() && !contains;
            boolean needsDetach = !networkModel.isAttached() && contains;
            boolean needsUpdate = false;

            if (contains && !needsDetach) {
                Network clusterNetwork = existingClusterNetworks.get(existingClusterNetworks.indexOf(network));

                if ((networkModel.isRequired() != clusterNetwork.getCluster().isRequired())
                        || (networkModel.isDisplayNetwork() != clusterNetwork.getCluster().getis_display())) {
                    needsUpdate = true;
                }
            }

            if (needsAttach || needsUpdate) {
                toAttach.add(new AttachNetworkToVdsGroupParameter(getEntity(), network));
            }

            if (needsDetach) {
                toDetach.add(new AttachNetworkToVdsGroupParameter(getEntity(), network));
            }
        }

        final IFrontendMultipleActionAsyncCallback callback = new IFrontendMultipleActionAsyncCallback() {
            Boolean needsAttach = !toAttach.isEmpty();
            Boolean needsDetach = !toDetach.isEmpty();

            @Override
            public void Executed(FrontendMultipleActionAsyncResult result) {
                if (result.getActionType() == VdcActionType.DetachNetworkToVdsGroup) {
                    needsDetach = false;
                }
                if (result.getActionType() == VdcActionType.AttachNetworkToVdsGroup) {
                    needsAttach = false;
                }

                if (needsAttach) {
                    Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, toAttach, this, null);
                }

                if (needsDetach) {
                    Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, toDetach, this, null);
                }

                if (!needsAttach && !needsDetach) {
                    doFinish();
                }
            }

            private void doFinish() {
                windowModel.StopProgress();
                Cancel();
                ForceRefresh();
            }
        };

        callback.Executed(new FrontendMultipleActionAsyncResult(null, null, null));
        windowModel.StartProgress(null);
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void EntityChanging(Object newValue, Object oldValue)
    {
        VDSGroup vdsGroup = (VDSGroup) newValue;
        getNewNetworkCommand().setIsExecutionAllowed(vdsGroup != null && vdsGroup.getstorage_pool_id() != null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        Network network = (Network) getSelectedItem();

        // CanRemove = SelectedItems != null && SelectedItems.Count > 0;
        getSetAsDisplayCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && network != null && !network.getCluster().getis_display()
                && network.getCluster().getstatus() != NetworkStatus.NonOperational);
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterNetworkModel networkModel = new ClusterNetworkModel();
        setWindow(networkModel);
        networkModel.setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        networkModel.setHashName("new_logical_network"); //$NON-NLS-1$

        if (getEntity().getstorage_pool_id() != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(networkModel);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    final ClusterNetworkModel clusterNetworkModel = (ClusterNetworkModel) model;
                    final storage_pool dataCenter = (storage_pool) result;

                    AsyncDataProvider.IsSupportBridgesReportByVDSM(new AsyncQuery(this,
                            new INewAsyncCallback() {
                                @Override
                                public void OnSuccess(Object target, Object returnValue) {
                                    Boolean isSupportBridgesReportByVDSM = (Boolean) returnValue;
                                    clusterNetworkModel.setSupportBridgesReportByVDSM(isSupportBridgesReportByVDSM);

                                    if (!isSupportBridgesReportByVDSM) {
                                        clusterNetworkModel.getIsVmNetwork().setEntity(true);
                                        clusterNetworkModel.getIsVmNetwork().setIsChangable(false);
                                    }

                                    clusterNetworkModel.setDataCenterName(dataCenter.getname());
                                    AsyncQuery _asyncQuery2 = new AsyncQuery();
                                    _asyncQuery2.asyncCallback = new INewAsyncCallback() {
                                        @Override
                                        public void OnSuccess(Object model, Object ReturnValue)
                                        {
                                            ClusterNetworkListModel networkListModel = ClusterNetworkListModel.this;
                                            ClusterNetworkModel networkModel =
                                                    (ClusterNetworkModel) networkListModel.getWindow();

                                            // Cluster list
                                            ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) ReturnValue;
                                            NetworkClusterModel networkClusterModel;
                                            ListModel networkClusterList =
                                                    new ListModel();
                                            List<NetworkClusterModel> items = new ArrayList<NetworkClusterModel>();
                                            for (VDSGroup cluster : clusterList)
                                            {
                                                networkClusterModel = new NetworkClusterModel(cluster);
                                                if (!(cluster.getId().equals(getEntity().getId()))) {
                                                    networkClusterModel.setAttached(false);
                                                } else {
                                                    networkClusterModel.setAttached(true);
                                                    networkClusterModel.setIsChangable(false);
                                                }

                                                items.add(networkClusterModel);
                                            }
                                            networkClusterList.setItems(items);
                                            networkModel.setNetworkClusterList(networkClusterList);

                                            UICommand tempVar = new UICommand("OnSave", networkListModel); //$NON-NLS-1$
                                            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                                            tempVar.setIsDefault(true);
                                            networkModel.getCommands().add(tempVar);

                                            UICommand tempVar2 = new UICommand("Cancel", networkListModel); //$NON-NLS-1$
                                            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                                            tempVar2.setIsCancel(true);
                                            networkModel.getCommands().add(tempVar2);
                                        }
                                    };
                                    AsyncDataProvider.GetClusterList(_asyncQuery2, dataCenter.getId());
                                }
                            }),
                            dataCenter.getcompatibility_version().toString());
                }
            };
            AsyncDataProvider.GetDataCenterById(_asyncQuery, getEntity().getstorage_pool_id().getValue());
        }
    }

    public void OnSave()
    {
        ClusterNetworkModel model = (ClusterNetworkModel) getWindow();

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        model.setcurrentNetwork(new Network());

        if (!model.Validate() || getEntity().getstorage_pool_id() == null)
        {
            return;
        }

        // Save changes.
        model.getcurrentNetwork().setstorage_pool_id(getEntity().getstorage_pool_id());
        model.getcurrentNetwork().setname((String) model.getName().getEntity());
        model.getcurrentNetwork().setstp((Boolean) model.getIsStpEnabled().getEntity());
        model.getcurrentNetwork().setdescription((String) model.getDescription().getEntity());
        model.getcurrentNetwork().setVmNetwork((Boolean) model.getIsVmNetwork().getEntity());

        model.getcurrentNetwork().setMtu(0);
        if (model.getMtu().getEntity() != null)
        {
            model.getcurrentNetwork().setMtu(Integer.parseInt(model.getMtu().getEntity().toString()));
        }

        model.getcurrentNetwork().setvlan_id(null);
        if ((Boolean) model.getHasVLanTag().getEntity())
        {
            model.getcurrentNetwork().setvlan_id(Integer.parseInt(model.getVLanTag().getEntity().toString()));
        }

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddNetwork,
                new AddNetworkStoragePoolParameters(model.getcurrentNetwork().getstorage_pool_id().getValue(),
                        model.getcurrentNetwork()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result1) {

                        ClusterNetworkListModel networkListModel1 =
                                (ClusterNetworkListModel) result1.getState();
                        VdcReturnValueBase retVal = result1.getReturnValue();
                        boolean succeeded = false;
                        if (retVal != null && retVal.getSucceeded())
                        {
                            succeeded = true;
                        }
                        networkListModel1.PostNetworkAction(succeeded ? (Guid) retVal.getActionReturnValue()
                                : null,
                                succeeded);

                    }
                },
                this);
    }

    public void PostNetworkAction(Guid networkGuid, boolean succeeded)
    {
        ClusterNetworkModel networkModel = (ClusterNetworkModel) getWindow();
        if (succeeded)
        {
            Cancel();
        }
        else
        {
            networkModel.StopProgress();
            return;
        }
        networkModel.StopProgress();

        Network network = networkModel.getcurrentNetwork();
        networkModel.setnewClusters(new ArrayList<VDSGroup>());

        for (Object item : networkModel.getNetworkClusterList().getItems())
        {
            NetworkClusterModel networkClusterModel = (NetworkClusterModel) item;
            if (networkClusterModel.isAttached())
            {
                networkModel.getnewClusters().add(networkClusterModel.getEntity());
            }
        }
        Guid networkId = networkGuid;

        ArrayList<VdcActionParametersBase> actionParameters1 =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : networkModel.getnewClusters())
        {
            Network tempVar = new Network();
            tempVar.setId(networkId);
            tempVar.setname(network.getname());
            // Init default network_cluster values (required, display, status)
            tempVar.setCluster(new network_cluster());
            actionParameters1.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar));
        }

        Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getManageCommand())
        {
            Manage();
        }
        else if (command == getSetAsDisplayCommand())
        {
            SetAsDisplay();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnManage")) //$NON-NLS-1$
        {
            OnManage();
        }
        else if (StringHelper.stringsEqual(command.getName(), "New")) //$NON-NLS-1$
        {
            New();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "ClusterNetworkListModel"; //$NON-NLS-1$
    }

}
