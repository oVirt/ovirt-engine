package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class DataCenterStorageListModel extends SearchableListModel
{

    private UICommand privateAttachStorageCommand;

    public UICommand getAttachStorageCommand()
    {
        return privateAttachStorageCommand;
    }

    private void setAttachStorageCommand(UICommand value)
    {
        privateAttachStorageCommand = value;
    }

    private UICommand privateAttachISOCommand;

    public UICommand getAttachISOCommand()
    {
        return privateAttachISOCommand;
    }

    private void setAttachISOCommand(UICommand value)
    {
        privateAttachISOCommand = value;
    }

    private UICommand privateAttachBackupCommand;

    public UICommand getAttachBackupCommand()
    {
        return privateAttachBackupCommand;
    }

    private void setAttachBackupCommand(UICommand value)
    {
        privateAttachBackupCommand = value;
    }

    private UICommand privateDetachCommand;

    public UICommand getDetachCommand()
    {
        return privateDetachCommand;
    }

    private void setDetachCommand(UICommand value)
    {
        privateDetachCommand = value;
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

    private UICommand privateMaintenanceCommand;

    public UICommand getMaintenanceCommand()
    {
        return privateMaintenanceCommand;
    }

    private void setMaintenanceCommand(UICommand value)
    {
        privateMaintenanceCommand = value;
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

    private StorageDomainType privateStorageDomainType = getStorageDomainType().values()[0];

    public StorageDomainType getStorageDomainType()
    {
        return privateStorageDomainType;
    }

    public void setStorageDomainType(StorageDomainType value)
    {
        privateStorageDomainType = value;
    }

    // A list of 'detach' action parameters
    private ArrayList<VdcActionParametersBase> privatepb_detach;

    private ArrayList<VdcActionParametersBase> getpb_detach()
    {
        return privatepb_detach;
    }

    private void setpb_detach(ArrayList<VdcActionParametersBase> value)
    {
        privatepb_detach = value;
    }

    // A list of 'remove' action parameters
    private ArrayList<VdcActionParametersBase> privatepb_remove;

    private ArrayList<VdcActionParametersBase> getpb_remove()
    {
        return privatepb_remove;
    }

    private void setpb_remove(ArrayList<VdcActionParametersBase> value)
    {
        privatepb_remove = value;
    }

    public DataCenterStorageListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHashName("storage"); //$NON-NLS-1$

        setAttachStorageCommand(new UICommand("AttachStorage", this)); //$NON-NLS-1$
        setAttachISOCommand(new UICommand("AttachISO", this)); //$NON-NLS-1$
        setAttachBackupCommand(new UICommand("AttachBackup", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            // omer - overriding AsyncSearch - using query instead of search
            // SearchString = StringFormat.format("storage: datacenter={0}", Entity.name);
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                searchableListModel.setItems((ArrayList<StorageDomain>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        StoragePoolQueryParametersBase tempVar = new StoragePoolQueryParametersBase(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                new StoragePoolQueryParametersBase(getEntity().getId())));
        setItems(getAsyncResult().getData());
    }

    public void Maintenance()
    {
        // Frontend.RunMultipleActions(VdcActionType.DeactivateStorageDomain,
        // SelectedItems.Cast<storage_domains>()
        // .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
        // .ToList()
        // );
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
        for (StorageDomain a : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            pb.add(new StorageDomainPoolParametersBase(a.getId(), getEntity().getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.DeactivateStorageDomain, pb);
    }

    public void Activate()
    {
        // Frontend.RunMultipleActions(VdcActionType.ActivateStorageDomain,
        // SelectedItems.Cast<storage_domains>()
        // .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
        // .ToList()
        // );
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
        for (StorageDomain a : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            pb.add(new StorageDomainPoolParametersBase(a.getId(), getEntity().getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.ActivateStorageDomain, pb);
    }

    public void AttachBackup()
    {
        AttachInternal(StorageDomainType.ImportExport, ConstantsManager.getInstance()
                .getConstants()
                .attachExportDomainTitle(), "attach_export_domain"); //$NON-NLS-1$
    }

    public void AttachISO()
    {
        AttachInternal(StorageDomainType.ISO,
                ConstantsManager.getInstance().getConstants().attachISOLibraryTitle(),
                "attach_iso_library"); //$NON-NLS-1$
    }

    public void AttachStorage()
    {
        AttachInternal(StorageDomainType.Data,
                ConstantsManager.getInstance().getConstants().attachStorageTitle(),
                "attach_storage"); //$NON-NLS-1$
    }

    private void AttachInternal(StorageDomainType storageType, String title, String hashName)
    {
        if (getWindow() != null)
        {
            return;
        }

        this.setStorageDomainType(storageType);

        ListModel listModel = new ListModel();
        setWindow(listModel);
        listModel.setTitle(title);
        listModel.setHashName(hashName);
        if (storageType == StorageDomainType.ISO)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    ArrayList<StorageDomain> list = (ArrayList<StorageDomain>) result;
                    DataCenterStorageListModel dcStorageModel = (DataCenterStorageListModel) model;
                    ArrayList<EntityModel> models;
                    models = new ArrayList<EntityModel>();
                    ArrayList<StorageDomain> items =
                            dcStorageModel.getItems() != null ? new ArrayList<StorageDomain>(Linq.<StorageDomain> Cast(dcStorageModel.getItems()))
                                    : new ArrayList<StorageDomain>();
                    for (StorageDomain a : list)
                    {
                        // if (Linq.All<storage_domains>(items, delegate(storage_domains b) { return b.id != a.id; }))
                        if (!Linq.IsSDItemExistInList(items, a.getId()))
                        {
                            EntityModel tempVar = new EntityModel();
                            tempVar.setEntity(a);
                            models.add(tempVar);
                        }
                    }
                    dcStorageModel.PostAttachInternal(models);

                }
            };
            AsyncDataProvider.GetISOStorageDomainList(_asyncQuery);
        }
        else
        {

            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    DataCenterStorageListModel dcStorageModel = (DataCenterStorageListModel) model;
                    ArrayList<StorageDomain> list = (ArrayList<StorageDomain>) result;
                    ArrayList<EntityModel> models = new ArrayList<EntityModel>();
                    boolean addToList;
                    ArrayList<StorageDomain> items =
                            dcStorageModel.getItems() != null ? new ArrayList<StorageDomain>(Linq.<StorageDomain> Cast(dcStorageModel.getItems()))
                                    : new ArrayList<StorageDomain>();
                    for (StorageDomain a : list)
                    {
                        addToList = false;

                        if (!Linq.IsSDItemExistInList(items, a.getId())
                                && a.getStorageDomainType() == dcStorageModel.getStorageDomainType())
                        {
                            if (dcStorageModel.getStorageDomainType() == StorageDomainType.Data
                                    && a.getStorageType() == dcStorageModel.getEntity().getstorage_pool_type()
                                    && a.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
                            {
                                if (dcStorageModel.getEntity().getStoragePoolFormatType() == null)
                                {
                                    // skip V3 format for DC ver <= 3
                                    if (a.getStorageStaticData().getStorageFormat() == StorageFormatType.V3
                                            && dcStorageModel.getEntity()
                                                    .getcompatibility_version()
                                                    .compareTo(Version.v3_0) <= 0) {
                                        continue;
                                    }
                                    // skip V2 format for DC <= 2.2
                                    else if (a.getStorageStaticData().getStorageFormat() == StorageFormatType.V2
                                            && dcStorageModel.getEntity()
                                                    .getcompatibility_version()
                                                    .compareTo(Version.v2_2) <= 0) {
                                        continue;
                                    }

                                    addToList = true;
                                }
                                else if (dcStorageModel.getEntity().getStoragePoolFormatType() == a.getStorageStaticData()
                                        .getStorageFormat())
                                {
                                    addToList = true;
                                }
                                else if (dcStorageModel.getEntity().getcompatibility_version().compareTo(Version.v3_1) >= 0)
                                {
                                    // if DC is >= 3.1 we support upgrading
                                    if (a.getStorageStaticData().getStorageFormat() == StorageFormatType.V1
                                            || a.getStorageStaticData().getStorageFormat() == StorageFormatType.V2)
                                    {
                                        addToList = true;
                                    }
                                }
                            }
                            else if (dcStorageModel.getStorageDomainType() == StorageDomainType.ImportExport
                                    && a.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
                            {
                                addToList = true;
                            }

                            if (addToList)
                            {
                                EntityModel tempVar2 = new EntityModel();
                                tempVar2.setEntity(a);
                                models.add(tempVar2);
                            }
                        }
                    }
                    dcStorageModel.PostAttachInternal(models);
                }
            };
            AsyncDataProvider.GetStorageDomainList(_asyncQuery);
        }

    }

    private void PostAttachInternal(ArrayList<EntityModel> models)
    {
        ListModel listModel = (ListModel) getWindow();
        listModel.setItems(models);

        if (models.isEmpty())
        {
            listModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .thereAreNoCompatibleStorageDomainsAttachThisDcMsg());

            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            listModel.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnAttach", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            listModel.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar3.setIsCancel(true);
            listModel.getCommands().add(tempVar3);
        }
    }

    public void OnAttach()
    {
        ListModel model = (ListModel) getWindow();

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        // var items = model.Items
        // .Cast<EntityModel>()
        // .Where(Selector.GetIsSelected)
        // .Select(a => (storage_domains)a.Entity)
        // .ToList();
        ArrayList<StorageDomain> items = new ArrayList<StorageDomain>();
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((StorageDomain) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            // Frontend.RunMultipleActions(VdcActionType.AttachStorageDomainToPool,
            // items
            // .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
            // .ToList()
            // );
            ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
            for (StorageDomain a : items)
            {
                pb.add(new StorageDomainPoolParametersBase(a.getId(), getEntity().getId()));
            }

            Frontend.RunMultipleAction(VdcActionType.AttachStorageDomainToPool, pb);
        }

        Cancel();
    }

    public void Detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().detachStorageTitle());
        model.setHashName("detach_storage"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDetachFollowingStoragesMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (StorageDomain item : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            list.add(item.getStorageName());
        }
        model.setItems(list);

        if (ContainsLocalStorage(model))
        {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);

            model.setNote(ConstantsManager.getInstance().getMessages().detachNote(GetLocalStoragesFormattedString()));
        }

        UICommand tempVar = new UICommand("OnDetach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private String GetLocalStoragesFormattedString()
    {
        String localStorages = ""; //$NON-NLS-1$
        for (StorageDomain a : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            if (a.getStorageType() == StorageType.LOCALFS)
            {
                localStorages += a.getStorageName() + ", "; //$NON-NLS-1$
            }
        }
        return localStorages.substring(0, localStorages.length() - 2);
    }

    private boolean ContainsLocalStorage(ConfirmationModel model)
    {
        for (StorageDomain a : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            if (a.getStorageType() == StorageType.LOCALFS)
            {
                return true;
            }
        }
        return false;
    }

    public void OnDetach()
    {
        ConfirmationModel confirmModel = (ConfirmationModel) getWindow();

        if (confirmModel.getProgress() != null)
        {
            return;
        }

        if (!confirmModel.Validate())
        {
            return;
        }

        // A list of 'detach' action parameters
        setpb_detach(new ArrayList<VdcActionParametersBase>());
        // A list of 'remove' action parameters
        setpb_remove(new ArrayList<VdcActionParametersBase>());
        String localStorgaeDC = null;
        for (StorageDomain a : Linq.<StorageDomain> Cast(getSelectedItems()))
        {
            // For local storage - remove; otherwise - detach
            if (a.getStorageType() == StorageType.LOCALFS && a.getStorageDomainType() != StorageDomainType.ISO)
            {
                getpb_remove().add(new RemoveStorageDomainParameters(a.getId()));
                localStorgaeDC = a.getStoragePoolName();
            }
            else
            {
                getpb_detach().add(new DetachStorageDomainFromPoolParameters(a.getId(), getEntity().getId()));
            }
        }

        confirmModel.StartProgress(null);

        if (getpb_remove().size() > 0)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    DataCenterStorageListModel dataCenterStorageListModel = (DataCenterStorageListModel) model;
                    VDS locaVds = (VDS) result;
                    for (VdcActionParametersBase item : dataCenterStorageListModel.getpb_remove())
                    {
                        ((RemoveStorageDomainParameters) item).setVdsId((locaVds != null ? locaVds.getId() : null));
                        ((RemoveStorageDomainParameters) item).setDoFormat(true);
                    }

                    dataCenterStorageListModel.PostDetach(dataCenterStorageListModel.getWindow());
                }
            };
            AsyncDataProvider.GetLocalStorageHost(_asyncQuery, localStorgaeDC);
        }
        else
        {
            PostDetach(confirmModel);
        }
    }

    public void PostDetach(Model model)
    {
        Frontend.RunMultipleAction(VdcActionType.RemoveStorageDomain, getpb_remove(),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result1) {

                        Object[] array = (Object[]) result1.getState();
                        ConfirmationModel localModel1 = (ConfirmationModel) array[0];
                        ArrayList<VdcActionParametersBase> parameters =
                                (ArrayList<VdcActionParametersBase>) array[1];
                        Frontend.RunMultipleAction(VdcActionType.DetachStorageDomainFromPool, parameters,
                                new IFrontendMultipleActionAsyncCallback() {
                                    @Override
                                    public void Executed(FrontendMultipleActionAsyncResult result2) {

                                        ConfirmationModel localModel2 = (ConfirmationModel) result2.getState();
                                        localModel2.StopProgress();
                                        Cancel();

                                    }
                                }, localModel1);

                    }
                }, new Object[] { model, getpb_detach() });
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.ItemsCollectionChanged(sender, e);
        UpdateActionAvailability();
    }

    @Override
    protected void ItemsChanged()
    {
        super.ItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.selectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    @Override
    protected void itemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.itemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    @Override
    protected boolean getNotifyPropertyChangeForAnyItem()
    {
        return true;
    }

    private void UpdateActionAvailability()
    {
        ArrayList<StorageDomain> items =
                getItems() != null ? Linq.<StorageDomain> Cast(getItems())
                        : new ArrayList<StorageDomain>();
        ArrayList<StorageDomain> selectedItems =
                getSelectedItems() != null ? Linq.<StorageDomain> Cast(getSelectedItems())
                        : new ArrayList<StorageDomain>();

        if (getEntity() != null)
        {
            getAttachStorageCommand().setIsExecutionAllowed(getEntity().getstorage_pool_type() != StorageType.LOCALFS);
        }

        boolean isMasterPresents = false;
        for (StorageDomain a : items)
        {
            if (a.getStorageDomainType() == StorageDomainType.Master && a.getStatus() != null
                    && a.getStatus() == StorageDomainStatus.Active)
            {
                isMasterPresents = true;
                break;
            }
        }

        boolean isISOPresents = false;
        for (StorageDomain a : items)
        {
            if (a.getStorageDomainType() == StorageDomainType.ISO)
            {
                isISOPresents = true;
                break;
            }
        }
        getAttachISOCommand().setIsExecutionAllowed(false);
        getAttachISOCommand().setIsExecutionAllowed(items.size() > 0 && isMasterPresents && !isISOPresents);

        boolean isBackupPresents = false;
        for (StorageDomain a : items)
        {
            if (a.getStorageDomainType() == StorageDomainType.ImportExport)
            {
                isBackupPresents = true;
                break;
            }
        }
        getAttachBackupCommand().setIsExecutionAllowed(items.size() > 0 && isMasterPresents && !isBackupPresents);

        getDetachCommand().setIsExecutionAllowed(selectedItems.size() > 0
                && VdcActionUtils.CanExecute(selectedItems,
                        StorageDomain.class,
                        VdcActionType.DetachStorageDomainFromPool));

        getActivateCommand().setIsExecutionAllowed(selectedItems.size() == 1
                && VdcActionUtils.CanExecute(selectedItems, StorageDomain.class, VdcActionType.ActivateStorageDomain));

        getMaintenanceCommand().setIsExecutionAllowed(selectedItems.size() == 1
                && VdcActionUtils.CanExecute(selectedItems,
                        StorageDomain.class,
                        VdcActionType.DeactivateStorageDomain));
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAttachStorageCommand())
        {
            AttachStorage();
        }
        else if (command == getAttachISOCommand())
        {
            AttachISO();
        }
        else if (command == getAttachBackupCommand())
        {
            AttachBackup();
        }
        else if (command == getDetachCommand())
        {
            Detach();
        }
        else if (command == getActivateCommand())
        {
            Activate();
        }
        else if (command == getMaintenanceCommand())
        {
            Maintenance();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAttach")) //$NON-NLS-1$
        {
            OnAttach();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDetach")) //$NON-NLS-1$
        {
            OnDetach();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterStorageListModel"; //$NON-NLS-1$
    }
}
