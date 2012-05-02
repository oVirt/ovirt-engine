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
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
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

    public storage_domain_static storageDomain;
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
        NfsStorageModel tempVar = new NfsStorageModel();
        tempVar.setRole(StorageDomainType.Data);
        items.add(tempVar);
        IscsiStorageModel tempVar2 = new IscsiStorageModel();
        tempVar2.setRole(StorageDomainType.Data);
        tempVar2.setIsGrouppedByTarget(true);
        items.add(tempVar2);
        FcpStorageModel tempVar3 = new FcpStorageModel();
        tempVar3.setRole(StorageDomainType.Data);
        items.add(tempVar3);
        LocalStorageModel tempVar4 = new LocalStorageModel();
        tempVar4.setRole(StorageDomainType.Data);
        items.add(tempVar4);

        NfsStorageModel tempVar5 = new NfsStorageModel();
        tempVar5.setRole(StorageDomainType.ISO);
        items.add(tempVar5);

        NfsStorageModel tempVar6 = new NfsStorageModel();
        tempVar6.setRole(StorageDomainType.ImportExport);
        items.add(tempVar6);
        IscsiStorageModel tempVar7 = new IscsiStorageModel();
        tempVar7.setRole(StorageDomainType.ImportExport);
        tempVar7.setIsGrouppedByTarget(true);
        items.add(tempVar7);
        FcpStorageModel tempVar8 = new FcpStorageModel();
        tempVar8.setRole(StorageDomainType.ImportExport);
        items.add(tempVar8);

        model.setItems(items);

        model.Initialize();

        UICommand tempVar9 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar9.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar9.setIsDefault(true);
        model.getCommands().add(tempVar9);
        UICommand tempVar10 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar10.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar10.setIsCancel(true);
        model.getCommands().add(tempVar10);
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

        model.getDataCenter().setIsAvailable(false);
        model.getFormat().setIsAvailable(false);

        IStorageModel item = null;
        switch (storage.getstorage_type())
        {
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

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private IStorageModel PrepareNfsStorageForEdit(storage_domains storage)
    {
        NfsStorageModel model = new NfsStorageModel();
        model.setRole(storage.getstorage_domain_type());
        model.getPath().setIsAvailable(false);

        AsyncDataProvider.GetStorageConnectionById(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        NfsStorageModel nfsStorageModel = (NfsStorageModel) target;
                        storage_server_connections connection = (storage_server_connections) returnValue;
                        nfsStorageModel.getPath().setEntity(connection.getconnection());

                    }
                }), storage.getstorage(), true);

        return model;
    }

    private IStorageModel PrepareLocalStorageForEdit(storage_domains storage)
    {
        LocalStorageModel model = new LocalStorageModel();
        model.setRole(storage.getstorage_domain_type());
        model.getPath().setIsAvailable(false);

        AsyncDataProvider.GetStorageConnectionById(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        LocalStorageModel localStorageModel = (LocalStorageModel) target;
                        storage_server_connections connection = (storage_server_connections) returnValue;
                        localStorageModel.getPath().setEntity(connection.getconnection());

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

    private void PrepareSanStorageForEdit(SanStorageModel model)
    {
        storage_domains storage = (storage_domains) getSelectedItem();

        AsyncDataProvider.GetLunsByVgId(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        SanStorageModel sanStorageModel = (SanStorageModel) target;
                        ArrayList<LUNs> lunList = (ArrayList<LUNs>) returnValue;
                        sanStorageModel.ApplyData(lunList, true);

                    }
                }), storage.getstorage());
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

        IscsiImportStorageModel tempVar3 = new IscsiImportStorageModel();
        tempVar3.setRole(StorageDomainType.ImportExport);
        items.add(tempVar3);

        FcpImportStorageModel tempVar4 = new FcpImportStorageModel();
        tempVar4.setRole(StorageDomainType.ImportExport);
        items.add(tempVar4);

        model.setItems(items);

        model.Initialize();

        UICommand tempVar5 = new UICommand("OnImport", this); //$NON-NLS-1$
        tempVar5.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar5.setIsDefault(true);
        model.getCommands().add(tempVar5);
        UICommand tempVar6 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar6.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar6.setIsCancel(true);
        model.getCommands().add(tempVar6);
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

        AsyncDataProvider.IsStorageDomainNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        StorageListModel storageListModel = (StorageListModel) target;
                        StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                        String name1 = (String) storageModel.getName().getEntity();
                        String tempVar = storageModel.getOriginalName();
                        String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$
                        boolean isNameUnique = (Boolean) returnValue;
                        if (!isNameUnique && name1.compareToIgnoreCase(originalName) != 0)
                        {
                            storageModel.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            storageModel.getName().setIsValid(false);
                            storageListModel.PostStorageNameValidation();
                        }
                        else
                        {
                            AsyncDataProvider.GetStorageDomainMaxNameLength(new AsyncQuery(storageListModel,
                                    new INewAsyncCallback() {
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
                                                    new NotEmptyValidation(), tempVar2 });
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

    private void CleanConnection(storage_server_connections connection, Guid hostId)
    {
        Frontend.RunAction(VdcActionType.RemoveStorageServerConnection,
                new StorageServerConnectionParametersBase(connection, hostId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                },
                this);
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

        AsyncDataProvider.GetHostList(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
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
                        if (hosts.isEmpty())
                        {
                            UICommand tempVar = new UICommand("Cancel", storageListModel); //$NON-NLS-1$
                            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                            tempVar.setIsDefault(true);
                            tempVar.setIsCancel(true);
                            removeStorageModel.getCommands().add(tempVar);
                        }
                        else
                        {
                            UICommand tempVar2 = new UICommand("OnRemove", storageListModel); //$NON-NLS-1$
                            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
                            tempVar2.setIsDefault(true);
                            removeStorageModel.getCommands().add(tempVar2);
                            UICommand tempVar3 = new UICommand("Cancel", storageListModel); //$NON-NLS-1$
                            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                            tempVar3.setIsCancel(true);
                            removeStorageModel.getCommands().add(tempVar3);
                        }

                    }
                }));
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

        UICommand tempVar = new UICommand("OnDestroy", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
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
                new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new StorageDomainParametersBase(storageDomain.getId()) })),
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

    private void SaveNfsStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveNfs" }))).Run(); //$NON-NLS-1$
    }

    private void SaveSanStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveSan" }))).Run(); //$NON-NLS-1$
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

        getEditCommand().setIsExecutionAllowed(item != null
                && items.size() == 1
                && (item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Active || item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Mixed));

        getRemoveCommand().setIsExecutionAllowed(items.size() == 1
                && Linq.FindAllStorageDomainsBySharedStatus(items, StorageDomainSharedStatus.Unattached).size() == items.size());

        getDestroyCommand().setIsExecutionAllowed(item != null && items.size() == 1);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage);

        getNewDomainCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
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
                isNew ? new storage_domain_static()
                        : (storage_domain_static) Cloner.clone(selectedItem.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setstorage_name((String) model.getName().getEntity());
        storageDomain.setStorageFormat((StorageFormatType) model.getFormat().getSelectedItem());

        if (isNew)
        {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            StorageListModel storageListModel = (StorageListModel) target;
                            ArrayList<storage_domains> storages =
                                    (ArrayList<storage_domains>) returnValue;
                            if (storages != null && storages.size() > 0)
                            {
                                String storageName = storages.get(0).getstorage_name();
                                OnFinish(storageListModel.context,
                                        false,
                                        storageListModel.storageModel,
                                        ConstantsManager.getInstance()
                                                .getMessages()
                                                .createFailedDomainAlreadyExistStorageMsg(storageName));
                            }
                            else
                            {
                                storageListModel.SaveNewNfsStorage();
                            }

                        }
                    }),
                    null,
                    path);
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

                // Attach storage to data center as neccessary.
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

        ArrayList<String> lunIds = new ArrayList<String>();
        for (LunModel lun : sanModel.getAddedLuns())
        {
            lunIds.add(lun.getLunId());
        }

        AddSANStorageDomainParameters tempVar = new AddSANStorageDomainParameters(storageDomain);
        tempVar.setVdsId(host.getId());
        tempVar.setLunIds(lunIds);
        Frontend.RunAction(VdcActionType.AddSANStorageDomain, tempVar,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                        storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                        {
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
                isNew ? new storage_domain_static()
                        : (storage_domain_static) Cloner.clone(selectedItem.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setstorage_name((String) model.getName().getEntity());

        if (isNew)
        {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            StorageListModel storageListModel = (StorageListModel) target;
                            ArrayList<storage_domains> storages =
                                    (ArrayList<storage_domains>) returnValue;
                            if (storages != null && storages.size() > 0)
                            {
                                String storageName = storages.get(0).getstorage_name();
                                OnFinish(storageListModel.context,
                                        false,
                                        storageListModel.storageModel,
                                        ConstantsManager.getInstance()
                                                .getMessages()
                                                .createFailedDomainAlreadyExistStorageMsg(storageName));
                            }
                            else
                            {
                                storageListModel.SaveNewLocalStorage();
                            }

                        }
                    }),
                    host.getstorage_pool_id(),
                    path);
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
                isNew ? new storage_domain_static()
                        : (storage_domain_static) Cloner.clone(storage.getStorageStaticData());

        storageDomain.setstorage_type(isNew ? sanModel.getType() : storageDomain.getstorage_type());

        storageDomain.setstorage_domain_type(isNew ? sanModel.getRole() : storageDomain.getstorage_domain_type());

        storageDomain.setStorageFormat(isNew ? (StorageFormatType) sanModel.getContainer()
                .getFormat()
                .getSelectedItem() : storageDomain.getStorageFormat());

        storageDomain.setstorage_name((String) model.getName().getEntity());

        if (isNew)
        {
            AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            StorageListModel storageListModel = (StorageListModel) target;
                            ArrayList<storage_domains> storages =
                                    (ArrayList<storage_domains>) returnValue;
                            if (storages != null && storages.size() > 0)
                            {
                                String storageName = storages.get(0).getstorage_name();
                                OnFinish(storageListModel.context,
                                        false,
                                        storageListModel.storageModel,
                                        ConstantsManager.getInstance()
                                                .getMessages()
                                                .createFailedDomainAlreadyExistStorageMsg(storageName));
                            }
                            else
                            {
                                storageListModel.SaveNewSanStorage();
                            }

                        }
                    }),
                    null,
                    path);
        }
        else
        {
            Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            StorageListModel storageListModel = (StorageListModel) result.getState();
                            StorageModel storageModel = (StorageModel) getWindow();
                            SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getSelectedItem();
                            storage_domains storageDomain1 = (storage_domains) storageListModel.getSelectedItem();
                            ArrayList<String> lunIds = new ArrayList<String>();
                            for (LunModel lun : sanStorageModel.getAddedLuns())
                            {
                                lunIds.add(lun.getLunId());
                            }
                            if (lunIds.size() > 0)
                            {
                                Frontend.RunAction(VdcActionType.ExtendSANStorageDomain,
                                        new ExtendSANStorageDomainParameters(storageDomain1.getId(), lunIds),
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
                            if (success)
                            {
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
        // Check storage domain existance
        AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        StorageListModel storageListModel = (StorageListModel) target;
                        ArrayList<storage_domains> storages =
                                (ArrayList<storage_domains>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getstorage_name();
                            OnFinish(storageListModel.context,
                                    false,
                                    storageListModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .importFailedDomainAlreadyExistStorageMsg(storageName));
                        }
                        else
                        {
                            storage_server_connections tempVar = new storage_server_connections();
                            tempVar.setconnection(storageListModel.path);
                            tempVar.setstorage_type(StorageType.NFS);
                            storageListModel.nfsConnection = tempVar;
                            storageListModel.ImportNfsStorageConnect();
                        }

                    }
                }),
                null,
                path);
    }

    public void ImportNfsStorageConnect()
    {
        Frontend.RunAction(VdcActionType.ConnectStorageToVds, new StorageServerConnectionParametersBase(nfsConnection,
                hostId),
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
                                            ArrayList<storage_domains> domains =
                                                    (ArrayList<storage_domains>) returnValue;
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
        storage_domain_static sdsToAdd = sdToAdd == null ? null : sdToAdd.getStorageStaticData();

        StorageDomainManagementParameter tempVar = new StorageDomainManagementParameter(sdsToAdd);
        tempVar.setVdsId(hostId);
        Frontend.RunAction(VdcActionType.AddExistingNFSStorageDomain, tempVar,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        Object[] array = (Object[]) result.getState();
                        StorageListModel storageListModel = (StorageListModel) array[0];
                        storage_domains sdToAdd1 = (storage_domains) array[1];
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success)
                        {
                            StorageModel model = (StorageModel) storageListModel.getWindow();
                            storage_pool dataCenter = (storage_pool) model.getDataCenter().getSelectedItem();
                            if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                            {
                                storageListModel.AttachStorageToDataCenter(sdToAdd1.getId(), dataCenter.getId());
                            }
                            PostImportNfsStorage(storageListModel.context, true, storageListModel.storageModel, null);
                        }
                        else
                        {
                            PostImportNfsStorage(storageListModel.context, false, storageListModel.storageModel, ""); //$NON-NLS-1$
                        }

                    }
                }, new Object[] { this, sdToAdd });
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
                        if (success)
                        {
                            nfsConnection = null;
                        }
                        Object[] array = (Object[]) result.getState();
                        OnFinish((TaskContext) array[0],
                                (Boolean) array[1],
                                (IStorageModel) array[2],
                                (String) array[3]);

                    }
                },
                new Object[] { context, isSucceeded, model, message });
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

        AddSANStorageDomainParameters tempVar =
                new AddSANStorageDomainParameters(storage == null ? null : storage.getStorageStaticData());
        tempVar.setVdsId(hostId);
        Frontend.RunAction(VdcActionType.AddExistingSANStorageDomain, tempVar,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        Object[] array = (Object[]) result.getState();
                        StorageListModel storageListModel = (StorageListModel) array[0];
                        storage_domains sdToAdd1 = (storage_domains) array[1];
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success)
                        {
                            StorageModel model = (StorageModel) storageListModel.getWindow();
                            storage_pool dataCenter = (storage_pool) model.getDataCenter().getSelectedItem();
                            if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                            {
                                storageListModel.AttachStorageToDataCenter(sdToAdd1.getId(), dataCenter.getId());
                            }
                            storageListModel.OnFinish(storageListModel.context, true, storageListModel.storageModel);
                        }
                        OnFinish(storageListModel.context, success, storageListModel.storageModel, null);

                    }
                }, new Object[] { this, storage });
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
