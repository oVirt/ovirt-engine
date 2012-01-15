package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("unused")
public class DataCenterNetworkListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{
    private static final String ENGINE_NETWORK = "engine";

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    @Override
    public storage_pool getEntity()
    {
        return (storage_pool) super.getEntity();
    }

    public void setEntity(storage_pool value)
    {
        super.setEntity(value);
    }

    private java.util.ArrayList<VDSGroup> privateClusterList;

    public java.util.ArrayList<VDSGroup> getClusterList()
    {
        return privateClusterList;
    }

    public void setClusterList(java.util.ArrayList<VDSGroup> value)
    {
        privateClusterList = value;
    }

    private java.util.ArrayList<SelectionTreeNodeModel> privateSelectionNodeList;

    public java.util.ArrayList<SelectionTreeNodeModel> getSelectionNodeList()
    {
        return privateSelectionNodeList;
    }

    public void setSelectionNodeList(java.util.ArrayList<SelectionTreeNodeModel> value)
    {
        privateSelectionNodeList = value;
    }

    public DataCenterNetworkListModel()
    {
        setTitle("Logical Networks");

        setNewCommand(new UICommand("New", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));

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
                searchableListModel.setItems((java.util.ArrayList<network>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        GetAllNetworkQueryParamenters tempVar = new GetAllNetworkQueryParamenters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllNetworks, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllNetworks,
                new GetAllNetworkQueryParamenters(getEntity().getId())));
        setItems(getAsyncResult().getData());
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Logical Network(s)");
        model.setHashName("remove_logical_network");
        model.setMessage("Logical Network(s)");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (network a : Linq.<network> Cast(getSelectedItems()))
        {
            list.add(a.getname());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRemove()
    {
        java.util.ArrayList<VdcActionParametersBase> pb = new java.util.ArrayList<VdcActionParametersBase>();
        for (network a : Linq.<network> Cast(getSelectedItems()))
        {
            pb.add(new AddNetworkStoragePoolParameters(getEntity().getId(), a));
        }
        Frontend.RunMultipleAction(VdcActionType.RemoveNetwork, pb);

        Cancel();
    }

    public void Edit()
    {
        network network = (network) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        DataCenterNetworkModel networkModel = new DataCenterNetworkModel();
        setWindow(networkModel);
        networkModel.setTitle("Edit Logical Network");
        networkModel.setHashName("edit_logical_network");
        networkModel.getName().setEntity(network.getname());
        networkModel.getDescription().setEntity(network.getdescription());
        networkModel.getIsStpEnabled().setEntity(network.getstp());
        networkModel.getHasVLanTag().setEntity(network.getvlan_id() != null);
        networkModel.getVLanTag().setEntity((network.getvlan_id() == null ? 0 : network.getvlan_id()));
        networkModel.setDetachAllCommand(new UICommand("DetachClusters", this));
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                DataCenterNetworkListModel dcNetworkModel = (DataCenterNetworkListModel) model;
                dcNetworkModel.setClusterList((java.util.ArrayList<VDSGroup>) ReturnValue);
                dcNetworkModel.setSelectionNodeList(new java.util.ArrayList<SelectionTreeNodeModel>());
                java.util.ArrayList<VdcQueryParametersBase> parametersList =
                        new java.util.ArrayList<VdcQueryParametersBase>();
                java.util.ArrayList<VdcQueryType> queryTypeList = new java.util.ArrayList<VdcQueryType>();
                for (VDSGroup vdsGroup : dcNetworkModel.getClusterList())
                {
                    queryTypeList.add(VdcQueryType.GetAllNetworksByClusterId);
                    parametersList.add(new VdsGroupQueryParamenters(vdsGroup.getID()));
                    SelectionTreeNodeModel tempVar = new SelectionTreeNodeModel();
                    tempVar.setIsSelectedNullable(false);
                    tempVar.setEntity(vdsGroup);
                    tempVar.setDescription(vdsGroup.getname());
                    dcNetworkModel.getSelectionNodeList().add(tempVar);
                }
                Frontend.RunMultipleQueries(queryTypeList, parametersList, dcNetworkModel);
                DataCenterNetworkModel networkModel1 = (DataCenterNetworkModel) dcNetworkModel.getWindow();

                // cannot detach engine networks from clusters
                network network1 = (network) dcNetworkModel.getSelectedItem();
                if (StringHelper.stringsEqual(network1.getname(), ENGINE_NETWORK))
                {
                    for (SelectionTreeNodeModel nodeModel : dcNetworkModel.getSelectionNodeList())
                    {
                        nodeModel.setIsChangable(false);
                    }
                    networkModel1.getDetachAllCommand().setIsAvailable(false);
                    networkModel1.getName().setIsChangable(false);
                    networkModel1.setMessage("Cannot detach Management Network from Clusters");
                }
            }
        };
        AsyncDataProvider.GetClusterList(_asyncQuery, getEntity().getId());
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        DataCenterNetworkModel networkModel = new DataCenterNetworkModel();
        setWindow(networkModel);
        networkModel.setTitle("New Logical Network");
        networkModel.setHashName("new_logical_network");
        networkModel.setIsNew(true);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) model;
                DataCenterNetworkModel networkModel1 = (DataCenterNetworkModel) networkListModel.getWindow();
                // networkModel1.ClusterTreeNodes
                java.util.ArrayList<VDSGroup> clusterList = (java.util.ArrayList<VDSGroup>) ReturnValue;
                SelectionTreeNodeModel nodeModel;
                java.util.ArrayList<SelectionTreeNodeModel> clusterTreeNodes =
                        new java.util.ArrayList<SelectionTreeNodeModel>();
                for (VDSGroup selectionTreeNodeModel : clusterList)
                {
                    nodeModel = new SelectionTreeNodeModel();
                    nodeModel.setEntity(selectionTreeNodeModel);
                    nodeModel.setDescription(selectionTreeNodeModel.getname());
                    nodeModel.setIsSelectedNullable(false);
                    clusterTreeNodes.add(nodeModel);
                }
                networkModel1.setClusterTreeNodes(clusterTreeNodes);

