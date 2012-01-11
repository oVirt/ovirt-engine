package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
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
public class DataCenterListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
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

    private UICommand privateForceRemoveCommand;

    public UICommand getForceRemoveCommand()
    {
        return privateForceRemoveCommand;
    }

    private void setForceRemoveCommand(UICommand value)
    {
        privateForceRemoveCommand = value;
    }

    private UICommand privateActivateCommand;

    public UICommand getActivateCommand()
    {
        return privateActivateCommand;
    }

    private void setActivateCommand(UICommand value)
    {
        privateActivateCommand = value;
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

    private UICommand privateRecoveryStorageCommand;

    public UICommand getRecoveryStorageCommand()
    {
        return privateRecoveryStorageCommand;
    }

    private void setRecoveryStorageCommand(UICommand value)
    {
        privateRecoveryStorageCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<storage_pool>().Select(a =>
    // a.id).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            java.util.ArrayList<Object> objL = new java.util.ArrayList<Object>();
            for (storage_pool a : Linq.<storage_pool> Cast(getSelectedItems()))
            {
                objL.add(a.getId());
            }
            return objL.toArray(new Object[] {});
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

    public DataCenterListModel()
    {
        setTitle("Data Centers");

        setDefaultSearchString("DataCenter:");
        setSearchString(getDefaultSearchString());

        setNewCommand(new UICommand("New", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));
        UICommand tempVar = new UICommand("ForceRemove", this);
        tempVar.setIsExecutionAllowed(true);
        setForceRemoveCommand(tempVar);
        setRecoveryStorageCommand(new UICommand("RecoveryStorage", this));
        setActivateCommand(new UICommand("Activate", this));
        setGuideCommand(new UICommand("Guide", this));

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void Guide()
    {
        DataCenterGuideModel model = new DataCenterGuideModel();
        setWindow(model);
        model.setTitle("New Data Center - Guide Me");
        model.setHashName("new_data_center_-_guide_me");
        if (getGuideContext() == null) {
            storage_pool dataCenter = (storage_pool) getSelectedItem();
            setGuideContext(dataCenter.getId());
        }

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterListModel dataCenterListModel = (DataCenterListModel) target;
                        DataCenterGuideModel model = (DataCenterGuideModel) dataCenterListModel.getWindow();
                        model.setEntity((storage_pool) returnValue);

                        UICommand tempVar = new UICommand("Cancel", dataCenterListModel);
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
        list.add(new DataCenterStorageListModel());
        list.add(new DataCenterNetworkListModel());
        list.add(new DataCenterClusterListModel());
        list.add(new PermissionListModel());
        list.add(new DataCenterEventListModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("datacenter");
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.StoragePool);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);

    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.StoragePool, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        DataCenterModel model = new DataCenterModel();
        setWindow(model);
        model.setTitle("New Data Center");
        model.setHashName("new_data_center");
        model.setIsNew(true);
        model.getStorageTypeList().setSelectedItem(StorageType.NFS);

        UICommand tempVar = new UICommand("OnSave", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void Edit()
    {
        storage_pool dataCenter = (storage_pool) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        DataCenterModel model = new DataCenterModel();
        setWindow(model);
        model.setEntity(dataCenter);
        model.setDataCenterId(dataCenter.getId());
        model.setTitle("Edit Data Center");
        model.setHashName("edit_data_center");
        model.getName().setEntity(dataCenter.getname());

        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter)
        {
            model.getName().setIsChangable(false);
            model.getName().setInfo("Cannot edit Data Center's Name in tree context");
        }

        model.getDescription().setEntity(dataCenter.getdescription());
        model.setOriginalName(dataCenter.getname());
        if (DataProvider.GetStorageDomainList(dataCenter.getId()).size() != 0)
        {
            model.getStorageTypeList().setIsChangable(false);
            model.getStorageTypeList()
                    .getChangeProhibitionReasons()
                    .add("Cannot change Repository type with Storage Domains attached to it");
        }

        model.getStorageTypeList().setSelectedItem(dataCenter.getstorage_pool_type());

        // Version
        // foreach (object a in model.Version.Items)
        // {
        // Version item = (Version)a;
        // if (item == dataCenter.compatibility_version)
        // {
        // model.Version.SelectedItem = item;
        // break;
        // }
        // }

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
        model.setTitle("Remove Data Center(s)");
        model.setHashName("remove_data_center");
        model.setMessage("Data Center(s)");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (storage_pool a : Linq.<storage_pool> Cast(getSelectedItems()))
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

    public void ForceRemove()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Force Remove Data Center");
        model.setHashName("force_remove_data_center");
        model.setMessage("Data Center(s)");
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangable(true);

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (storage_pool a : Linq.<storage_pool> Cast(getSelectedItems()))
        {
            list.add(a.getname());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnForceRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void RecoveryStorage()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Data Center Re-Initialize");
        model.setHashName("data_center_re-initialize");
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangable(true);

        // IEnumerable<storage_domains> list = DataProvider.GetStorageDomainList();
        // List<EntityModel> models = list
        // .Where(a => (a.storage_domain_type == StorageDomainType.Data && a.storage_type ==
        // ((storage_pool)SelectedItem).storage_pool_type) &&
        // (a.storage_domain_shared_status == StorageDomainSharedStatus.Unattached)
        // )
        // .Select(a => new EntityModel() { Entity = a })
        // .ToList();
        java.util.ArrayList<EntityModel> models = new java.util.ArrayList<EntityModel>();
        for (storage_domains a : DataProvider.GetStorageDomainList())
        {
            if (a.getstorage_domain_type() == StorageDomainType.Data
                    && a.getstorage_type() == ((storage_pool) getSelectedItem()).getstorage_pool_type()
                    && (a.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached))
            {
                EntityModel tempVar = new EntityModel();
                tempVar.setEntity(a);
                models.add(tempVar);
            }
        }

        model.setItems(models);

        if (models.size() > 0)
        {
            EntityModel entityModel = models.size() != 0 ? models.get(0) : null;
            if (entityModel != null)
            {
                entityModel.setIsSelected(true);
            }
        }

        if (models.isEmpty())
        {
            model.setMessage("There are no compatible Storage Domains to attach to this Data Center. Please add new Storage from the Storage tab.");

            UICommand tempVar2 = new UICommand("Cancel", this);
            tempVar2.setTitle("Close");
            tempVar2.setIsDefault(true);
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
        }
        else
        {
            UICommand tempVar3 = new UICommand("OnRecover", this);
            tempVar3.setTitle("OK");
            tempVar3.setIsDefault(true);
            model.getCommands().add(tempVar3);
            UICommand tempVar4 = new UICommand("Cancel", this);
            tempVar4.setTitle("Cancel");
            tempVar4.setIsCancel(true);
            model.getCommands().add(tempVar4);
        }
    }

    public void OnRecover()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        // storage_domains master =
        // DataProvider.GetStorageDomainList(((storage_pool)SelectedItem).id).FirstOrDefault(a=>a.storage_domain_type ==
        // StorageDomainType.Master);
        storage_domains master = null;
        for (storage_domains a : DataProvider.GetStorageDomainList(((storage_pool) getSelectedItem()).getId()))
        {
            if (a.getstorage_domain_type() == StorageDomainType.Master)
            {
                master = a;
                break;
            }
        }

        java.util.ArrayList<storage_domains> items = new java.util.ArrayList<storage_domains>();
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((storage_domains) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            if (model.getProgress() != null)
            {
                return;
            }

            java.util.ArrayList<VdcActionParametersBase> parameters =
                    new java.util.ArrayList<VdcActionParametersBase>();
            for (storage_domains a : items)
            {
                parameters.add(new RecoveryStoragePoolParameters(((storage_pool) getSelectedItem()).getId(), a.getid()));
            }

            model.StartProgress(null);

            Frontend.RunMultipleAction(VdcActionType.RecoveryStoragePool, parameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.StopProgress();
                            Cancel();

                        }
                    }, model);
        }
        else
        {
            Cancel();
        }
    }

    public void Activate()
    {
        // Frontend.RunMultipleActions(VdcActionType.ActivateStoragePool,
        // SelectedItems.Cast<storage_pool>()
        // .Select(a => (VdcActionParametersBase)new StoragePoolParametersBase(a.id))
        // .ToList()
        // );
    }

    public void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
        for (storage_pool a : Linq.<storage_pool> Cast(getSelectedItems()))
        {
            parameters.add(new StoragePoolParametersBase(a.getId()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveStoragePool, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    public void OnForceRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        if (!model.Validate())
        {
            return;
        }
        VdcActionParametersBase parametersBase = new VdcActionParametersBase();
        StoragePoolParametersBase tempVar = new StoragePoolParametersBase(((storage_pool) getSelectedItem()).getId());
        tempVar.setForceDelete(true);
        parametersBase = tempVar;
        Frontend.RunAction(VdcActionType.RemoveStoragePool, parametersBase);
        Cancel();
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

    public void OnSave()
    {
        DataCenterModel model = (DataCenterModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        if (!model.getIsNew()
                && !((Version) model.getVersion().getSelectedItem()).equals(((storage_pool) getSelectedItem()).getcompatibility_version()))
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle("Change Data Center Compatibility Version");
            confirmModel.setHashName("change_data_center_compatibility_version");
            confirmModel.setMessage("You are about to change the Data Center Compatibility Version. Are you sure you want to continue?");

            UICommand tempVar = new UICommand("OnSaveInternal", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this);
            tempVar2.setTitle("Cancel");
            tempVar2.setIsCancel(true);
            confirmModel.getCommands().add(tempVar2);
        }
        else
        {
            OnSaveInternal();
        }
    }

    public void OnSaveInternal()
    {
        DataCenterModel model = (DataCenterModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        storage_pool dataCenter =
                model.getIsNew() ? new storage_pool() : (storage_pool) Cloner.clone(getSelectedItem());

        // cancel confirm window if there is
        CancelConfirmation();

        // Save changes.
        dataCenter.setname((String) model.getName().getEntity());
        dataCenter.setdescription((String) model.getDescription().getEntity());
        dataCenter.setstorage_pool_type((StorageType) model.getStorageTypeList().getSelectedItem());
        dataCenter.setcompatibility_version((Version) model.getVersion().getSelectedItem());

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddEmptyStoragePool : VdcActionType.UpdateStoragePool,
                new StoragePoolManagementParameter(dataCenter),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        DataCenterListModel localModel = (DataCenterListModel) result.getState();
                        localModel.PostOnSaveInternal(result.getReturnValue());

                    }
                },
                this);
    }

    public void PostOnSaveInternal(VdcReturnValueBase returnValue)
    {
        DataCenterModel model = (DataCenterModel) getWindow();

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
        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter)
        {
            storage_pool dataCenter = (storage_pool) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.FirstOrDefault(Linq.<storage_pool> Cast(getItems()),
                    new Linq.DataCenterPredicate(dataCenter.getId())));
        }
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        java.util.ArrayList<storage_pool> items =
                getSelectedItems() != null ? new java.util.ArrayList<storage_pool>(Linq.<storage_pool> Cast(getSelectedItems()))
                        : new java.util.ArrayList<storage_pool>();

        boolean isAllDown = true;
        for (storage_pool item : items)
        {
            if (item.getstatus() == StoragePoolStatus.Up || item.getstatus() == StoragePoolStatus.Contend)
            {
                isAllDown = false;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && items.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && isAllDown);

        storage_pool storagePoolItem = (getSelectedItem() != null ? (storage_pool) getSelectedItem() : null);

        getForceRemoveCommand().setIsExecutionAllowed(storagePoolItem != null && items.size() == 1
                && storagePoolItem.getstatus() != StoragePoolStatus.Up
                && storagePoolItem.getstatus() != StoragePoolStatus.Contend
                && storagePoolItem.getstatus() != StoragePoolStatus.Uninitialized);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getActivateCommand().setIsExecutionAllowed(items.size() > 0);
        if (getActivateCommand().getIsExecutionAllowed())
        {
            for (storage_pool a : items)
            {
                if (a.getstatus() == StoragePoolStatus.Up || a.getstatus() == StoragePoolStatus.Uninitialized)
                {
                    getActivateCommand().setIsExecutionAllowed(false);
                    break;
                }
            }
        }

        getRecoveryStorageCommand().setIsExecutionAllowed(items != null && items.size() == 1);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter);

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
        else if (command == getForceRemoveCommand())
        {
            ForceRemove();
        }
        else if (command == getActivateCommand())
        {
            Activate();
        }
        else if (command == getGuideCommand())
        {
            Guide();
        }
        else if (command == getRecoveryStorageCommand())
        {
            RecoveryStorage();
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
        else if (StringHelper.stringsEqual(command.getName(), "OnForceRemove"))
        {
            OnForceRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternal"))
        {
            OnSaveInternal();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
        {
            CancelConfirmation();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRecover"))
        {
            OnRecover();
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
        return "DataCenterListModel";
    }
}
