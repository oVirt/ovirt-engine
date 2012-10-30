package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NetworkClusterListModel extends ClusterListModel
{
    private UICommand privateManageCommand;

    public UICommand getManageCommand()
    {
        return privateManageCommand;
    }

    private void setManageCommand(UICommand value)
    {
        privateManageCommand = value;
    }

    public void Manage()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterNetworkManageModel networkManageModel = new ClusterNetworkManageModel(getEntity().getNetwork());
        setWindow(networkManageModel);
        networkManageModel.setTitle(ConstantsManager.getInstance().getConstants().assignDetachNetworksTitle());
        networkManageModel.setHashName("assign_networks"); //$NON-NLS-1$

        // TODO- initialize with correct values
        networkManageModel.setAttached(true);
        networkManageModel.setRequired(true);
        networkManageModel.setDisplayNetwork(true);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        networkManageModel.getCommands().add(cancelCommand);

        UICommand okCommand = new UICommand("OnManage", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        networkManageModel.getCommands().add(0, okCommand);

    }

//    private ListModel createNetworkList(List<Network> dcNetworks) {
//        List<ClusterNetworkManageModel> networkList = new ArrayList<ClusterNetworkManageModel>();
//        java.util.ArrayList<Network> clusterNetworks = Linq.<Network> Cast(getItems());
//        for (Network network : dcNetworks) {
//            ClusterNetworkManageModel networkManageModel = new ClusterNetworkManageModel(network);
//            int index = clusterNetworks.indexOf(network);
//            if (index >= 0) {
//                Network clusterNetwork = clusterNetworks.get(index);
//                networkManageModel.setVmNetwork(clusterNetwork.isVmNetwork());
//
//                // getCluster can return null if the networks is not attached
//                if (clusterNetwork.getCluster() == null) {
//                    // Init default network_cluster values
//                    clusterNetwork.setCluster(new network_cluster());
//                }
//                networkManageModel.setRequired(clusterNetwork.getCluster().isRequired());
//                networkManageModel.setDisplayNetwork(clusterNetwork.getCluster().getis_display());
//                if (clusterNetwork.getCluster().getis_display()) {
//                    displayNetwork = clusterNetwork;
//                }
//                networkManageModel.setAttached(true);
//            } else {
//                networkManageModel.setAttached(false);
//            }
//            networkList.add(networkManageModel);
//        }
//
//        Collections.sort(networkList, networkComparator);
//
//        ListModel listModel = new ListModel();
//        listModel.setItems(networkList);
//
//        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
//        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
//        cancelCommand.setIsCancel(true);
//        listModel.getCommands().add(cancelCommand);
//
//        UICommand okCommand = new UICommand("OnManage", this); //$NON-NLS-1$
//        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
//        okCommand.setIsDefault(true);
//        listModel.getCommands().add(0, okCommand);
//
//        return listModel;
//    }

    public void OnManage() {
//        final ListModel windowModel = (ListModel) getWindow();
//
//        List<ClusterNetworkManageModel> manageList = Linq.<ClusterNetworkManageModel> Cast(windowModel.getItems());
//        List<Network> existingClusterNetworks = Linq.<Network> Cast(getItems());
//        final ArrayList<VdcActionParametersBase> toAttach = new ArrayList<VdcActionParametersBase>();
//        final ArrayList<VdcActionParametersBase> toDetach = new ArrayList<VdcActionParametersBase>();
//
//        for (ClusterNetworkManageModel networkModel : manageList) {
//            Network network = networkModel.getEntity();
//            boolean contains = existingClusterNetworks.contains(network);
//
//            boolean needsAttach = networkModel.isAttached() && !contains;
//            boolean needsDetach = !networkModel.isAttached() && contains;
//            boolean needsUpdate = false;
//
//            if (contains && !needsDetach) {
//                Network clusterNetwork = existingClusterNetworks.get(existingClusterNetworks.indexOf(network));
//
//                if ((networkModel.isRequired() != clusterNetwork.getCluster().isRequired())
//                        || (networkModel.isDisplayNetwork() != clusterNetwork.getCluster().getis_display())) {
//                    needsUpdate = true;
//                }
//            }
//
//            if (needsAttach || needsUpdate) {
//                toAttach.add(new AttachNetworkToVdsGroupParameter(getEntity(), network));
//            }
//
//            if (needsDetach) {
//                toDetach.add(new AttachNetworkToVdsGroupParameter(getEntity(), network));
//            }
//        }
//
//        final IFrontendMultipleActionAsyncCallback callback = new IFrontendMultipleActionAsyncCallback() {
//            Boolean needsAttach = !toAttach.isEmpty();
//            Boolean needsDetach = !toDetach.isEmpty();
//
//            @Override
//            public void Executed(FrontendMultipleActionAsyncResult result) {
//                if (result.getActionType() == VdcActionType.DetachNetworkToVdsGroup) {
//                    needsDetach = false;
//                }
//                if (result.getActionType() == VdcActionType.AttachNetworkToVdsGroup) {
//                    needsAttach = false;
//                }
//
//                if (needsAttach) {
//                    Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, toAttach, this, null);
//                }
//
//                if (needsDetach) {
//                    Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, toDetach, this, null);
//                }
//
//                if (!needsAttach && !needsDetach) {
//                    doFinish();
//                }
//            }
//
//            private void doFinish() {
//                windowModel.StopProgress();
//                Cancel();
//                ForceRefresh();
//            }
//        };
//
//        callback.Executed(new FrontendMultipleActionAsyncResult(null, null, null));
//        windowModel.StartProgress(null);
    }

    @Override
    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    public NetworkView getEntity()
    {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
    }

    public void setEntity(NetworkView value)
    {
        super.setEntity(value);
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
    protected void SyncSearch() {
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
                NetworkClusterListModel.this.setItems((ArrayList<VDSGroup>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        NetworkIdParameters networkIdParams = new NetworkIdParameters(getEntity().getNetwork().getId());
        networkIdParams.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetVdsGroupsByNetworkId, networkIdParams, _asyncQuery);
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getManageCommand())
        {
            Manage();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnManage")) //$NON-NLS-1$
        {
            OnManage();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "NetworkClusterListModel"; //$NON-NLS-1$
    }
}

