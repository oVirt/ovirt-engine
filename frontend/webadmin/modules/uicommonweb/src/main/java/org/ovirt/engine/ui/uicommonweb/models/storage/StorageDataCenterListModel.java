package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class StorageDataCenterListModel extends SearchableListModel
{

    private UICommand privateAttachCommand;

    public UICommand getAttachCommand()
    {
        return privateAttachCommand;
    }

    private void setAttachCommand(UICommand value)
    {
        privateAttachCommand = value;
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
    public storage_domains getEntity()
    {
        return (storage_domains) super.getEntity();
    }

    public void setEntity(storage_domains value)
    {
        super.setEntity(value);
    }

    /**
     * Gets the value indicating whether multiple data centers can be selected to attach storage to.
     */
    private boolean privateAttachMultiple;

    public boolean getAttachMultiple()
    {
        return privateAttachMultiple;
    }

    private void setAttachMultiple(boolean value)
    {
        privateAttachMultiple = value;
    }

    private java.util.ArrayList<VdcActionParametersBase> privatedetachPrms;

    public java.util.ArrayList<VdcActionParametersBase> getdetachPrms()
    {
        return privatedetachPrms;
    }

    public void setdetachPrms(java.util.ArrayList<VdcActionParametersBase> value)
    {
        privatedetachPrms = value;
    }

    private java.util.ArrayList<VdcActionParametersBase> privateremovePrms;

    public java.util.ArrayList<VdcActionParametersBase> getremovePrms()
    {
        return privateremovePrms;
    }

    public void setremovePrms(java.util.ArrayList<VdcActionParametersBase> value)
    {
        privateremovePrms = value;
    }

    private java.util.ArrayList<EntityModel> privateattachCandidateDatacenters;

    public java.util.ArrayList<EntityModel> getattachCandidateDatacenters()
    {
        return privateattachCandidateDatacenters;
    }

    public void setattachCandidateDatacenters(java.util.ArrayList<EntityModel> value)
    {
        privateattachCandidateDatacenters = value;
    }

    private java.util.ArrayList<storage_pool> privateavailableDatacenters;

    public java.util.ArrayList<storage_pool> getavailableDatacenters()
    {
        return privateavailableDatacenters;
    }

    public void setavailableDatacenters(java.util.ArrayList<storage_pool> value)
    {
        privateavailableDatacenters = value;
    }

    public StorageDataCenterListModel()
    {
        setTitle("Data Center");

        setAttachCommand(new UICommand("Attach", this));
        setDetachCommand(new UICommand("Detach", this));
        setActivateCommand(new UICommand("Activate", this));
        setMaintenanceCommand(new UICommand("Maintenance", this));

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        getSearchCommand().Execute();
        UpdateActionAvailability();
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
                searchableListModel.setItems((java.util.ArrayList<storage_domains>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                setIsEmpty(((java.util.List) searchableListModel.getItems()).size() == 0);
            }
        };

        StorageDomainQueryParametersBase tempVar = new StorageDomainQueryParametersBase(getEntity().getid());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetStorageDomainListById, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainListById,
                new StorageDomainQueryParametersBase(getEntity().getid())));
        setItems(getAsyncResult().getData());
    }

    private void Attach()
    {
        if (getWindow() != null)
        {
            return;
        }

        setattachCandidateDatacenters(new java.util.ArrayList<EntityModel>());
        setAttachMultiple(getEntity().getstorage_domain_type() == StorageDomainType.ISO);

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        StorageDataCenterListModel listModel = (StorageDataCenterListModel) target;
                        listModel.setavailableDatacenters((java.util.ArrayList<storage_pool>) returnValue);
                        for (storage_pool dataCenter : listModel.getavailableDatacenters())
                        {
                            switch (getEntity().getstorage_domain_type())
                            {
                            case Master:
                            case Data:
                                boolean addDatacenter =
                                        (dataCenter.getstatus() == StoragePoolStatus.Uninitialized || dataCenter.getstatus() == StoragePoolStatus.Up)
                                                && (dataCenter.getStoragePoolFormatType() == null || dataCenter.getStoragePoolFormatType() == getEntity().getStorageStaticData()
                                                        .getStorageFormat())
                                                && dataCenter.getstorage_pool_type() == getEntity().getstorage_type();
                                AddToAttachCandidateDatacenters(dataCenter, addDatacenter);
                                break;
                            case ISO:
                                AsyncDataProvider.GetIsoDomainByDataCenterId(new AsyncQuery(new Object[] { listModel,
                                        dataCenter },
                                        new INewAsyncCallback() {
                                            @Override
                                            public void OnSuccess(Object target1, Object returnValue1) {

                                                Object[] array1 = (Object[]) target1;
                                                StorageDataCenterListModel listModel1 =
                                                        (StorageDataCenterListModel) array1[0];
                                                storage_pool dataCenter1 = (storage_pool) array1[1];
                                                boolean addDatacenter1 =
                                                        dataCenter1.getstatus() == StoragePoolStatus.Up
                                                                && returnValue1 == null;
                                                listModel1.AddToAttachCandidateDatacenters(dataCenter1, addDatacenter1);

                                            }
                                        }),
                                        dataCenter.getId());
                                break;
                            case ImportExport:
                                AsyncDataProvider.GetExportDomainByDataCenterId(new AsyncQuery(new Object[] {
                                        listModel, dataCenter },
                                        new INewAsyncCallback() {
                                            @Override
                                            public void OnSuccess(Object target2, Object returnValue2) {

                                                Object[] array2 = (Object[]) target2;
                                                StorageDataCenterListModel listModel2 =
                                                        (StorageDataCenterListModel) array2[0];
                                                storage_pool dataCenter2 = (storage_pool) array2[1];
                                                boolean addDatacenter2 =
                                                        dataCenter2.getstatus() == StoragePoolStatus.Up
                                                                && returnValue2 == null;
                                                listModel2.AddToAttachCandidateDatacenters(dataCenter2, addDatacenter2);

                                            }
                                        }),
                                        dataCenter.getId());
                                break;
                            }
                        }

                    }
                }));
    }

    public void AddToAttachCandidateDatacenters(storage_pool dataCenter, boolean addDatacenter)
    {
        // Add a new datacenter EntityModel
        EntityModel dcEntityModel = new EntityModel();
        if (addDatacenter)
        {
            dcEntityModel.setEntity(dataCenter);
        }
        getattachCandidateDatacenters().add(dcEntityModel);

        // If not finished going through the datacenters list - return
        if (getattachCandidateDatacenters().size() != getavailableDatacenters().size())
        {
            return;
        }

        // Filter datacenters list
        java.util.ArrayList<EntityModel> datacenters = new java.util.ArrayList<EntityModel>();
        for (EntityModel datacenter : getattachCandidateDatacenters())
        {
            if (datacenter.getEntity() != null)
            {
                datacenters.add(datacenter);
            }
        }

        PostAttachInit(datacenters);
    }

    public void PostAttachInit(java.util.ArrayList<EntityModel> datacenters)
    {
        ListModel model = new ListModel();
        setWindow(model);
        model.setTitle("Attach to Data Center");
        model.setItems(datacenters);

        if (datacenters.isEmpty())
        {
            model.setMessage("There are No Data Centers to which the Storage Domain can be attached");

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnAttach", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    private void OnAttach()
    {
        ListModel model = (ListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        java.util.ArrayList<storage_pool> items = new java.util.ArrayList<storage_pool>();
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((storage_pool) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            model.StartProgress(null);

            java.util.ArrayList<VdcActionParametersBase> parameters =
                    new java.util.ArrayList<VdcActionParametersBase>();
            for (storage_pool dataCenter : items)
            {
                parameters.add(new StorageDomainPoolParametersBase(getEntity().getid(), dataCenter.getId()));
            }

            Frontend.RunMultipleAction(VdcActionType.AttachStorageDomainToPool, parameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            ListModel localModel = (ListModel) result.getState();
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

    private void Detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Detach Storage");
        model.setHashName("detach_storage");
        model.setMessage("Are you sure you want to Detach storage from the following Data Center(s)?");

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;
            items.add(a.getstorage_pool_name());
        }
        model.setItems(items);

        if (ContainsLocalStorage(model))
        {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);

            model.setNote("Note: " + GetLocalStoragesFormattedString() + " will be removed!");
        }

        UICommand tempVar = new UICommand("OnDetach", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private String GetLocalStoragesFormattedString()
    {
        String localStorages = "";
        for (storage_domains a : Linq.<storage_domains> Cast(getSelectedItems()))
        {
            if (a.getstorage_type() == StorageType.LOCALFS)
            {
                localStorages += a.getstorage_name() + ", ";
            }
        }
        return localStorages.substring(0, localStorages.length() - 2);
    }

    private boolean ContainsLocalStorage(ConfirmationModel model)
    {
        for (storage_domains a : Linq.<storage_domains> Cast(getSelectedItems()))
        {
            if (a.getstorage_type() == StorageType.LOCALFS)
            {
                return true;
            }
        }
        return false;
    }

    private void OnDetach()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        setdetachPrms(new java.util.ArrayList<VdcActionParametersBase>());
        setremovePrms(new java.util.ArrayList<VdcActionParametersBase>());

        for (Object item : getSelectedItems())
        {
            storage_domains storageDomain = (storage_domains) item;
            if (storageDomain.getstorage_type() != StorageType.LOCALFS)
            {
                DetachStorageDomainFromPoolParameters param = new DetachStorageDomainFromPoolParameters();
                param.setStorageDomainId(getEntity().getid());
                if (storageDomain.getstorage_pool_id() != null)
                {
                    param.setStoragePoolId(storageDomain.getstorage_pool_id().getValue());
                }

                getdetachPrms().add(param);
            }
            else
            {
                AsyncDataProvider.GetLocalStorageHost(new AsyncQuery(new Object[] { this, storageDomain },
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                StorageDataCenterListModel listModel = (StorageDataCenterListModel) array[0];
                                storage_domains storage = (storage_domains) array[1];
                                VDS locaVds = (VDS) returnValue;
                                RemoveStorageDomainParameters tempVar =
                                        new RemoveStorageDomainParameters(storage.getid());
                                tempVar.setVdsId((locaVds != null ? locaVds.getvds_id() : null));
                                tempVar.setDoFormat(true);
                                RemoveStorageDomainParameters removeStorageDomainParameters = tempVar;
                                listModel.getremovePrms().add(removeStorageDomainParameters);
                                if (listModel.getremovePrms().size() + listModel.getdetachPrms().size() == listModel.getSelectedItems()
                                        .size())
                                {
                                    Frontend.RunMultipleAction(VdcActionType.RemoveStorageDomain,
                                            listModel.getremovePrms());
                                }

                            }
                        }),
                        storageDomain.getstorage_pool_name());
            }

            if (getdetachPrms().size() > 0)
            {
                Frontend.RunMultipleAction(VdcActionType.DetachStorageDomainFromPool, getdetachPrms());
            }
        }

        Cancel();
    }

    private void Maintenance()
    {
        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getid());
            if (a.getstorage_pool_id() != null)
            {
                parameters.setStoragePoolId(a.getstorage_pool_id().getValue());
            }

            list.add(parameters);
        }

        Frontend.RunMultipleAction(VdcActionType.DeactivateStorageDomain, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void Activate()
    {
        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getid());
            if (a.getstorage_pool_id() != null)
            {
                parameters.setStoragePoolId(a.getstorage_pool_id().getValue());
            }

            list.add(parameters);
        }

        Frontend.RunMultipleAction(VdcActionType.ActivateStorageDomain, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void Cancel()
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
        java.util.ArrayList<storage_domains> items =
                getSelectedItems() != null ? Linq.<storage_domains> Cast(getSelectedItems())
                        : new java.util.ArrayList<storage_domains>();

        getActivateCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.ActivateStorageDomain));

        getMaintenanceCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.DeactivateStorageDomain));

        getAttachCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached || getEntity().getstorage_domain_type() == StorageDomainType.ISO));

        getDetachCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.DetachStorageDomainFromPool));
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAttachCommand())
        {
            Attach();
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
        else if (StringHelper.stringsEqual(command.getName(), "OnAttach"))
        {
            OnAttach();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDetach"))
        {
            OnDetach();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageDataCenterListModel";
    }
}
