package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.Collections;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.DisplayNetworkToVdsGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
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
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
                searchableListModel.setItems((java.util.ArrayList<network>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
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
        network network = (network) getSelectedItem();

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
                java.util.ArrayList<network> networkList = (java.util.ArrayList<network>) result;
                ListModel listModel = new ListModel();
                clusterNetworkListModel.setWindow(listModel);
                listModel.setTitle(ConstantsManager.getInstance().getConstants().assignDetachNetworksTitle());
                listModel.setHashName("assign_networks"); //$NON-NLS-1$
                clusterNetworkListModel.PostManage(networkList, listModel);
            }
        };
        AsyncDataProvider.GetNetworkList(_asyncQuery, storagePoolId);
    }

    public void PostManage(java.util.ArrayList<network> networkList, ListModel model)
    {
        Collections.sort(networkList, new Linq.NetworkByNameComparer());

        java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
        for (network a : networkList)
        {
            if (!a.getname().equals("engine")) //$NON-NLS-1$
            {
                EntityModel tempVar = new EntityModel();
                tempVar.setEntity(a);
                tempVar.setTitle(a.getname());
                items.add(tempVar);
            }
        }
        model.setItems(items);

        boolean noItems = items.isEmpty();

        java.util.ArrayList<network> networks = Linq.<network> Cast(getItems());
        java.util.ArrayList<EntityModel> selectedItems = new java.util.ArrayList<EntityModel>();
        for (EntityModel item : items)
        {
            network net = (network) item.getEntity();
            boolean value = false;
            for (network a : networks)
            {
                if (a.getId().equals(net.getId()))
                {
                    value = true;
                    break;
                }
            }
            item.setIsSelected(value);
            if (value) {
                selectedItems.add(item);
            }
        }

        model.setSelectedItems(selectedItems);

        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsDefault(noItems);
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);

        if (!noItems)
        {
            UICommand tempVar3 = new UICommand("OnManage", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar3.setIsDefault(true);
            model.getCommands().add(0, tempVar3);
        }
    }

    public void OnManage()
    {
        ListModel model = (ListModel) getWindow();

        java.util.ArrayList<EntityModel> items = Linq.<EntityModel> Cast(model.getItems());
        java.util.ArrayList<network> networks = Linq.<network> Cast(getItems());

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> prms1 = new java.util.ArrayList<VdcActionParametersBase>();
        for (EntityModel a : items)
        {
            boolean value = false;
            for (network b : networks)
            {
                if (b.getId().equals(((network) a.getEntity()).getId()))
                {
                    value = true;
                    break;
                }
            }
            if (a.getIsSelected() && !value)
            {
                prms1.add(new AttachNetworkToVdsGroupParameter(getEntity(), (network) a.getEntity()));
            }
        }

        // Call the command only if necessary (i.e. only if there are paramters):
        if (prms1.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, prms1);
        }

        java.util.ArrayList<VdcActionParametersBase> prms2 = new java.util.ArrayList<VdcActionParametersBase>();
        for (EntityModel a : items)
        {
            boolean value = true;
            for (network b : networks)
            {
                if (b.getId().equals(((network) a.getEntity()).getId()))
                {
                    value = false;
                    break;
                }
            }
            if (!a.getIsSelected() && !value)
            {
                prms2.add(new AttachNetworkToVdsGroupParameter(getEntity(), (network) a.getEntity()));
            }
        }

        // Call the command only if necessary (i.e. only if there are paramters):
        if (prms2.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, prms2);
        }

        Cancel();
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
        network network = (network) getSelectedItem();

        // CanRemove = SelectedItems != null && SelectedItems.Count > 0;
        getSetAsDisplayCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && network != null && !(network.getis_display() == null ? false : network.getis_display())
                && network.getStatus() != NetworkStatus.NonOperational);
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterNetworkModel clusterModel = new ClusterNetworkModel();
        setWindow(clusterModel);
        clusterModel.setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        clusterModel.setHashName("new_logical_network"); //$NON-NLS-1$
        clusterModel.setIsNew(true);
        if (getEntity().getstorage_pool_id() != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(clusterModel);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    ClusterNetworkModel clusterNetworkModel = (ClusterNetworkModel) model;
                    storage_pool dataCenter = (storage_pool) result;
                    clusterNetworkModel.setDataCenterName(dataCenter.getname());
                }
            };
            AsyncDataProvider.GetDataCenterById(_asyncQuery, getEntity().getstorage_pool_id().getValue());
        }
        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        clusterModel.getCommands().add(tempVar);

        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        clusterModel.getCommands().add(tempVar2);
    }

    public void OnSave()
    {
        ClusterNetworkModel model = (ClusterNetworkModel) getWindow();
        network network = new network(null);

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        if (!model.Validate() || getEntity().getstorage_pool_id() == null)
        {
            return;
        }

        network.setstorage_pool_id(getEntity().getstorage_pool_id());
        network.setname((String) model.getName().getEntity());
        network.setstp((Boolean) model.getIsStpEnabled().getEntity());
        network.setdescription((String) model.getDescription().getEntity());
        network.setVmNetwork((Boolean) model.getIsVmNetwork().getEntity());

        network.setMtu(0);
        if (model.getMtu().getEntity() != null)
        {
            network.setMtu(Integer.parseInt(model.getMtu().getEntity().toString()));
        }

        network.setvlan_id(null);
        if ((Boolean) model.getHasVLanTag().getEntity())
        {
            network.setvlan_id(Integer.parseInt(model.getVLanTag().getEntity().toString()));
        }

        Frontend.RunAction(VdcActionType.AddNetwork, new AddNetworkStoragePoolParameters(network.getstorage_pool_id()
                .getValue(), network),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        Object[] data = (Object[]) result.getState();
                        ClusterNetworkListModel networkListModel = (ClusterNetworkListModel) data[0];
                        VdcReturnValueBase retVal = result.getReturnValue();
                        if (retVal != null && retVal.getSucceeded())
                        {
                            network tempVar = new network(null);
                            tempVar.setId((Guid) retVal.getActionReturnValue());
                            tempVar.setname(((network) data[1]).getname());
                            Frontend.RunAction(VdcActionType.AttachNetworkToVdsGroup,
                                    new AttachNetworkToVdsGroupParameter(networkListModel.getEntity(), tempVar));
                        }
                        networkListModel.Cancel();

                    }
                }, new Object[] { this, network });
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
