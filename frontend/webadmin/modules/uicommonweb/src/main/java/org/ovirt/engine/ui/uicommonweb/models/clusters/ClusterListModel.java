package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class ClusterListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

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

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand()
    {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value)
    {
        privateGuideCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VDSGroup>().Select(a =>
    // a.ID).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            java.util.ArrayList<Object> items = new java.util.ArrayList<Object>();
            for (Object i : getSelectedItems())
            {
                items.add(((VDSGroup) i).getID());
            }
            return items.toArray(new Object[] {});
        }
    }

    private Object privateGuideContext;

    public Object getGuideContext()
    {
        return privateGuideContext;
    }

    public void setGuideContext(Object value)
    {
        privateGuideContext = value;
    }

    public ClusterListModel()
    {
        setTitle("Clusters");

        setDefaultSearchString("Cluster:");
        setSearchString(getDefaultSearchString());

        setNewCommand(new UICommand("New", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));
        setGuideCommand(new UICommand("Guide", this));

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void Guide()
    {
        ClusterGuideModel model = new ClusterGuideModel();
        setWindow(model);
        model.setTitle("New Cluster - Guide Me");
        model.setHashName("new_cluster_-_guide_me");

        if (getGuideContext() == null) {
            VDSGroup cluster = (VDSGroup) getSelectedItem();
            setGuideContext(cluster.getID());
        }

        AsyncDataProvider.GetClusterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterListModel clusterListModel = (ClusterListModel) target;
                        ClusterGuideModel model = (ClusterGuideModel) clusterListModel.getWindow();
                        model.setEntity((VDSGroup) returnValue);

                        UICommand tempVar = new UICommand("Cancel", clusterListModel);
                        tempVar.setTitle("Configure Later");
                        tempVar.setIsDefault(true);
                        tempVar.setIsCancel(true);
                        model.getCommands().add(tempVar);
                    }
                }), (Guid) getGuideContext());
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new ClusterPolicyModel());
        list.add(new ClusterHostListModel());
        list.add(new ClusterVmListModel());
        list.add(new ClusterNetworkListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("cluster");
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Cluster);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.Cluster, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterModel clusterModel = new ClusterModel();
        clusterModel.Init(false);
        setWindow(clusterModel);
        clusterModel.setTitle("New Cluster");
        clusterModel.setHashName("new_cluster");
        clusterModel.setIsNew(true);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterListModel clModel = (ClusterListModel) model;
                ClusterModel cModel = (ClusterModel) clModel.getWindow();
                java.util.ArrayList<storage_pool> dataCenters = (java.util.ArrayList<storage_pool>) result;

                cModel.getDataCenter().setItems(dataCenters);

                // Be aware of system tree selection.
                // Strict data center as neccessary.
                if (clModel.getSystemTreeSelectedItem() != null
                        && clModel.getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
                {
                    storage_pool selectDataCenter = (storage_pool) clModel.getSystemTreeSelectedItem().getEntity();

                    cModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters,
                            new Linq.DataCenterPredicate(selectDataCenter.getId())));
                    cModel.getDataCenter().setIsChangable(false);
                }
                else
                {
                    cModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                }

                UICommand tempVar = new UICommand("OnSave", clModel);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                cModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", clModel);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                cModel.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);
    }

    public void Edit()
    {
        VDSGroup cluster = (VDSGroup) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        ClusterModel model = new ClusterModel();
        model.Init(true);
        model.setEntity(cluster);
        setWindow(model);
        model.setTitle("Edit Cluster");
        model.setHashName("edit_cluster");
        model.setOriginalName(cluster.getname());
        model.getName().setEntity(cluster.getname());

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster)
        {
            model.getName().setIsChangable(false);
            model.getName().setInfo("Cannot edit Cluster's Name in tree context");
            ;
        }

        UICommand tempVar = new UICommand("OnSave", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Cluster(s)");
        model.setHashName("remove_cluster");
        model.setMessage("Cluster(s)");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (VDSGroup a : Linq.<VDSGroup> Cast(getSelectedItems()))
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
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object a : getSelectedItems())
        {
            prms.add(new VdsGroupParametersBase(((VDSGroup) a).getID()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVdsGroup, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    public void OnSave()
    {
        ClusterModel model = (ClusterModel) getWindow();
        if (!model.Validate())
        {
            return;
        }

        if (!model.getIsNew()
                && !((Version) model.getVersion().getSelectedItem()).equals(((VDSGroup) getSelectedItem()).getcompatibility_version()))
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle("Change Cluster Compatibility Version");
            confirmModel.setHashName("change_cluster_compatibility_version");
            confirmModel.setMessage("You are about to change the Cluster Compatibility Version. Are you sure you want to continue?");

            UICommand tempVar = new UICommand("OnSaveInternal", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this);
            tempVar2.setTitle("Cancel");
            tempVar2.setIsCancel(true);
            getConfirmWindow().getCommands().add(tempVar2);
        }
        else
        {
            OnSaveInternal();
        }
    }

    public void OnSaveInternal()
    {
        ClusterModel model = (ClusterModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        // cancel confirm window if there is
        CancelConfirmation();

        VDSGroup cluster = model.getIsNew() ? new VDSGroup() : (VDSGroup) Cloner.clone(getSelectedItem());

        Version version = (Version) model.getVersion().getSelectedItem();

        cluster.setname((String) model.getName().getEntity());
        cluster.setdescription((String) model.getDescription().getEntity());
        cluster.setstorage_pool_id(((storage_pool) model.getDataCenter().getSelectedItem()).getId());
        cluster.setcpu_name(((ServerCpu) model.getCPU().getSelectedItem()).getCpuName());
        cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
        cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
        cluster.setcompatibility_version(version);
        cluster.setMigrateOnError(model.getMigrateOnErrorOption());

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddVdsGroup : VdcActionType.UpdateVdsGroup,
                new VdsGroupOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        ClusterListModel localModel = (ClusterListModel) result.getState();
                        localModel.PostOnSaveInternal(result.getReturnValue());

                    }
                },
                this);
    }

    public void PostOnSaveInternal(VdcReturnValueBase returnValue)
    {
        ClusterModel model = (ClusterModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();

            if (model.getIsNew())
            {
                setGuideContext(returnValue.getActionReturnValue());
                UpdateActionAvailability();
                getGuideCommand().Execute();
            }
        }
    }

    public void Cancel()
    {
        CancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        UpdateActionAvailability();
    }

    public void CancelConfirmation()
    {
        setConfirmWindow(null);
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

    @Override
    protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.ItemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster)
        {
            VDSGroup cluster = (VDSGroup) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.FirstOrDefault(Linq.<VDSGroup> Cast(getItems()),
                    new Linq.ClusterPredicate(cluster.getID())));
        }
    }

    private void UpdateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
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
        else if (command == getGuideCommand())
        {
            Guide();
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
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternal"))
        {
            OnSaveInternal();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
        {
            CancelConfirmation();
        }
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged()
    {
        UpdateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "ClusterListModel";
    }
}