                UICommand tempVar = new UICommand("OnSave", networkListModel);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                networkModel1.getCommands().add(tempVar);
                networkModel1.setDetachAllCommand(new UICommand("DetachClusters", networkListModel));
                networkModel1.getDetachAllAvailable().setEntity(false);
                UICommand tempVar2 = new UICommand("Cancel", networkListModel);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                networkModel1.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.GetClusterList(_asyncQuery, getEntity().getId());
    }

    public void OnSave()
    {
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();

        if (getEntity() == null || (!model.getIsNew() && getSelectedItem() == null))
        {
            Cancel();
            return;
        }

        model.setcurrentNetwork(model.getIsNew() ? new network(null) : (network) Cloner.clone(getSelectedItem()));

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        model.getcurrentNetwork().setstorage_pool_id(getEntity().getId());
        model.getcurrentNetwork().setname((String) model.getName().getEntity());
        model.getcurrentNetwork().setstp((Boolean) model.getIsStpEnabled().getEntity());
        model.getcurrentNetwork().setdescription((String) model.getDescription().getEntity());
        model.getcurrentNetwork().setvlan_id(null);

        if ((Boolean) model.getHasVLanTag().getEntity())
        {
            model.getcurrentNetwork().setvlan_id(Integer.parseInt(model.getVLanTag().getEntity().toString()));
        }

        model.setnewClusters(new java.util.ArrayList<VDSGroup>());

        for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
        {
            // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
            // null-value logic:
            if (selectionTreeNodeModel.getIsSelectedNullable() != null
                    && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
            {
                model.getnewClusters().add((VDSGroup) selectionTreeNodeModel.getEntity());
            }
        }
        java.util.ArrayList<VDSGroup> detachNetworkFromClusters =
                Linq.Except(model.getOriginalClusters(), model.getnewClusters());
        java.util.ArrayList<VdcActionParametersBase> actionParameters =
                new java.util.ArrayList<VdcActionParametersBase>();

        for (VDSGroup detachNetworkFromCluster : detachNetworkFromClusters)
        {
            actionParameters.add(new AttachNetworkToVdsGroupParameter(detachNetworkFromCluster,
                    model.getcurrentNetwork()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) result.getState();
                        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) networkListModel.getWindow();
                        network network = networkModel.getcurrentNetwork();
                        if (networkModel.getIsNew())
                        {
                            Frontend.RunAction(VdcActionType.AddNetwork,
                                    new AddNetworkStoragePoolParameters(getEntity().getId(), network),
                                    new IFrontendActionAsyncCallback() {
                                        @Override
                                        public void Executed(FrontendActionAsyncResult result1) {

                                            DataCenterNetworkListModel networkListModel1 =
                                                    (DataCenterNetworkListModel) result1.getState();
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
                                    networkListModel);
                        }
                        else
                        {
                            if ((Boolean) networkModel.getIsEnabled().getEntity())
                            {
                                Frontend.RunAction(VdcActionType.UpdateNetwork,
                                        new AddNetworkStoragePoolParameters(getEntity().getId(), network),
                                        new IFrontendActionAsyncCallback() {
                                            @Override
                                            public void Executed(FrontendActionAsyncResult result1) {

                                                DataCenterNetworkListModel networkListModel1 =
                                                        (DataCenterNetworkListModel) result1.getState();
                                                VdcReturnValueBase retVal = result1.getReturnValue();
                                                networkListModel1.PostNetworkAction(null,
                                                        retVal != null && retVal.getSucceeded());

                                            }
                                        },
                                        networkListModel);
                            }
                            else
                            {
                                PostNetworkAction(null, true);
                            }
                        }

                    }
                },
                this);
    }

    public void PostNetworkAction(Guid networkGuid, boolean succeeded)
    {
        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) getWindow();
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
        network network = networkModel.getcurrentNetwork();
        Guid networkId = networkModel.getIsNew() ? networkGuid : network.getId();
        java.util.ArrayList<VDSGroup> attachNetworkToClusters =
                Linq.Except(networkModel.getnewClusters(), networkModel.getOriginalClusters());
        java.util.ArrayList<VdcActionParametersBase> actionParameters1 =
                new java.util.ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : attachNetworkToClusters)
        {
            network tempVar = new network(null);
            tempVar.setId(networkId);
            tempVar.setname(network.getname());
            actionParameters1.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar));
        }

        Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
    }

    public void DetachClusters()
    {
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle("Detach Network from ALL Clusters");
        confirmModel.setHashName("detach_network_from_all_clusters");
        confirmModel.setMessage("You are about to detach the Network from all of the Clusters to which it is currentlyattached.\nAs a result, the Clusters' Hosts might become unreachable.\n\nAre you sure you want to continue?");
        confirmModel.getLatch().setIsAvailable(true);
        UICommand tempVar = new UICommand("OnDetachClusters", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        confirmModel.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("CancelConfirmation", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        confirmModel.getCommands().add(tempVar2);
    }

    public void CancelConfirmation()
    {
        setConfirmWindow(null);
    }

    public void OnDetachClusters()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (!confirmationModel.Validate())
        {
            return;
        }
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();
        network network = (network) getSelectedItem();

        java.util.ArrayList<VdcActionParametersBase> actionParameters =
                new java.util.ArrayList<VdcActionParametersBase>();

        for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
        {
            // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
            // null-value logic:
            if (selectionTreeNodeModel.getIsSelectedNullable() != null
                    && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
            {
                selectionTreeNodeModel.setIsSelectedNullable(false);
                actionParameters.add(new AttachNetworkToVdsGroupParameter((VDSGroup) selectionTreeNodeModel.getEntity(),
                        network));
            }
        }
        Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) result.getState();
                        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) networkListModel.getWindow();
                        boolean isSucceded = true;
                        networkListModel.CancelConfirmation();
                        if (isSucceded)
                        {
                            networkModel.setOriginalClusters(new java.util.ArrayList<VDSGroup>());
                            networkModel.getIsEnabled().setEntity(true);
                            networkModel.getDetachAllAvailable().setEntity(!(Boolean) networkModel.getIsEnabled()
                                    .getEntity());
                        }
                        else
                        {
                            networkListModel.Cancel();
                        }

                    }
                },
                this);
    }

    public void Cancel()
    {
        setWindow(null);
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
        java.util.List tempVar = getSelectedItems();
        java.util.ArrayList selectedItems =
                (java.util.ArrayList) ((tempVar != null) ? tempVar : new java.util.ArrayList());

        boolean anyEngine = false;
        for (Object item : selectedItems)
        {
            network network = (network) item;
            if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
            {
                anyEngine = true;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "DetachClusters"))
        {
            DetachClusters();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDetachClusters"))
        {
            OnDetachClusters();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
        {
            CancelConfirmation();
        }
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        network network = (network) getSelectedItem();
        java.util.List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();
        java.util.ArrayList<network> clusterNetworkList = null;
        boolean networkHasAttachedClusters = false;
        for (int i = 0; i < returnValueList.size(); i++)
        {
            VdcQueryReturnValue returnValue = returnValueList.get(i);
            if (returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                clusterNetworkList = (java.util.ArrayList<network>) returnValue.getReturnValue();
                for (network clusterNetwork : clusterNetworkList)
                {
                    if (clusterNetwork.getId().equals(network.getId()))
                    {
                        model.getOriginalClusters().add((VDSGroup) getSelectionNodeList().get(i).getEntity());
                        getSelectionNodeList().get(i).setIsSelectedNullable(true);
                        networkHasAttachedClusters = true;
                        break;
                    }
                }
            }
        }
        if (networkHasAttachedClusters)
        {
            model.getIsEnabled().setEntity(false);
            if (!StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
            {
                model.getDetachAllAvailable().setEntity(!(Boolean) model.getIsEnabled().getEntity());
            }
        }

        model.setClusterTreeNodes(getSelectionNodeList());
        if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK) && getSelectionNodeList().size() > 0)
        {
            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnSave", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterNetworkListModel";
    }

}
