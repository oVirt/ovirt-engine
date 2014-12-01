package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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
    public StorageDomain getEntity()
    {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
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

    private ArrayList<StoragePool> privateavailableDatacenters;

    public ArrayList<StoragePool> getavailableDatacenters()
    {
        return privateavailableDatacenters;
    }

    public void setavailableDatacenters(ArrayList<StoragePool> value)
    {
        privateavailableDatacenters = value;
    }

    public StorageDataCenterListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().dataCenterTitle());
        setHelpTag(HelpTag.data_center);
        setHashName("data_center"); //$NON-NLS-1$

        setAttachCommand(new UICommand("Attach", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                ArrayList<StorageDomain> domains =
                        (ArrayList<StorageDomain>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                for (StorageDomain domain : domains) {
                    domain.setId(domain.getStoragePoolId());
                }
                Collections.sort(domains, new Linq.StorageDomainByPoolNameComparator());
                searchableListModel.setItems(domains);
                setIsEmpty(((List) searchableListModel.getItems()).size() == 0);
            }
        };

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetStorageDomainListById, tempVar, _asyncQuery);
    }

    private void attach()
    {
        if (getWindow() != null)
        {
            return;
        }

        setattachCandidateDatacenters(new ArrayList<EntityModel>());
        setAttachMultiple(getEntity().getStorageDomainType() == StorageDomainType.ISO);

        AsyncDataProvider.getDataCenterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        StorageDataCenterListModel listModel = (StorageDataCenterListModel) target;
                        listModel.setavailableDatacenters((ArrayList<StoragePool>) returnValue);
                        for (StoragePool dataCenter : listModel.getavailableDatacenters())
                        {
                            switch (getEntity().getStorageDomainType())
                            {
                            case Master:
                            case Data:
                                boolean addDatacenter =
                                        (dataCenter.getStatus() == StoragePoolStatus.Uninitialized || dataCenter.getStatus() == StoragePoolStatus.Up)
                                                && (dataCenter.getStoragePoolFormatType() == null || dataCenter.getStoragePoolFormatType() == getEntity().getStorageStaticData()
                                                        .getStorageFormat() && dataCenter.isLocal() == (getEntity().getStorageType() == StorageType.LOCALFS));
                                addToAttachCandidateDatacenters(dataCenter, addDatacenter);
                                break;
                            case ISO:
                                AsyncDataProvider.getIsoDomainByDataCenterId(new AsyncQuery(new Object[] { listModel,
                                        dataCenter },
                                        new INewAsyncCallback() {
                                            @Override
                                            public void onSuccess(Object target1, Object returnValue1) {

                                                Object[] array1 = (Object[]) target1;
                                                StorageDataCenterListModel listModel1 =
                                                        (StorageDataCenterListModel) array1[0];
                                                StoragePool dataCenter1 = (StoragePool) array1[1];
                                                boolean addDatacenter1 =
                                                        dataCenter1.getStatus() == StoragePoolStatus.Up
                                                                && returnValue1 == null;
                                                listModel1.addToAttachCandidateDatacenters(dataCenter1, addDatacenter1);

                                            }
                                        }),
                                        dataCenter.getId());
                                break;
                            case ImportExport:
                                AsyncDataProvider.getExportDomainByDataCenterId(new AsyncQuery(new Object[] {
                                        listModel, dataCenter },
                                        new INewAsyncCallback() {
                                            @Override
                                            public void onSuccess(Object target2, Object returnValue2) {

                                                Object[] array2 = (Object[]) target2;
                                                StorageDataCenterListModel listModel2 =
                                                        (StorageDataCenterListModel) array2[0];
                                                StoragePool dataCenter2 = (StoragePool) array2[1];
                                                boolean addDatacenter2 =
                                                        dataCenter2.getStatus() == StoragePoolStatus.Up
                                                                && returnValue2 == null;
                                                listModel2.addToAttachCandidateDatacenters(dataCenter2, addDatacenter2);

                                            }
                                        }),
                                        dataCenter.getId());
                                break;
                            }
                        }

                    }
                }));
    }

    public void addToAttachCandidateDatacenters(StoragePool dataCenter, boolean addDatacenter)
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

        postAttachInit(datacenters);
    }

    public void postAttachInit(ArrayList<EntityModel> datacenters)
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

    private void onAttach()
    {
        ListModel model = (ListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getEntity() == null)
        {
            cancel();
            return;
        }

        ArrayList<StoragePool> items = new ArrayList<StoragePool>();
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((StoragePool) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            model.startProgress(null);

            ArrayList<VdcActionParametersBase> parameters =
                    new ArrayList<VdcActionParametersBase>();
            for (StoragePool dataCenter : items)
            {
                parameters.add(new AttachStorageDomainToPoolParameters(getEntity().getId(), dataCenter.getId()));
            }

            Frontend.getInstance().runMultipleAction(VdcActionType.AttachStorageDomainToPool, parameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            ListModel localModel = (ListModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }
        else
        {
            cancel();
        }
    }

    private void detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().detachStorageTitle());
        model.setHelpTag(HelpTag.detach_storage);
        model.setHashName("detach_storage"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDetachStorageFromDcsMsg());

        ArrayList<String> items = new ArrayList<String>();
        boolean shouldAddressWarnning = false;
        for (Object item : getSelectedItems())
        {
            StorageDomain a = (StorageDomain) item;
            items.add(a.getStoragePoolName());
            if (a.getStorageDomainType().isDataDomain()) {
                shouldAddressWarnning = true;
                break;
            }
        }
        model.setItems(items);

        if (containsLocalStorage(model))
        {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);
            shouldAddressWarnning = false;
            model.setNote(ConstantsManager.getInstance().getMessages().detachNote(getLocalStoragesFormattedString()));
        }
        if (shouldAddressWarnning) {
            model.setNote(ConstantsManager.getInstance().getConstants().detachWarnningNote());
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

    private String getLocalStoragesFormattedString()
    {
        StringBuilder localStorages = new StringBuilder();
        for (StorageDomain a : Linq.<StorageDomain> cast(getSelectedItems()))
        {
            if (a.getStorageType() == StorageType.LOCALFS)
            {
                localStorages.append(a.getStorageName()).append(", "); //$NON-NLS-1$
            }
        }
        return localStorages.substring(0, localStorages.length() - 2);
    }

    private boolean containsLocalStorage(ConfirmationModel model)
    {
        for (StorageDomain a : Linq.<StorageDomain> cast(getSelectedItems()))
        {
            if (a.getStorageType() == StorageType.LOCALFS)
            {
                return true;
            }
        }
        return false;
    }

    private void onDetach()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        setdetachPrms(new ArrayList<VdcActionParametersBase>());
        setremovePrms(new ArrayList<VdcActionParametersBase>());

        for (Object item : getSelectedItems())
        {
            StorageDomain storageDomain = (StorageDomain) item;
            if (storageDomain.getStorageType() != StorageType.LOCALFS)
            {
                DetachStorageDomainFromPoolParameters param = new DetachStorageDomainFromPoolParameters();
                param.setStorageDomainId(getEntity().getId());
                if (storageDomain.getStoragePoolId() != null)
                {
                    param.setStoragePoolId(storageDomain.getStoragePoolId());
                }

                getdetachPrms().add(param);
            }
            else
            {
                AsyncDataProvider.getLocalStorageHost(new AsyncQuery(new Object[] { this, getEntity() },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                StorageDataCenterListModel listModel = (StorageDataCenterListModel) array[0];
                                StorageDomain storage = (StorageDomain) array[1];
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
                                    Frontend.getInstance().runMultipleAction(VdcActionType.RemoveStorageDomain,
                                            listModel.getremovePrms());
                                }

                            }
                        }),
                        storageDomain.getStoragePoolName());
            }

            if (getdetachPrms().size() > 0)
            {
                Frontend.getInstance().runMultipleAction(VdcActionType.DetachStorageDomainFromPool, getdetachPrms());
            }
        }

        cancel();
    }

    private void maintenance()
    {
        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().maintenanceStorageDomainsTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToPlaceFollowingStorageDomainsIntoMaintenanceModeMsg());
        model.setHashName("maintenance_storage_domain"); //$NON-NLS-1$
        setWindow(model);

        ArrayList<String> items = new ArrayList<String>();
        for (Object selected : getSelectedItems()) {
            items.add(((StorageDomain) selected).getName());
        }
        model.setItems(items);

        UICommand maintenance = new UICommand("OnMaintenance", this); //$NON-NLS-1$
        maintenance.setTitle(ConstantsManager.getInstance().getConstants().ok());
        maintenance.setIsDefault(true);
        model.getCommands().add(maintenance);

        UICommand cancel = new UICommand("Cancel", this); //$NON-NLS-1$
        cancel.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancel.setIsCancel(true);
        model.getCommands().add(cancel);
    }

    private void onMaintenance()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            StorageDomain a = (StorageDomain) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getId());
            if (a.getStoragePoolId() != null)
            {
                parameters.setStoragePoolId(a.getStoragePoolId());
            }

            list.add(parameters);
        }

        final ConfirmationModel confirmationModel = (ConfirmationModel) getWindow();
        confirmationModel.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.DeactivateStorageDomain, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        confirmationModel.stopProgress();
                        setWindow(null);
                    }
                }, null);
    }

    private void activate()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            StorageDomain a = (StorageDomain) item;

            StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
            parameters.setStorageDomainId(getEntity().getId());
            if (a.getStoragePoolId() != null)
            {
                parameters.setStoragePoolId(a.getStoragePoolId());
            }

            list.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.ActivateStorageDomain, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    private void updateActionAvailability()
    {
        ArrayList<StorageDomain> items =
                getSelectedItems() != null ? Linq.<StorageDomain> cast(getSelectedItems())
                        : new ArrayList<StorageDomain>();

        getActivateCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.canExecute(items, StorageDomain.class, VdcActionType.ActivateStorageDomain));

        getMaintenanceCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.canExecute(items, StorageDomain.class, VdcActionType.DeactivateStorageDomain));

        getAttachCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached || getEntity().getStorageDomainType() == StorageDomainType.ISO));

        getDetachCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, StorageDomain.class, VdcActionType.DetachStorageDomainFromPool));
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getAttachCommand())
        {
            attach();
        }
        else if (command == getDetachCommand())
        {
            detach();
        }
        else if (command == getActivateCommand())
        {
            activate();
        }
        else if (command == getMaintenanceCommand())
        {
            maintenance();
        }
        else if ("OnAttach".equals(command.getName())) //$NON-NLS-1$
        {
            onAttach();
        }
        else if ("OnDetach".equals(command.getName())) //$NON-NLS-1$
        {
            onDetach();
        }
        else if ("OnMaintenance".equals(command.getName())) //$NON-NLS-1$
        {
            onMaintenance();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageDataCenterListModel"; //$NON-NLS-1$
    }
}
