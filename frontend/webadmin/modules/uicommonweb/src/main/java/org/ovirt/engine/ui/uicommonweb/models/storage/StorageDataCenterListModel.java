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
import org.ovirt.engine.core.compat.Guid;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.ArrayList;
import java.util.List;

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

    private ArrayList<VdcActionParametersBase> privatedetachPrms;

    public ArrayList<VdcActionParametersBase> getdetachPrms()
    {
        return privatedetachPrms;
    }

    public void setdetachPrms(ArrayList<VdcActionParametersBase> value)
    {
        privatedetachPrms = value;
    }

    private ArrayList<VdcActionParametersBase> privateremovePrms;

    public ArrayList<VdcActionParametersBase> getremovePrms()
    {
        return privateremovePrms;
    }

    public void setremovePrms(ArrayList<VdcActionParametersBase> value)
    {
        privateremovePrms = value;
    }

    private ArrayList<EntityModel> privateattachCandidateDatacenters;

    public ArrayList<EntityModel> getattachCandidateDatacenters()
    {
        return privateattachCandidateDatacenters;
    }

    public void setattachCandidateDatacenters(ArrayList<EntityModel> value)
    {
        privateattachCandidateDatacenters = value;
    }

    private ArrayList<storage_pool> privateavailableDatacenters;

    public ArrayList<storage_pool> getavailableDatacenters()
    {
        return privateavailableDatacenters;
    }

    public void setavailableDatacenters(ArrayList<storage_pool> value)
    {
        privateavailableDatacenters = value;
    }

    public StorageDataCenterListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().dataCenterTitle());
        setHashName("data_center"); //$NON-NLS-1$

        setAttachCommand(new UICommand("Attach", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this)); //$NON-NLS-1$

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
                ArrayList<storage_domains> domains =
                        (ArrayList<storage_domains>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                for (storage_domains domain : domains) {
                    String guid =
                            domain.getstorage_pool_id() != null ? domain.getstorage_pool_id().getValue().toString()
                                    : Guid.Empty.toString();
                    domain.setQueryableId(domain.getId() + "_" + guid); //$NON-NLS-1$
                }
                searchableListModel.setItems(domains);
                setIsEmpty(((List) searchableListModel.getItems()).size() == 0);
            }
        };

        StorageDomainQueryParametersBase tempVar = new StorageDomainQueryParametersBase(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetStorageDomainListById, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainListById,
                new StorageDomainQueryParametersBase(getEntity().getId())));
        setItems(getAsyncResult().getData());
    }

    private void Attach()
    {
        if (getWindow() != null)
        {
            return;
        }

        setattachCandidateDatacenters(new ArrayList<EntityModel>());
        setAttachMultiple(getEntity().getstorage_domain_type() == StorageDomainType.ISO);

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        StorageDataCenterListModel listModel = (StorageDataCenterListModel) target;
                        listModel.setavailableDatacenters((ArrayList<storage_pool>) returnValue);
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
        ArrayList<EntityModel> datacenters = new ArrayList<EntityModel>();
        for (EntityModel datacenter : getattachCandidateDatacenters())
        {
            if (datacenter.getEntity() != null)
            {
                datacenters.add(datacenter);
            }
        }

        PostAttachInit(datacenters);
    }

    public void PostAttachInit(ArrayList<EntityModel> datacenters)
    {
        ListModel model = new ListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().attachToDataCenterTitle());
        model.setItems(datacenters);

        if (datacenters.isEmpty())
        {
            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .thereAreNoDataCenterStorageDomainAttachedMsg());

            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnAttach", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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

        ArrayList<storage_pool> items = new ArrayList<storage_pool>();
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

            ArrayList<VdcActionParametersBase> parameters =
                    new ArrayList<VdcActionParametersBase>();
            for (storage_pool dataCenter : items)
            {
                parameters.add(new StorageDomainPoolParametersBase(getEntity().getId(), dataCenter.getId()));
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
        model.setTitle(ConstantsManager.getInstance().getConstants().detachStorageTitle());
        model.setHashName("detach_storage"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDetachStorageFromDcsMsg());

        ArrayList<String> items = new ArrayList<String>();
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
        for (storage_domains a : Linq.<storage_domains> Cast(getSelectedItems()))
        {
            if (a.getstorage_type() == StorageType.LOCALFS)
            {
                localStorages += a.getstorage_name() + ", "; //$NON-NLS-1$
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

        setdetachPrms(new ArrayList<VdcActionParametersBase>());
        setremovePrms(new ArrayList<VdcActionParametersBase>());

        for (Object item : getSelectedItems())
        {
            storage_domains storageDomain = (storage_domains) item;
            if (storageDomain.getstorage_type() != StorageType.LOCALFS)
            {
                DetachStorageDomainFromPoolParameters param = new DetachStorageDomainFromPoolParameters();
                param.setStorageDomainId(getEntity().getId());
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
                                        new RemoveStorageDomainParameters(storage.getId());
                                tempVar.setVdsId((locaVds != null ? locaVds.getId() : null));
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
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getId());
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
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getId());
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

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        ArrayList<storage_domains> items =
                getSelectedItems() != null ? Linq.<storage_domains> Cast(getSelectedItems())
                        : new ArrayList<storage_domains>();

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
        return "StorageDataCenterListModel"; //$NON-NLS-1$
    }
}
