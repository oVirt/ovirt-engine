package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

@SuppressWarnings("unused")
public class StorageListModel extends ListWithDetailsModel implements ITaskTarget, ISupportSystemTreeContext
{

    private UICommand privateNewDomainCommand;

    public UICommand getNewDomainCommand()
    {
        return privateNewDomainCommand;
    }

    private void setNewDomainCommand(UICommand value)
    {
        privateNewDomainCommand = value;
    }

    private UICommand privateImportDomainCommand;

    public UICommand getImportDomainCommand()
    {
        return privateImportDomainCommand;
    }

    private void setImportDomainCommand(UICommand value)
    {
        privateImportDomainCommand = value;
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

    private UICommand privateDestroyCommand;

    public UICommand getDestroyCommand()
    {
        return privateDestroyCommand;
    }

    private void setDestroyCommand(UICommand value)
    {
        privateDestroyCommand = value;
    }

    private ArrayList<String> usedLunsMessages;

    public ArrayList<String> getUsedLunsMessages() {
        return usedLunsMessages;
    }

    public void setUsedLunsMessages(ArrayList<String> usedLunsMessages) {
        this.usedLunsMessages = usedLunsMessages;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<storage_domains>().Select(a =>
    // a.id).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            ArrayList<Object> items = new ArrayList<Object>();
            for (Object item : getSelectedItems())
            {
                storage_domains i = (storage_domains) item;
                items.add(i.getId());
            }
            return items.toArray(new Object[] {});
        }
    }

    public StorageListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());

