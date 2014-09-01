package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;
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
    public StoragePool getEntity()
    {
        return (StoragePool) super.getEntity();
    }

    public void setEntity(StoragePool value)
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
        setHelpTag(HelpTag.storage);
        setHashName("storage"); //$NON-NLS-1$

        setAttachStorageCommand(new UICommand("AttachStorage", this)); //$NON-NLS-1$
        setAttachISOCommand(new UICommand("AttachISO", this)); //$NON-NLS-1$
        setAttachBackupCommand(new UICommand("AttachBackup", this)); //$NON-NLS-1$
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
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            // omer - overriding AsyncSearch - using query instead of search
            // SearchString = StringFormat.format("storage: datacenter={0}", Entity.name);
            super.search();
        }
    }

    @Override
    protected void syncSearch()
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

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, tempVar, _asyncQuery);
    }

    public void onMaintenance()
    {
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
        for (StorageDomain a : Linq.<StorageDomain> cast(getSelectedItems()))
        {
            pb.add(new StorageDomainPoolParametersBase(a.getId(), getEntity().getId()));
        }

        final ConfirmationModel confirmationModel = (ConfirmationModel) getWindow();
        confirmationModel.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.DeactivateStorageDomain, pb, new IFrontendMultipleActionAsyncCallback() {
            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                confirmationModel.stopProgress();
                setWindow(null);
            }
        });
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

    public void activate()
    {
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
        for (StorageDomain a : Linq.<StorageDomain> cast(getSelectedItems()))
        {
            pb.add(new StorageDomainPoolParametersBase(a.getId(), getEntity().getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.ActivateStorageDomain, pb);
    }

    public void attachBackup()
    {
        ListModel listModel = new ListModel();
        listModel.setTitle(ConstantsManager.getInstance().getConstants().attachExportDomainTitle());
        listModel.setHelpTag(HelpTag.attach_export_domain);
        listModel.setHashName("attach_export_domain"); //$NON-NLS-1$
        attachInternal(listModel, StorageDomainType.ImportExport);
    }

    public void attachISO()
    {
        ListModel listModel = new ListModel();
        listModel.setTitle(ConstantsManager.getInstance().getConstants().attachISOLibraryTitle());
        listModel.setHelpTag(HelpTag.attach_iso_library);
        listModel.setHashName("attach_iso_library"); //$NON-NLS-1$
        attachInternal(listModel, StorageDomainType.ISO);
    }

    public void attachStorage()
    {
        ListModel listModel = new ListModel();
        listModel.setTitle(ConstantsManager.getInstance().getConstants().attachStorageTitle());
        listModel.setHelpTag(HelpTag.attach_storage);
        listModel.setHashName("attach_storage"); //$NON-NLS-1$
        attachInternal(listModel, StorageDomainType.Data);
    }

    private void attachInternal(ListModel listModel, StorageDomainType storageType)
    {
        if (getWindow() != null)
        {
            return;
        }

        this.setStorageDomainType(storageType);

        setWindow(listModel);

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
                            dcStorageModel.getItems() != null ? new ArrayList<StorageDomain>(Linq.<StorageDomain> cast(dcStorageModel.getItems()))
                                    : new ArrayList<StorageDomain>();
                    for (StorageDomain a : list)
                    {
                        if (!Linq.isSDItemExistInList(items, a.getId()))
                        {
                            EntityModel tempVar = new EntityModel();
                            tempVar.setEntity(a);
                            models.add(tempVar);
                        }
                    }
                    dcStorageModel.postAttachInternal(models);

                }
            };
            AsyncDataProvider.getInstance().getISOStorageDomainList(_asyncQuery);
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
                            dcStorageModel.getItems() != null ? new ArrayList<StorageDomain>(Linq.<StorageDomain> cast(dcStorageModel.getItems()))
                                    : new ArrayList<StorageDomain>();
                    for (StorageDomain a : list)
                    {
                        addToList = false;

                        if (!Linq.isSDItemExistInList(items, a.getId())
                                && a.getStorageDomainType() == dcStorageModel.getStorageDomainType())
                        {
                            if (dcStorageModel.getStorageDomainType() == StorageDomainType.Data
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
                    dcStorageModel.postAttachInternal(models);
                }
            };
            AsyncDataProvider.getInstance().getStorageDomainList(_asyncQuery);
        }

    }

    private void postAttachInternal(ArrayList<EntityModel> models)
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

    public void onAttach()
    {
        ListModel model = (ListModel) getWindow();

        if (getEntity() == null)
        {
            cancel();
            return;
        }

        ArrayList<StorageDomain> items = new ArrayList<StorageDomain>();
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((StorageDomain) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
            for (StorageDomain a : items)
            {
                pb.add(new AttachStorageDomainToPoolParameters(a.getId(), getEntity().getId()));
            }

            Frontend.getInstance().runMultipleAction(VdcActionType.AttachStorageDomainToPool, pb);
        }

        cancel();
    }

    public void detach()
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
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDetachFollowingStoragesMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (StorageDomain item : Linq.<StorageDomain> cast(getSelectedItems()))
        {
            list.add(item.getStorageName());
        }
        model.setItems(list);

        if (containsLocalStorage(model))
        {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);

            model.setNote(ConstantsManager.getInstance().getMessages().detachNote(getLocalStoragesFormattedString()));
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

    public void onDetach()
    {
        ConfirmationModel confirmModel = (ConfirmationModel) getWindow();

        if (confirmModel.getProgress() != null)
        {
            return;
        }

        if (!confirmModel.validate())
        {
            return;
        }

        // A list of 'detach' action parameters
        setpb_detach(new ArrayList<VdcActionParametersBase>());
        // A list of 'remove' action parameters
        setpb_remove(new ArrayList<VdcActionParametersBase>());
        String localStorgaeDC = null;
        for (StorageDomain a : Linq.<StorageDomain> cast(getSelectedItems()))
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

        confirmModel.startProgress(null);

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

                    dataCenterStorageListModel.postDetach(dataCenterStorageListModel.getWindow());
                }
            };
            AsyncDataProvider.getInstance().getLocalStorageHost(_asyncQuery, localStorgaeDC);
        }
        else
        {
            postDetach(confirmModel);
        }
    }

    public void postDetach(Model model)
    {
        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveStorageDomain, getpb_remove(),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result1) {

                        Object[] array = (Object[]) result1.getState();
                        ConfirmationModel localModel1 = (ConfirmationModel) array[0];
                        ArrayList<VdcActionParametersBase> parameters =
                                (ArrayList<VdcActionParametersBase>) array[1];
                        Frontend.getInstance().runMultipleAction(VdcActionType.DetachStorageDomainFromPool, parameters,
                                new IFrontendMultipleActionAsyncCallback() {
                                    @Override
                                    public void executed(FrontendMultipleActionAsyncResult result2) {

                                        ConfirmationModel localModel2 = (ConfirmationModel) result2.getState();
                                        localModel2.stopProgress();
                                        cancel();

                                    }
                                }, localModel1);

                    }
                }, new Object[] { model, getpb_detach() });
    }

    public void cancel()
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
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.itemsCollectionChanged(sender, e);
        updateActionAvailability();
    }

    @Override
    protected void itemsChanged()
    {
        super.itemsChanged();
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

    @Override
    protected void itemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.itemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    @Override
    protected boolean getNotifyPropertyChangeForAnyItem()
    {
        return true;
    }

    private void updateActionAvailability()
    {
        ArrayList<StorageDomain> items =
                getItems() != null ? Linq.<StorageDomain> cast(getItems())
                        : new ArrayList<StorageDomain>();
        ArrayList<StorageDomain> selectedItems =
                getSelectedItems() != null ? Linq.<StorageDomain> cast(getSelectedItems())
                        : new ArrayList<StorageDomain>();

        if (getEntity() != null)
        {
            getAttachStorageCommand().setIsExecutionAllowed(!getEntity().isLocal());
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
                && VdcActionUtils.canExecute(selectedItems,
                StorageDomain.class,
                VdcActionType.DetachStorageDomainFromPool));

        getActivateCommand().setIsExecutionAllowed(selectedItems.size() == 1
                && VdcActionUtils.canExecute(selectedItems, StorageDomain.class, VdcActionType.ActivateStorageDomain));

        getMaintenanceCommand().setIsExecutionAllowed(selectedItems.size() == 1
                && VdcActionUtils.canExecute(selectedItems,
                StorageDomain.class,
                VdcActionType.DeactivateStorageDomain));
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getAttachStorageCommand())
        {
            attachStorage();
        }
        else if (command == getAttachISOCommand())
        {
            attachISO();
        }
        else if (command == getAttachBackupCommand())
        {
            attachBackup();
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
        return "DataCenterStorageListModel"; //$NON-NLS-1$
    }
}