        setDefaultSearchString("Storage:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewDomainCommand(new UICommand("NewDomain", this)); //$NON-NLS-1$
        setImportDomainCommand(new UICommand("ImportDomain", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setDestroyCommand(new UICommand("Destroy", this)); //$NON-NLS-1$

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private EntityModel vmBackupModel;
    private EntityModel templateBackupModel;
    private ListModel vmListModel;
    private ListModel templateListModel;
    private ListModel isoListModel;

    public StorageDomainStatic storageDomain;
    public TaskContext context;
    public IStorageModel storageModel;
    public NGuid storageId;
    public storage_server_connections nfsConnection;
    public storage_server_connections connection;
    public Guid hostId = new Guid();
    public String path;
    public StorageDomainType domainType = StorageDomainType.values()[0];
    public boolean removeConnection;

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        vmBackupModel = new VmBackupModel();
        vmBackupModel.setIsAvailable(false);

        templateBackupModel = new TemplateBackupModel();
        templateBackupModel.setIsAvailable(false);

        vmListModel = new StorageVmListModel();
        vmListModel.setIsAvailable(false);

        templateListModel = new StorageTemplateListModel();
        templateListModel.setIsAvailable(false);

        isoListModel = new StorageIsoListModel();
        isoListModel.setIsAvailable(false);

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new StorageGeneralModel());
        list.add(new StorageDataCenterListModel());
        list.add(vmBackupModel);
        list.add(templateBackupModel);
        list.add(vmListModel);
        list.add(templateListModel);
        list.add(isoListModel);
        list.add(new StorageEventListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("storage"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.StorageDomain);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.StorageDomain, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    private void NewDomain()
    {
        if (getWindow() != null)
        {
            return;
        }

        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDomainTitle());
        model.setHashName("new_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());

        ArrayList<IStorageModel> items = new ArrayList<IStorageModel>();
        // putting all Data domains at the beginning on purpose (so when choosing the
        // first selectable storage type/function, it will be a Data one, if relevant).

        NfsStorageModel nfsDataModel = new NfsStorageModel();
        nfsDataModel.setRole(StorageDomainType.Data);
        items.add(nfsDataModel);

        IscsiStorageModel iscsiDataModel = new IscsiStorageModel();
        iscsiDataModel.setRole(StorageDomainType.Data);
        iscsiDataModel.setIsGrouppedByTarget(true);
        items.add(iscsiDataModel);

        FcpStorageModel fcpDataModel = new FcpStorageModel();
        fcpDataModel.setRole(StorageDomainType.Data);
        items.add(fcpDataModel);

        LocalStorageModel localDataModel = new LocalStorageModel();
        localDataModel.setRole(StorageDomainType.Data);
        items.add(localDataModel);

        PosixStorageModel posixDataModel = new PosixStorageModel();
        posixDataModel.setRole(StorageDomainType.Data);
        items.add(posixDataModel);

        NfsStorageModel nfsIsoModel = new NfsStorageModel();
        nfsIsoModel.setRole(StorageDomainType.ISO);
        items.add(nfsIsoModel);

        NfsStorageModel nfsExportModel = new NfsStorageModel();
        nfsExportModel.setRole(StorageDomainType.ImportExport);
        items.add(nfsExportModel);

        IscsiStorageModel iscsiExportModel = new IscsiStorageModel();
        iscsiExportModel.setRole(StorageDomainType.ImportExport);
        iscsiExportModel.setIsGrouppedByTarget(true);
        items.add(iscsiExportModel);

        FcpStorageModel fcpExportModel = new FcpStorageModel();
        fcpExportModel.setRole(StorageDomainType.ImportExport);
        items.add(fcpExportModel);

        model.setItems(items);

        model.Initialize();


        UICommand command;
        command = new UICommand("OnSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void Edit()
    {
        storage_domains storage = (storage_domains) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editDomainTitle());
        model.setHashName("edit_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
        model.setStorage(storage);
        model.getName().setEntity(storage.getstorage_name());
        model.setOriginalName(storage.getstorage_name());

        model.getDataCenter().setIsChangable(false);
        model.getFormat().setIsChangable(false);

        boolean isStorageActive = model.isStorageActive();
        boolean isNew = storage == null;

        model.getHost().setIsChangable(isNew);
        model.getName().setIsChangable(isStorageActive);
        model.getAvailableStorageItems().setIsChangable(isStorageActive);
        model.setIsChangable(isStorageActive);

        IStorageModel item = null;
        switch (storage.getstorage_type()) {
            case NFS:
                item = PrepareNfsStorageForEdit(storage);
                break;

            case FCP:
                item = PrepareFcpStorageForEdit(storage);
                break;

            case ISCSI:
                item = PrepareIscsiStorageForEdit(storage);
                break;

            case LOCALFS:
                item = PrepareLocalStorageForEdit(storage);
                break;

            case POSIXFS:
                item = PreparePosixStorageForEdit(storage);
                break;
        }

        model.setItems(new ArrayList<IStorageModel>(Arrays.asList(new IStorageModel[] {item})));
        model.setSelectedItem(item);

        model.Initialize();

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case Storage: {
                model.getName().setIsChangable(false);
                model.getName().setInfo("Cannot edit Storage Domains's Name in this tree context"); //$NON-NLS-1$
            }
                break;
            }
        }

        UICommand command;
        if (isStorageActive) {
            command = new UICommand("OnSave", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().ok());
            command.setIsDefault(true);
            model.getCommands().add(command);

            command = new UICommand("Cancel", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            command.setIsCancel(true);
            model.getCommands().add(command);
        }
        else {
            command = new UICommand("Cancel", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().close());
            command.setIsCancel(true);
            model.getCommands().add(command);
        }
    }

    private IStorageModel PrepareNfsStorageForEdit(storage_domains storage)
    {
        final NfsStorageModel model = new NfsStorageModel();
        model.setRole(storage.getstorage_domain_type());
        model.setIsEditMode(true);

        AsyncDataProvider.GetStorageConnectionById(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                storage_server_connections connection = (storage_server_connections) returnValue;
                model.getPath().setEntity(connection.getconnection());
                model.getRetransmissions().setEntity(connection.getNfsRetrans());
                model.getTimeout().setEntity(connection.getNfsTimeo());

                for (Object item : model.getVersion().getItems()) {

                    EntityModel itemModel = (EntityModel) item;
                    boolean noNfsVersion = itemModel.getEntity() == null && connection.getNfsVersion() == null;
                    boolean foundNfsVersion = itemModel.getEntity() != null &&
                            itemModel.getEntity().equals(connection.getNfsVersion());

                    if (noNfsVersion || foundNfsVersion) {
                        model.getVersion().setSelectedItem(item);
                        break;
                    }
                }

                // If any settings were overridden, reflect this in the override checkbox
                model.getOverride().setEntity(
                        connection.getNfsVersion() != null ||
                        connection.getNfsRetrans() != null ||
                        connection.getNfsTimeo() != null);

            }
        }), storage.getstorage(), true);

        return model;
    }

    private IStorageModel PrepareLocalStorageForEdit(storage_domains storage)
    {
        LocalStorageModel model = new LocalStorageModel();
        model.setRole(storage.getstorage_domain_type());
        model.getPath().setIsAvailable(false);

        AsyncDataProvider.GetStorageConnectionById(new AsyncQuery(model, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                LocalStorageModel localStorageModel = (LocalStorageModel) target;
                storage_server_connections connection = (storage_server_connections) returnValue;
                localStorageModel.getPath().setEntity(connection.getconnection());

            }
        }), storage.getstorage(), true);

        return model;
    }

    private IStorageModel PreparePosixStorageForEdit(storage_domains storage) {

        final PosixStorageModel model = new PosixStorageModel();
        model.setRole(storage.getstorage_domain_type());
        model.getPath().setIsChangable(false);
        model.getVfsType().setIsChangable(false);
        model.getMountOptions().setIsChangable(false);

        AsyncDataProvider.GetStorageConnectionById(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                storage_server_connections connection = (storage_server_connections) returnValue;
                model.getPath().setEntity(connection.getconnection());
                model.getVfsType().setEntity(connection.getVfsType());
                model.getMountOptions().setEntity(connection.getMountOptions());

            }
        }), storage.getstorage(), true);

        return model;
    }

    private IStorageModel PrepareIscsiStorageForEdit(storage_domains storage)
    {
        IscsiStorageModel model = new IscsiStorageModel();
        model.setRole(storage.getstorage_domain_type());

        PrepareSanStorageForEdit(model);

        return model;
    }

    private IStorageModel PrepareFcpStorageForEdit(storage_domains storage)
    {
        FcpStorageModel model = new FcpStorageModel();
        model.setRole(storage.getstorage_domain_type());

        PrepareSanStorageForEdit(model);

        return model;
    }

    private void PrepareSanStorageForEdit(final SanStorageModel model)
    {
        StorageModel storageModel = (StorageModel) getWindow();
        storage_domains storage = (storage_domains) getSelectedItem();
        boolean isStorageActive = storageModel.isStorageActive();

        if (isStorageActive) {
            storageModel.getHost().getSelectedItemChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    PostPrepareSanStorageForEdit(model, true);
                }
            });
        }
        else {
            PostPrepareSanStorageForEdit(model, false);
        }
    }

    private void PostPrepareSanStorageForEdit(SanStorageModel model, boolean isStorageActive)
    {
        StorageModel storageModel = (StorageModel) getWindow();
        storage_domains storage = (storage_domains) getSelectedItem();
        model.setStorageDomain(storage);

        VDS host = (VDS) storageModel.getHost().getSelectedItem();
        Guid hostId = host != null && isStorageActive ? host.getId() : null;

        AsyncDataProvider.GetLunsByVgId(new AsyncQuery(model, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                SanStorageModel sanStorageModel = (SanStorageModel) target;
                ArrayList<LUNs> lunList = (ArrayList<LUNs>) returnValue;
                sanStorageModel.ApplyData(lunList, true);
            }
        }, storageModel.getHash()), storage.getstorage(), hostId);
    }

    private void ImportDomain()
    {
        if (getWindow() != null)
        {
            return;
        }

        StorageModel model = new StorageModel(new ImportStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().importPreConfiguredDomainTitle());
        model.setHashName("import_pre-configured_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
        model.getName().setIsAvailable(false);
        model.getFormat().setIsAvailable(false);

        ArrayList<IStorageModel> items = new ArrayList<IStorageModel>();
        NfsStorageModel tempVar = new NfsStorageModel();
        tempVar.setRole(StorageDomainType.ISO);
        items.add(tempVar);
        NfsStorageModel tempVar2 = new NfsStorageModel();
        tempVar2.setRole(StorageDomainType.ImportExport);
        items.add(tempVar2);

        model.setItems(items);

        model.Initialize();


        UICommand command;
        command = new UICommand("OnImport", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void OnImport()
    {
        StorageModel model = (StorageModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        model.StartProgress(ConstantsManager.getInstance().getConstants().importingStorageDomainProgress());

        VDS host = (VDS) model.getHost().getSelectedItem();

        // Save changes.
        if (model.getSelectedItem() instanceof NfsStorageModel)
        {
            NfsStorageModel nfsModel = (NfsStorageModel) model.getSelectedItem();
            nfsModel.setMessage(null);

            Task.Create(this,
                    new ArrayList<Object>(Arrays.asList(new Object[] { "ImportNfs", //$NON-NLS-1$
                            host.getId(), nfsModel.getPath().getEntity(), nfsModel.getRole() }))).Run();
        }
        else
        {
            Task.Create(this,
                    new ArrayList<Object>(Arrays.asList(new Object[] { "ImportSan", //$NON-NLS-1$
                            host.getId() }))).Run();
        }
    }

    public void StorageNameValidation()
    {
        StorageModel model = (StorageModel) getWindow();
        String name = (String) model.getName().getEntity();
        model.getName().setIsValid(true);

        AsyncDataProvider.IsStorageDomainNameUnique(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                StorageListModel storageListModel = (StorageListModel) target;
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();

                String name1 = (String) storageModel.getName().getEntity();
                String tempVar = storageModel.getOriginalName();
                String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$
                boolean isNameUnique = (Boolean) returnValue;

                if (!isNameUnique && name1.compareToIgnoreCase(originalName) != 0) {
                    storageModel.getName()
                        .getInvalidityReasons()
                        .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                    storageModel.getName().setIsValid(false);
                    storageListModel.PostStorageNameValidation();
                } else {

                    AsyncDataProvider.GetStorageDomainMaxNameLength(new AsyncQuery(storageListModel, new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target1, Object returnValue1) {

                            StorageListModel storageListModel1 = (StorageListModel) target1;
                            StorageModel storageModel1 = (StorageModel) storageListModel1.getWindow();
                            int nameMaxLength = (Integer) returnValue1;
                            RegexValidation tempVar2 = new RegexValidation();
                            tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                            tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                .nameCanContainOnlyMsg(nameMaxLength));
                            storageModel1.getName().ValidateEntity(new IValidation[] {
                                new NotEmptyValidation(), tempVar2});
                            storageListModel1.PostStorageNameValidation();

                        }
                    }));
                }

            }
        }),
            name);
    }

    public void PostStorageNameValidation()
    {
        if (getLastExecutedCommand().getName().equals("OnSave")) //$NON-NLS-1$
        {
            OnSavePostNameValidation();
        }
    }

    private void CleanConnection(storage_server_connections connection, Guid hostId) {

        Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, hostId), new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

            }
        }, this);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        RemoveStorageModel model = new RemoveStorageModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeStoragesTitle());
        model.setHashName("remove_storage"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToRemoveTheStorageDomainMsg());
        model.getFormat().setIsAvailable(false);

        AsyncDataProvider.GetHostListByStatus(new AsyncQuery(new Object[] {this, model}, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                Object[] array = (Object[]) target;
                StorageListModel storageListModel = (StorageListModel) array[0];
                RemoveStorageModel removeStorageModel = (RemoveStorageModel) array[1];
                storage_domains storage = (storage_domains) storageListModel.getSelectedItem();
                ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                removeStorageModel.getHostList().setItems(hosts);
                removeStorageModel.getHostList().setSelectedItem(Linq.FirstOrDefault(hosts));
                removeStorageModel.getFormat()
                    .setIsAvailable(storage.getstorage_domain_type() == StorageDomainType.ISO
                        || storage.getstorage_domain_type() == StorageDomainType.ImportExport);

                if (hosts.isEmpty()) {

                    UICommand tempVar = new UICommand("Cancel", storageListModel); //$NON-NLS-1$
                    tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                    tempVar.setIsDefault(true);
                    tempVar.setIsCancel(true);
                    removeStorageModel.getCommands().add(tempVar);
                } else {

                    UICommand command;
                    command = new UICommand("OnRemove", storageListModel); //$NON-NLS-1$
                    command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                    command.setIsDefault(true);
                    removeStorageModel.getCommands().add(command);

                    command = new UICommand("Cancel", storageListModel); //$NON-NLS-1$
                    command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                    command.setIsCancel(true);
                    removeStorageModel.getCommands().add(command);
                }

            }
        }), VDSStatus.Up);
    }

    private void OnRemove()
    {
        if (getSelectedItem() != null)
        {
            storage_domains storage = (storage_domains) getSelectedItem();
            RemoveStorageModel model = (RemoveStorageModel) getWindow();

            if (!model.Validate())
            {
                return;
            }

            VDS host = (VDS) model.getHostList().getSelectedItem();

            RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(storage.getId());
            tempVar.setVdsId(host.getId());
            tempVar.setDoFormat((storage.getstorage_domain_type() == StorageDomainType.Data || storage.getstorage_domain_type() == StorageDomainType.Master) ? true
                : (Boolean) model.getFormat().getEntity());

            Frontend.RunAction(VdcActionType.RemoveStorageDomain, tempVar,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, this);
        }

        Cancel();
    }

    private void Destroy()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().destroyStorageDomainTitle());
        model.setHashName("destroy_storage_domain"); //$NON-NLS-1$
        ArrayList<String> items = new ArrayList<String>();
        items.add(((storage_domains) getSelectedItem()).getstorage_name());
        model.setItems(items);

        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangable(true);


        UICommand command;
        command = new UICommand("OnDestroy", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void OnDestroy()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        storage_domains storageDomain = (storage_domains) getSelectedItem();

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ForceRemoveStorageDomain,
            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] {new StorageDomainParametersBase(storageDomain.getId())})),
            new IFrontendMultipleActionAsyncCallback() {
                @Override
                public void Executed(FrontendMultipleActionAsyncResult result) {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.StopProgress();
                    Cancel();

                }
            },
            model);
    }

    private void OnSave()
    {
        StorageNameValidation();
    }

    private void OnSavePostNameValidation()
    {
        StorageModel model = (StorageModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        if (model.getSelectedItem() instanceof NfsStorageModel)
        {
            SaveNfsStorage();
        }
        else if (model.getSelectedItem() instanceof LocalStorageModel)
        {
            SaveLocalStorage();
        }
        else if (model.getSelectedItem() instanceof PosixStorageModel)
        {
            SavePosixStorage();
        }
        else
        {
            SaveSanStorage();
        }
    }

    private void SaveLocalStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveLocal" }))).Run(); //$NON-NLS-1$
    }

    private void  SaveNfsStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveNfs" }))).Run(); //$NON-NLS-1$
    }

    private void SavePosixStorage() {

        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] {"SavePosix"}))).Run(); //$NON-NLS-1$
    }

    private void SaveSanStorage()
    {
        StorageModel storageModel = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getSelectedItem();
        ArrayList<String> usedLunsMessages = sanStorageModel.getUsedLunsMessages();

        if (usedLunsMessages.isEmpty()) {
            OnSaveSanStorage();
        }
        else {
            ForceCreationWarning(usedLunsMessages);
        }
    }

    private void OnSaveSanStorage()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (confirmationModel != null && !confirmationModel.Validate())
        {
            return;
        }

        CancelConfirm();
        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveSan" }))).Run(); //$NON-NLS-1$
    }

    private void ForceCreationWarning(ArrayList<String> usedLunsMessages) {
        StorageModel storageModel = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getSelectedItem();
        sanStorageModel.setForce(true);

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().forceStorageDomainCreation());
        model.setMessage(ConstantsManager.getInstance().getConstants().lunsAlreadyInUse());
        model.setHashName("force_storage_domain_creation"); //$NON-NLS-1$
        model.setItems(usedLunsMessages);

        UICommand command;
        command = new UICommand("OnSaveSanStorage", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void CancelConfirm()
    {
        setConfirmWindow(null);
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
    protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.ItemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            storage_domains storage = (storage_domains) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.FirstOrDefault(Linq.<storage_domains> Cast(getItems()),
                    new Linq.StoragePredicate(storage.getId())));
        }
    }

    @Override
    protected void UpdateDetailsAvailability()
    {
        if (getSelectedItem() != null)
        {
            storage_domains storage = (storage_domains) getSelectedItem();
            boolean isBackupStorage = storage.getstorage_domain_type() == StorageDomainType.ImportExport;
            boolean isDataStorage =
                    storage.getstorage_domain_type() == StorageDomainType.Data
                            || storage.getstorage_domain_type() == StorageDomainType.Master;
            boolean isIsoStorage = storage.getstorage_domain_type() == StorageDomainType.ISO;

            vmBackupModel.setIsAvailable(isBackupStorage);
            templateBackupModel.setIsAvailable(isBackupStorage);

            vmListModel.setIsAvailable(isDataStorage);
            templateListModel.setIsAvailable(isDataStorage);

            isoListModel.setIsAvailable(isIsoStorage);
        }
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

        if (e.PropertyName.equals("storage_domain_shared_status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        ArrayList<storage_domains> items =
                getSelectedItems() != null ? Linq.<storage_domains> Cast(getSelectedItems())
                        : new ArrayList<storage_domains>();

        storage_domains item = (storage_domains) getSelectedItem();

        getNewDomainCommand().setIsAvailable(true);

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && isEditAvailable(item));

        getRemoveCommand().setIsExecutionAllowed(items.size() == 1
                && Linq.FindAllStorageDomainsBySharedStatus(items, StorageDomainSharedStatus.Unattached).size() == items.size());

        getDestroyCommand().setIsExecutionAllowed(item != null && items.size() == 1);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage);

        getNewDomainCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    private boolean isEditAvailable(storage_domains storageDomain) {
        if (storageDomain == null) {
            return false;
        }

        boolean isActive = storageDomain.getstorage_domain_shared_status() == StorageDomainSharedStatus.Active
                || storageDomain.getstorage_domain_shared_status() == StorageDomainSharedStatus.Mixed;
        boolean isBlockStorage = storageDomain.getstorage_type().isBlockDomain();

        return isBlockStorage ? true : isActive;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewDomainCommand())
        {
            NewDomain();
        }
        else if (command == getImportDomainCommand())
        {
            ImportDomain();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getDestroyCommand())
        {
            Destroy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnImport")) //$NON-NLS-1$
        {
            OnImport();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDestroy")) //$NON-NLS-1$
        {
            OnDestroy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveSanStorage")) //$NON-NLS-1$
        {
            OnSaveSanStorage();
        }
    }

    private void SavePosixStorage(TaskContext context) {

        this.context = context;

        storage_domains selectedItem = (storage_domains) getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        PosixStorageModel posixModel = (PosixStorageModel) storageModel;
        path = (String) posixModel.getPath().getEntity();

        storageDomain = isNew ? new StorageDomainStatic() : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());
        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());
        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());
        storageDomain.setstorage_name((String) model.getName().getEntity());
        storageDomain.setStorageFormat((StorageFormatType) model.getFormat().getSelectedItem());

        if (isNew) {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<storage_domains> storages = (ArrayList<storage_domains>) returnValue;

                    if (storages != null && storages.size() > 0) {
                        String storageName = storages.get(0).getstorage_name();

                        OnFinish(storageListModel.context,
                            false,
                            storageListModel.storageModel,
                            ConstantsManager.getInstance().getMessages().createFailedDomainAlreadyExistStorageMsg(storageName));
                    } else {
                        storageListModel.SaveNewPosixStorage();
                    }
                }
            }), null, path);
        } else {

            Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain), new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);

                }
            }, this);
        }
    }

    public void SaveNewPosixStorage() {

        StorageModel model = (StorageModel) getWindow();
        PosixStorageModel posixModel = (PosixStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        storage_server_connections connection = new storage_server_connections();
        connection.setconnection(path);
        connection.setstorage_type(posixModel.getType());
        connection.setVfsType((String) posixModel.getVfsType().getEntity());
        connection.setMountOptions((String) posixModel.getMountOptions().getEntity());
        this.connection = connection;

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddPosixFsStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(this.connection, host.getId()));
        StorageDomainManagementParameter parameter = new StorageDomainManagementParameter(storageDomain);
        parameter.setVdsId(host.getId());
        parameters.add(parameter);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setstorage((String) vdcReturnValueBase.getActionReturnValue());
            }
        };

        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageId = (NGuid) vdcReturnValueBase.getActionReturnValue();

                // Attach storage to data center as necessary.
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                    storageListModel.AttachStorageToDataCenter((Guid) storageListModel.storageId, dataCenter.getId());
                }

                storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);
            }
        };

        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.CleanConnection(storageListModel.connection, storageListModel.hostId);
                storageListModel.OnFinish(storageListModel.context, false, storageListModel.storageModel);
            }
        };

        Frontend.RunMultipleActions(actionTypes,
            parameters,
            new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2 })),
            failureCallback,
            this);
    }

    private void SaveNfsStorage(TaskContext context)
    {
        this.context = context;

        storage_domains selectedItem = (storage_domains) getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = (String) nfsModel.getPath().getEntity();

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setstorage_name((String) model.getName().getEntity());
        storageDomain.setStorageFormat((StorageFormatType) model.getFormat().getSelectedItem());

        if (isNew)
        {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<storage_domains> storages = (ArrayList<storage_domains>) returnValue;

                    if (storages != null && storages.size() > 0) {
                        String storageName = storages.get(0).getstorage_name();

                        OnFinish(storageListModel.context,
                            false,
                            storageListModel.storageModel,
                            ConstantsManager.getInstance().getMessages().createFailedDomainAlreadyExistStorageMsg(storageName));
                    } else {
                        storageListModel.SaveNewNfsStorage();
                    }
                }
            }), null, path);
        }
        else
        {
            Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);

                    }
                }, this);
        }
    }

    public void SaveNewNfsStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        NfsStorageModel nfsModel = (NfsStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        storage_server_connections tempVar = new storage_server_connections();
        tempVar.setconnection(path);
        tempVar.setstorage_type(nfsModel.getType());
        if ((Boolean) nfsModel.getOverride().getEntity()) {
            tempVar.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
            tempVar.setNfsRetrans(nfsModel.getRetransmissions().AsConvertible().nullableShort());
            tempVar.setNfsTimeo(nfsModel.getTimeout().AsConvertible().nullableShort());
        }
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddNFSStorageDomain);
        actionTypes.add(VdcActionType.RemoveStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setstorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageId = (NGuid) vdcReturnValueBase.getActionReturnValue();

            }
        };
        IFrontendActionAsyncCallback callback3 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();

                // Attach storage to data center as necessary.
                storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                {
                    storageListModel.AttachStorageToDataCenter((Guid) storageListModel.storageId, dataCenter.getId());
                }

                storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.CleanConnection(storageListModel.connection, storageListModel.hostId);
                storageListModel.OnFinish(storageListModel.context, false, storageListModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2, callback3 })),
                failureCallback,
                this);
    }

    public void SaveNewSanStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        boolean force = sanModel.isForce();

        ArrayList<String> lunIds = new ArrayList<String>();
        for (LunModel lun : sanModel.getAddedLuns())
        {
            lunIds.add(lun.getLunId());
        }

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storageDomain);
        params.setVdsId(host.getId());
        params.setLunIds(lunIds);
        params.setForce(force);
        Frontend.RunAction(VdcActionType.AddSANStorageDomain, params,
            new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                        storageListModel.storageModel = storageModel.getSelectedItem();
                        if (!result.getReturnValue().getSucceeded()) {
                            storageListModel.OnFinish(storageListModel.context, false, storageListModel.storageModel);
                            return;
                        }

                        storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                            VdcReturnValueBase returnValue = result.getReturnValue();
                            NGuid storageId = (NGuid) returnValue.getActionReturnValue();
                            storageListModel.AttachStorageToDataCenter((Guid) storageId, dataCenter.getId());
                        }

                    storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);
                }
            }, this);
    }

    private void SaveLocalStorage(TaskContext context)
    {
        this.context = context;

        storage_domains selectedItem = (storage_domains) getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        VDS host = (VDS) model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = (String) localModel.getPath().getEntity();

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setstorage_name((String) model.getName().getEntity());

        if (isNew)
        {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<storage_domains> storages = (ArrayList<storage_domains>) returnValue;
                    if (storages != null && storages.size() > 0) {
                        String storageName = storages.get(0).getstorage_name();

                        OnFinish(storageListModel.context,
                            false,
                            storageListModel.storageModel,
                            ConstantsManager.getInstance().getMessages().createFailedDomainAlreadyExistStorageMsg(storageName));
                    } else {
                        storageListModel.SaveNewLocalStorage();
                    }

                }
            }), host.getStoragePoolId(), path);
        }
        else
        {
            Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain), new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);

                }
            }, this);
        }
    }

    public void SaveNewLocalStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        LocalStorageModel localModel = (LocalStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        storage_server_connections tempVar = new storage_server_connections();
        tempVar.setconnection(path);
        tempVar.setstorage_type(localModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddLocalStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.removeConnection = true;

                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setstorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.removeConnection = false;

                storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();

                if (storageListModel.removeConnection)
                {
                    storageListModel.CleanConnection(storageListModel.connection, storageListModel.hostId);
                    storageListModel.removeConnection = false;
                }

                storageListModel.OnFinish(storageListModel.context, false, storageListModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2 })),
                failureCallback,
                this);
    }

    public void OnFinish(TaskContext context, boolean isSucceeded, IStorageModel model)
    {
        OnFinish(context, isSucceeded, model, null);
    }

    public void OnFinish(TaskContext context, boolean isSucceeded, IStorageModel model, String message)
    {
        context.InvokeUIThread(this,
                new ArrayList<Object>(Arrays.asList(new Object[] { "Finish", isSucceeded, model, //$NON-NLS-1$
                        message })));
    }

    private void SaveSanStorage(TaskContext context)
    {
        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getSelectedItem();
        storage_domains storage = (storage_domains) getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();

        boolean isNew = model.getStorage() == null;

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(storage.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? sanModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? sanModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setStorageFormat(isNew ? (StorageFormatType) sanModel.getContainer()
                .getFormat()
                .getSelectedItem() : storageDomain.getStorageFormat());

        storageDomain.setstorage_name((String) model.getName().getEntity());

        if (isNew)
        {
            SaveNewSanStorage();
        }
        else
        {
            Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain), new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    StorageModel storageModel = (StorageModel) getWindow();
                    SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getSelectedItem();
                    boolean force = sanStorageModel.isForce();
                    storage_domains storageDomain1 = (storage_domains) storageListModel.getSelectedItem();
                    ArrayList<String> lunIds = new ArrayList<String>();

                    for (LunModel lun : sanStorageModel.getAddedLuns()) {
                        lunIds.add(lun.getLunId());
                    }

                    if (lunIds.size() > 0) {
                        Frontend.RunAction(VdcActionType.ExtendSANStorageDomain,
                            new ExtendSANStorageDomainParameters(storageDomain1.getId(), lunIds, force),
                            new IFrontendActionAsyncCallback() {
                                @Override
                                public void Executed(FrontendActionAsyncResult result1) {

                                }
                            },
                            this);
                    }
                    storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);
                }
            }, this);
        }
    }

    private void AttachStorageToDataCenter(Guid storageId, Guid dataCenterId)
    {
        Frontend.RunAction(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(storageId,
            dataCenterId),
            new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                }
            }, this);
    }

    private void ImportNfsStorage(TaskContext context)
    {
        this.context = context;

        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        StorageModel model = (StorageModel) getWindow();

        storageModel = model.getSelectedItem();
        hostId = (Guid) data.get(1);
        path = (String) data.get(2);
        domainType = (StorageDomainType) data.get(3);

        ImportNfsStorageInit();
    }

    public void ImportNfsStorageInit()
    {
        if (nfsConnection != null)
        {
            // Clean nfs connection
            Frontend.RunAction(VdcActionType.RemoveStorageServerConnection,
                new StorageServerConnectionParametersBase(nfsConnection, hostId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success) {
                            storageListModel.nfsConnection = null;
                        }
                        storageListModel.ImportNfsStoragePostInit();

                    }
                },
                this);
        }
        else
        {
            ImportNfsStoragePostInit();
        }
    }

    public void ImportNfsStoragePostInit()
    {
        // Check storage domain existence
        AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                StorageListModel storageListModel = (StorageListModel) target;
                ArrayList<storage_domains> storages = (ArrayList<storage_domains>) returnValue;

                if (storages != null && storages.size() > 0) {

                    String storageName = storages.get(0).getstorage_name();
                    OnFinish(storageListModel.context,
                        false,
                        storageListModel.storageModel,
                        ConstantsManager.getInstance().getMessages().importFailedDomainAlreadyExistStorageMsg(storageName));
                } else {
                    storage_server_connections tempVar = new storage_server_connections();
                    storageModel = storageListModel.storageModel;
                    NfsStorageModel nfsModel = (NfsStorageModel) storageModel;

                    tempVar.setconnection(storageListModel.path);
                    tempVar.setstorage_type(StorageType.NFS);
                    if ((Boolean) nfsModel.getOverride().getEntity()) {
                        tempVar.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
                        tempVar.setNfsRetrans(nfsModel.getRetransmissions().AsConvertible().nullableShort());
                        tempVar.setNfsTimeo(nfsModel.getTimeout().AsConvertible().nullableShort());
                    }
                    storageListModel.nfsConnection = tempVar;
                    storageListModel.ImportNfsStorageConnect();
                }
            }
        }), null, path);
    }

    public void ImportNfsStorageConnect()
    {
        Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(nfsConnection, hostId),
            new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success)
                        {
                            AsyncDataProvider.GetExistingStorageDomainList(new AsyncQuery(storageListModel,
                                    new INewAsyncCallback() {
                                        @Override
                                        public void OnSuccess(Object target, Object returnValue) {

                                            StorageListModel storageListModel1 = (StorageListModel) target;
                                            ArrayList<storage_domains> domains = (ArrayList<storage_domains>) returnValue;
                                            if (domains != null)
                                            {
                                                if (domains.isEmpty())
                                                {
                                                    PostImportNfsStorage(storageListModel1.context,
                                                            false,
                                                            storageListModel1.storageModel,
                                                            ConstantsManager.getInstance()
                                                                    .getConstants()
                                                                    .thereIsNoStorageDomainUnderTheSpecifiedPathMsg());
                                                }
                                                else
                                                {
                                                    storageListModel1.ImportNfsStorageAddDomain(domains);
                                                }
                                            }
                                            else
                                            {
                                                PostImportNfsStorage(storageListModel1.context,
                                                        false,
                                                        storageListModel1.storageModel,
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .failedToRetrieveExistingStorageDomainInformationMsg());
                                            }

                                        }
                                    }),
                                    hostId,
                                    domainType,
                                    path);
                        }
                        else
                        {
                            PostImportNfsStorage(storageListModel.context,
                                    false,
                                    storageListModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getConstants()
                                            .failedToRetrieveExistingStorageDomainInformationMsg());
                        }

                    }
                },
                this);
    }

    public void ImportNfsStorageAddDomain(ArrayList<storage_domains> domains)
    {
        storage_domains sdToAdd = Linq.FirstOrDefault(domains);
        StorageDomainStatic sdsToAdd = sdToAdd == null ? null : sdToAdd.getStorageStaticData();

        StorageDomainManagementParameter params = new StorageDomainManagementParameter(sdsToAdd);
        params.setVdsId(hostId);
        Frontend.RunAction(VdcActionType.AddExistingNFSStorageDomain, params, new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                Object[] array = (Object[]) result.getState();
                StorageListModel storageListModel = (StorageListModel) array[0];
                storage_domains sdToAdd1 = (storage_domains) array[1];
                VdcReturnValueBase returnVal = result.getReturnValue();

                boolean success = returnVal != null && returnVal.getSucceeded();
                if (success) {

                    StorageModel model = (StorageModel) storageListModel.getWindow();
                    storage_pool dataCenter = (storage_pool) model.getDataCenter().getSelectedItem();
                    if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                        storageListModel.AttachStorageToDataCenter(sdToAdd1.getId(), dataCenter.getId());
                    }
                    PostImportNfsStorage(storageListModel.context, true, storageListModel.storageModel, null);

                } else {
                    PostImportNfsStorage(storageListModel.context, false, storageListModel.storageModel, ""); //$NON-NLS-1$
                }
            }
        }, new Object[] {this, sdToAdd});
    }

    public void PostImportNfsStorage(TaskContext context, boolean isSucceeded, IStorageModel model, String message)
    {
        Frontend.RunAction(VdcActionType.RemoveStorageServerConnection,
            new StorageServerConnectionParametersBase(nfsConnection, hostId),
            new IFrontendActionAsyncCallback() {
                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    VdcReturnValueBase returnValue = result.getReturnValue();
                    boolean success = returnValue != null && returnValue.getSucceeded();
                    if (success) {
                        nfsConnection = null;
                    }
                    Object[] array = (Object[]) result.getState();
                    OnFinish((TaskContext) array[0],
                        (Boolean) array[1],
                        (IStorageModel) array[2],
                        (String) array[3]);

                }
            },
            new Object[] {context, isSucceeded, model, message});
    }

    private void ImportSanStorage(TaskContext context)
    {
        this.context = context;

        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        StorageModel storageModel = (StorageModel) getWindow();
        ImportSanStorageModel sanStorageModel = (ImportSanStorageModel) storageModel.getSelectedItem();
        Guid hostId = (Guid) data.get(1);
        storage_domains storage;

        if (sanStorageModel.getSelectedItem() != null)
        {
            storage = (storage_domains) sanStorageModel.getSelectedItem();
        }
        else
        {
            ListModel candidates = sanStorageModel.getCandidatesList();
            EntityModel selectedItem = (EntityModel) candidates.getSelectedItem();
            storage = (storage_domains) selectedItem.getEntity();
        }

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storage == null ? null : storage.getStorageStaticData());
        params.setVdsId(hostId);

        Frontend.RunAction(VdcActionType.AddExistingSANStorageDomain, params, new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                Object[] array = (Object[]) result.getState();
                StorageListModel storageListModel = (StorageListModel) array[0];
                storage_domains sdToAdd1 = (storage_domains) array[1];
                VdcReturnValueBase returnVal = result.getReturnValue();

                boolean success = returnVal != null && returnVal.getSucceeded();
                if (success) {

                    StorageModel model = (StorageModel) storageListModel.getWindow();
                    storage_pool dataCenter = (storage_pool) model.getDataCenter().getSelectedItem();
                    if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                        storageListModel.AttachStorageToDataCenter(sdToAdd1.getId(), dataCenter.getId());
                    }
                    storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);
                }

                OnFinish(storageListModel.context, success, storageListModel.storageModel, null);
            }
        }, new Object[] {this, storage});
    }

    @Override
    public void run(TaskContext context)
    {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if (StringHelper.stringsEqual(key, "SaveNfs")) //$NON-NLS-1$
        {
            SaveNfsStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "SaveLocal")) //$NON-NLS-1$
        {
            SaveLocalStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "SavePosix")) //$NON-NLS-1$
        {
            SavePosixStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "SaveSan")) //$NON-NLS-1$
        {
            SaveSanStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "ImportNfs")) //$NON-NLS-1$
        {
            ImportNfsStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "ImportSan")) //$NON-NLS-1$
        {
            ImportSanStorage(context);
        }
        else if (StringHelper.stringsEqual(key, "Finish")) //$NON-NLS-1$
        {
            getWindow().StopProgress();

            if ((Boolean) data.get(1))
            {
                Cancel();
            }
            else
            {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
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
        return "StorageListModel"; //$NON-NLS-1$
    }

    @Override
    protected void OpenReport() {

        final ReportModel reportModel = super.createReportModel();

        List<storage_domains> items =
                getSelectedItems() != null && getSelectedItem() != null ? getSelectedItems()
                        : new ArrayList<storage_domains>();
        storage_domains storage = items.iterator().next();

        AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                List<storage_pool> dataCenters = (List<storage_pool>) returnValue;
                for (storage_pool dataCenter : dataCenters) {
                    reportModel.addDataCenterID(dataCenter.getId().toString());
                }

                if (reportModel == null) {
                    return;
                }

                setWidgetModel(reportModel);
            }
        }), storage.getId());
    }

    @Override
    protected void setReportModelResourceId(ReportModel reportModel, String idParamName, boolean isMultiple) {
        ArrayList<storage_domains> items =
                getSelectedItems() != null ? Linq.<storage_domains> Cast(getSelectedItems())
                        : new ArrayList<storage_domains>();

        if (idParamName != null) {
            for (storage_domains item : items) {
                if (isMultiple) {
                    reportModel.addResourceId(idParamName, item.getId().toString());
                } else {
                    reportModel.setResourceId(idParamName, item.getId().toString());
                }
            }
        }
    }
}
