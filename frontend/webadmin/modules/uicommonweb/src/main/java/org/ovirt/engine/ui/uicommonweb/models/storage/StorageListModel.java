package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsAndReportsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class StorageListModel extends ListWithDetailsAndReportsModel<Void, StorageDomain> implements ITaskTarget, ISupportSystemTreeContext {

    private UICommand newDomainCommand;

    public UICommand getNewDomainCommand() {
        return newDomainCommand;
    }

    private void setNewDomainCommand(UICommand value) {
        newDomainCommand = value;
    }

    private UICommand importDomainCommand;

    public UICommand getImportDomainCommand() {
        return importDomainCommand;
    }

    private void setImportDomainCommand(UICommand value) {
        importDomainCommand = value;
    }

    private UICommand editCommand;

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    private UICommand removeCommand;

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    private UICommand destroyCommand;

    public UICommand getDestroyCommand() {
        return destroyCommand;
    }

    private void setDestroyCommand(UICommand value) {
        destroyCommand = value;
    }

    private UICommand scanDisksCommand;

    public UICommand getScanDisksCommand() {
        return scanDisksCommand;
    }

    private void setScanDisksCommand(UICommand value) {
        scanDisksCommand = value;
    }

    @Inject
    public StorageListModel(final StorageGeneralModel storageGeneralModel,
            final StorageDataCenterListModel storageDataCenterListModel,
            final VmBackupModel storageVmBackupModel, final TemplateBackupModel storageTemplateBackupModel,
            final StorageRegisterVmListModel storageRegisterVmListModel,
            final StorageRegisterTemplateListModel storageRegisterTemplateListModel,
            final StorageRegisterDiskImageListModel storageRegisterDiskImageListModel,
            final StorageVmListModel storageVmListModel, final StorageTemplateListModel storageTemplateListModel,
            final StorageIsoListModel storageIsoListModel, final StorageDiskListModel storageDiskListModel,
            final StorageRegisterDiskListModel storageRegisterDiskListModel,
            final StorageSnapshotListModel storageSnapshotListModel, final DiskProfileListModel diskProfileListModel,
            final StorageEventListModel storageEventListModel,
            final PermissionListModel<StorageDomain> permissionListModel) {
        generalModel = storageGeneralModel;
        dcListModel = storageDataCenterListModel;
        vmBackupModel = storageVmBackupModel;
        templateBackupModel = storageTemplateBackupModel;
        vmRegisterListModel = storageRegisterVmListModel;
        templateRegisterListModel = storageRegisterTemplateListModel;
        diskImageRegisterListModel = storageRegisterDiskImageListModel;
        vmListModel = storageVmListModel;
        templateListModel = storageTemplateListModel;
        isoListModel = storageIsoListModel;
        diskListModel = storageDiskListModel;
        registerDiskListModel = storageRegisterDiskListModel;
        snapshotListModel = storageSnapshotListModel;
        this.diskProfileListModel = diskProfileListModel;

        setDetailList(storageEventListModel, permissionListModel);
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setApplicationPlace(WebAdminApplicationPlaces.storageMainTabPlace);

        setDefaultSearchString("Storage:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewDomainCommand(new UICommand("NewDomain", this)); //$NON-NLS-1$
        setImportDomainCommand(new UICommand("ImportDomain", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setDestroyCommand(new UICommand("Destroy", this)); //$NON-NLS-1$
        setScanDisksCommand(new UICommand("ScanDisks", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final StorageEventListModel storageEventListModel,
            final PermissionListModel<StorageDomain> permissionListModel) {
        generalModel.setIsAvailable(false);
        dcListModel.setIsAvailable(false);
        this.vmBackupModel.setIsAvailable(false);
        this.templateBackupModel.setIsAvailable(false);
        vmRegisterListModel.setIsAvailable(false);
        templateRegisterListModel.setIsAvailable(false);
        diskImageRegisterListModel.setIsAvailable(false);
        vmListModel.setIsAvailable(false);
        templateListModel.setIsAvailable(false);
        isoListModel.setIsAvailable(false);
        diskListModel.setIsAvailable(false);
        registerDiskListModel.setIsAvailable(false);
        snapshotListModel.setIsAvailable(false);
        this.diskProfileListModel.setIsAvailable(false);

        List<HasEntity<StorageDomain>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(dcListModel);
        list.add(vmBackupModel);
        list.add(templateBackupModel);
        list.add(vmRegisterListModel);
        list.add(templateRegisterListModel);
        list.add(diskImageRegisterListModel);
        list.add(vmListModel);
        list.add(templateListModel);
        list.add(isoListModel);
        list.add(diskListModel);
        list.add(registerDiskListModel);
        list.add(snapshotListModel);
        list.add(this.diskProfileListModel);
        list.add(storageEventListModel);
        list.add(permissionListModel);
        setDetailModels(list);
    }

    private final StorageGeneralModel generalModel;
    private final VmBackupModel vmBackupModel;
    private final TemplateBackupModel templateBackupModel;
    private final StorageDataCenterListModel dcListModel;
    private final StorageRegisterVmListModel vmRegisterListModel;
    private final StorageRegisterTemplateListModel templateRegisterListModel;
    private final StorageRegisterDiskImageListModel diskImageRegisterListModel;
    private final StorageVmListModel vmListModel;
    private final StorageTemplateListModel templateListModel;
    private final StorageIsoListModel isoListModel;
    private final StorageDiskListModel diskListModel;
    private final StorageRegisterDiskListModel registerDiskListModel;
    private final StorageSnapshotListModel snapshotListModel;
    private final DiskProfileListModel diskProfileListModel;

    public StorageDomainStatic storageDomain;
    public TaskContext context;
    public IStorageModel storageModel;
    public Guid storageId;
    public StorageServerConnections fileConnection;
    public StorageServerConnections connection;
    public Guid hostId = Guid.Empty;
    public String path;
    public StorageDomainType domainType = StorageDomainType.values()[0];
    public StorageType storageType;
    public boolean removeConnection;
    public List<StorageDomain> storageDomainsToAdd;

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("storage"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.StorageDomain, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    private void newDomain() {
        if (getWindow() != null) {
            return;
        }

        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDomainTitle());
        model.setHelpTag(HelpTag.new_domain);
        model.setHashName("new_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());

        // putting all Data domains at the beginning on purpose (so when choosing the
        // first selectable storage type/function, it will be a Data one, if relevant).

        List<IStorageModel> items = AsyncDataProvider.getInstance().getDataStorageModels();
        items.addAll(AsyncDataProvider.getInstance().getIsoStorageModels());

        items.addAll(AsyncDataProvider.getInstance().getExportStorageModels());

        model.setStorageModels(items);

        model.initialize();

        UICommand  command = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(command);

        command = createCancelCommand("Cancel"); //$NON-NLS-1$
        model.getCommands().add(command);
    }

    private void edit() {
        StorageDomain storage = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editDomainTitle());
        model.setHelpTag(HelpTag.edit_domain);
        model.setHashName("edit_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
        model.setStorage(storage);
        model.getName().setEntity(storage.getStorageName());
        model.getDescription().setEntity(storage.getDescription());
        model.getComment().setEntity(storage.getComment());
        model.setOriginalName(storage.getStorageName());

        model.getDataCenter().setIsChangeable(false);
        model.getFormat().setIsChangeable(false);

        boolean isStorageNameEditable = model.isStorageActive() || model.isNewStorage();
        boolean isStoragePropertiesEditable = model.isNewStorage();
        boolean isStorageInMaintenance = !model.isNewStorage() &&
                model.getStorage().getStatus() == StorageDomainStatus.Maintenance;
        model.getHost().setIsChangeable(false);
        model.getName().setIsChangeable(isStorageNameEditable);
        model.getDescription().setIsChangeable(isStoragePropertiesEditable);
        model.getComment().setIsChangeable(isStoragePropertiesEditable);
        model.getWipeAfterDelete().setIsChangeable(isStoragePropertiesEditable);
        //set the field domain type to non editable
        model.getAvailableStorageTypeItems().setIsChangeable(false);
        model.getAvailableStorageDomainTypeItems().setIsChangeable(false);
        model.setIsChangeable((isStorageNameEditable || isStoragePropertiesEditable) && !isStorageInMaintenance);

        model.getWarningLowSpaceIndicator().setEntity(storage.getWarningLowSpaceIndicator());
        model.getWarningLowSpaceSize().setEntity(
                ConstantsManager.getInstance().getMessages().bracketsWithGB(storage.getWarningLowSpaceSize()));
        model.getWarningLowSpaceSize().setIsAvailable(true);
        model.getCriticalSpaceActionBlocker().setEntity(storage.getCriticalSpaceActionBlocker());

        boolean isPathEditable = isPathEditable(storage);
        isStorageNameEditable = isStorageNameEditable || isPathEditable;

        IStorageModel item = prepareStorageForEdit(storage, model);

        model.setStorageModels(new ArrayList<>(Arrays.asList(new IStorageModel[]{item})));
        model.setCurrentStorageItem(item);

        model.initialize();

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage) {
            model.getName().setIsChangeable(false);
            model.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
        }


        UICommand command;
        if (isStorageNameEditable || isStoragePropertiesEditable) {
            command = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
            model.getCommands().add(command);

            command = createCancelCommand("Cancel"); //$NON-NLS-1$
            model.getCommands().add(command);
        }
        else {
            // close is created the same as cancel, but with a different title
            // thus most of creation code can be reused.
            command = createCancelCommand("Cancel"); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().close());
            model.getCommands().add(command);
        }
    }

    private IStorageModel prepareStorageForEdit(StorageDomain storage, StorageModel model) {
        final IStorageModel storageTypeModel = getStorageModelByStorage(storage);
        if (storageTypeModel != null) {
            storageTypeModel.setContainer(model);
            storageTypeModel.setRole(storage.getStorageDomainType());
            storageTypeModel.prepareForEdit(storage);
        }

        return storageTypeModel;
    }

    private IStorageModel getStorageModelByStorage(StorageDomain storage) {
        switch (storage.getStorageType()) {
        case NFS:
            return new NfsStorageModel();
        case FCP:
            return new FcpStorageModel();
        case ISCSI:
            return new IscsiStorageModel();
        case LOCALFS:
            return new LocalStorageModel();
        case POSIXFS:
            return new PosixStorageModel();
        case GLUSTERFS:
            return new GlusterStorageModel();
        }
        return null;
    }

    private boolean isPathEditable(StorageDomain storage) {
        if (storage.getStorageType().isFileDomain()) {
            StorageDomainType storageDomainType = storage.getStorageDomainType();
            return storageDomainType.isInternalDomain() && isStorageStatusValidForPathEditing(storage);
        }
        return false;
    }

    private boolean isStorageStatusValidForPathEditing(StorageDomain storage) {
        return storage.getStatus() == StorageDomainStatus.Maintenance
                || storage.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached;
    }

    private void importDomain() {
        if (getWindow() != null) {
            return;
        }

        StorageModel model = new StorageModel(new ImportStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().importPreConfiguredDomainTitle());
        model.setHelpTag(HelpTag.import_pre_configured_domain);
        model.setHashName("import_pre-configured_domain"); //$NON-NLS-1$
        model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
        model.getFormat().setIsAvailable(false);

        model.initialize();

        UICommand command;
        command = UICommand.createDefaultOkUiCommand("OnImport", this); //$NON-NLS-1$
        model.getCommands().add(command);

        command = createCancelCommand("Cancel"); //$NON-NLS-1$
        model.getCommands().add(command);
    }

    private void onImport() {
        StorageModel model = (StorageModel) getWindow();
        if (model.getProgress() != null) {
            return;
        }
        if (!model.validate()) {
            return;
        }
        model.startProgress(ConstantsManager.getInstance().getConstants().importingStorageDomainProgress());
        VDS host = model.getHost().getSelectedItem();

        // Save changes.
        if (model.getCurrentStorageItem() instanceof NfsStorageModel) {
            NfsStorageModel nfsModel = (NfsStorageModel) model.getCurrentStorageItem();
            nfsModel.setMessage(null);

            Task.create(this,
                    new ArrayList<>(Arrays.asList(new Object[]{"ImportFile", //$NON-NLS-1$
                            host.getId(), nfsModel.getPath().getEntity(), nfsModel.getRole(), StorageType.NFS,
                            model.getActivateDomain().getEntity()}))).run();
        } else if (model.getCurrentStorageItem() instanceof LocalStorageModel) {
            LocalStorageModel localModel = (LocalStorageModel) model.getCurrentStorageItem();
            localModel.setMessage(null);

            Task.create(this,
                    new ArrayList<>(Arrays.asList(new Object[]{"ImportFile", //$NON-NLS-1$
                            host.getId(), localModel.getPath().getEntity(), localModel.getRole(), StorageType.LOCALFS,
                            model.getActivateDomain().getEntity()}))).run();
        } else if (model.getCurrentStorageItem() instanceof PosixStorageModel) {
            PosixStorageModel posixModel = (PosixStorageModel) model.getCurrentStorageItem();
            posixModel.setMessage(null);

            Task.create(this,
                    new ArrayList<>(Arrays.asList(new Object[]{"ImportFile", //$NON-NLS-1$
                            host.getId(), posixModel.getPath().getEntity(), posixModel.getRole(), posixModel.getType(),
                            model.getActivateDomain().getEntity()}))).run();
        } else if (model.getCurrentStorageItem() instanceof ImportSanStorageModel) {
            Task.create(this,
                    new ArrayList<>(Arrays.asList(new Object[]{"ImportSan", //$NON-NLS-1$
                            host.getId(), model.getActivateDomain().getEntity()}))).run();
        }
    }

    public void storageNameValidation() {
        StorageModel model = (StorageModel) getWindow();
        String name = model.getName().getEntity();
        model.getName().setIsValid(true);

        AsyncDataProvider.getInstance().isStorageDomainNameUnique(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                StorageListModel storageListModel = (StorageListModel) target;
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();

                String name1 = storageModel.getName().getEntity();
                String tempVar = storageModel.getOriginalName();
                String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$
                boolean isNameUnique = (Boolean) returnValue;

                if (!isNameUnique && name1.compareToIgnoreCase(originalName) != 0) {
                    storageModel.getName()
                        .getInvalidityReasons()
                        .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                    storageModel.getName().setIsValid(false);
                    storageListModel.postStorageNameValidation();
                } else {

                    AsyncDataProvider.getInstance().getStorageDomainMaxNameLength(new AsyncQuery(storageListModel, new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target1, Object returnValue1) {

                            StorageListModel storageListModel1 = (StorageListModel) target1;
                            StorageModel storageModel1 = (StorageModel) storageListModel1.getWindow();
                            int nameMaxLength = (Integer) returnValue1;
                            RegexValidation tempVar2 = new RegexValidation();
                            tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                            tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                                        .nameCanContainOnlyMsg(nameMaxLength));
                            storageModel1.getName().validateEntity(new IValidation[] {
                                    new NotEmptyValidation(), tempVar2});
                            storageListModel1.postStorageNameValidation();

                        }
                    }));
                }

            }
        }),
            name);
    }

    public void postStorageNameValidation() {
        if (getLastExecutedCommand().getName().equals("OnSave")) { //$NON-NLS-1$
            onSavePostNameValidation();
        }
    }

    private void cleanConnection(StorageServerConnections connection, Guid hostId) {
        // if create connection command was the one to fail and didn't create a connection
        // then the id of connection will be empty, and there's nothing to delete.
        if (connection.getId() != null && !connection.getId().equals("")) {  //$NON-NLS-1$
            Frontend.getInstance().runAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, hostId),
                null, this);
        }

    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveStorageModel model = new RemoveStorageModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeStoragesTitle());
        model.setHelpTag(HelpTag.remove_storage);
        model.setHashName("remove_storage"); //$NON-NLS-1$

        StorageDomain storage = getSelectedItem();
        boolean localFsOnly = storage.getStorageType() == StorageType.LOCALFS;

        AsyncDataProvider.getInstance().getHostsForStorageOperation(new AsyncQuery(new Object[]{this, model}, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                Object[] array = (Object[]) target;
                RemoveStorageModel removeStorageModel = (RemoveStorageModel) array[1];
                List<VDS> hosts = (List<VDS>) returnValue;
                removeStorageModel.getHostList().setItems(hosts);
                removeStorageModel.getHostList().setSelectedItem(Linq.firstOrNull(hosts));

                if (hosts.isEmpty()) {
                    UICommand tempVar = createCancelCommand("Cancel"); //$NON-NLS-1$
                    tempVar.setIsDefault(true);
                    removeStorageModel.getCommands().add(tempVar);
                } else {

                    UICommand command;
                    command = UICommand.createDefaultOkUiCommand("OnRemove", StorageListModel.this); //$NON-NLS-1$
                    removeStorageModel.getCommands().add(command);

                    command = createCancelCommand("Cancel"); //$NON-NLS-1$
                    removeStorageModel.getCommands().add(command);
                }

            }
        }), null, localFsOnly);
    }

    private void onRemove() {
        if (getSelectedItem() != null) {
            StorageDomain storage = getSelectedItem();
            RemoveStorageModel model = (RemoveStorageModel) getWindow();

            if (!model.validate()) {
                return;
            }

            VDS host = model.getHostList().getSelectedItem();

            RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(storage.getId());
            tempVar.setVdsId(host.getId());
            tempVar.setDoFormat(model.getFormat().getEntity());

            Frontend.getInstance().runAction(VdcActionType.RemoveStorageDomain, tempVar, null, this);
        }

        cancel();
    }

    private void destroy() {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().destroyStorageDomainTitle());
        model.setHelpTag(HelpTag.destroy_storage_domain);
        model.setHashName("destroy_storage_domain"); //$NON-NLS-1$
        ArrayList<String> items = new ArrayList<>();
        items.add(getSelectedItem().getStorageName());
        model.setItems(items);

        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangeable(true);


        UICommand command;
        command = UICommand.createDefaultOkUiCommand("OnDestroy", this); //$NON-NLS-1$
        model.getCommands().add(command);

        command = createCancelCommand("Cancel"); //$NON-NLS-1$
        model.getCommands().add(command);
    }

    private void onDestroy() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        StorageDomain storageDomain = getSelectedItem();

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.ForceRemoveStorageDomain,
                new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{new StorageDomainParametersBase(storageDomain.getId())})),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                },
                model);
    }

    private void scanDisks() {
        StorageDomain storageDomain = getSelectedItem();
        if (storageDomain != null) {
            Frontend.getInstance().runAction(VdcActionType.ScanStorageForUnregisteredDisks,
                    new StorageDomainParametersBase(storageDomain.getStoragePoolId(), storageDomain.getId()));
        }
    }

    private void onSave() {
        storageNameValidation();
    }

    private void onSavePostNameValidation() {
        StorageModel model = (StorageModel) getWindow();

        if (!model.validate()) {
            return;
        }

        if (model.getCurrentStorageItem() instanceof NfsStorageModel) {
            saveNfsStorage();
        }
        else if (model.getCurrentStorageItem() instanceof LocalStorageModel) {
            saveLocalStorage();
        }
        else if (model.getCurrentStorageItem() instanceof PosixStorageModel) {
            savePosixStorage();
        }
        else {
            saveSanStorage();
        }
    }

    private void saveLocalStorage() {
        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveLocal"}))).run(); //$NON-NLS-1$
    }

    private void saveNfsStorage() {
        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveNfs"}))).run(); //$NON-NLS-1$
    }

    private void savePosixStorage() {

        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SavePosix"}))).run(); //$NON-NLS-1$
    }

    private void saveSanStorage() {
        StorageModel storageModel = (StorageModel) getWindow();
        final SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getCurrentStorageItem();

        Guid hostId = sanStorageModel.getContainer().getHost().getSelectedItem().getId();
        Model target = getWidgetModel() != null ? getWidgetModel() : sanStorageModel.getContainer();
        if (sanStorageModel.getAddedLuns().isEmpty()) {
            onSaveSanStorage();
            return;
        }
        List<String> unkownStatusLuns = new ArrayList<>();
        for (LunModel lunModel : sanStorageModel.getAddedLuns()) {
            unkownStatusLuns.add(lunModel.getLunId());
        }
        Frontend.getInstance()
                .runQuery(VdcQueryType.GetDeviceList,
                        new GetDeviceListQueryParameters(hostId,
                                sanStorageModel.getType(),
                                true,
                                unkownStatusLuns),
                        new AsyncQuery(target, new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                                if (response.getSucceeded()) {
                                    List<LUNs> checkedLuns = (ArrayList<LUNs>) response.getReturnValue();
                                    postGetLunsMessages(sanStorageModel.getUsedLunsMessages(checkedLuns));
                                } else {
                                    sanStorageModel.setGetLUNsFailure(
                                            ConstantsManager.getInstance()
                                                    .getConstants()
                                                    .couldNotRetrieveLUNsLunsFailure());
                                }
                            }
                        }, true));
    }

    private void postGetLunsMessages(ArrayList<String> usedLunsMessages) {

        if (usedLunsMessages.isEmpty()) {
            onSaveSanStorage();
        }
        else {
            forceCreationWarning(usedLunsMessages);
        }
    }

    private void onSaveSanStorage() {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (confirmationModel != null && !confirmationModel.validate()) {
            return;
        }

        cancelConfirm();
        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveSan"}))).run(); //$NON-NLS-1$
    }

    private void forceCreationWarning(ArrayList<String> usedLunsMessages) {
        StorageModel storageModel = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getCurrentStorageItem();
        sanStorageModel.setForce(true);

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().forceStorageDomainCreation());
        model.setMessage(ConstantsManager.getInstance().getConstants().lunsAlreadyInUse());
        model.setHelpTag(HelpTag.force_storage_domain_creation);
        model.setHashName("force_storage_domain_creation"); //$NON-NLS-1$
        model.setItems(usedLunsMessages);

        UICommand command = UICommand.createDefaultOkUiCommand("OnSaveSanStorage", this); //$NON-NLS-1$
        model.getCommands().add(command);

        command = createCancelCommand("CancelConfirm"); //$NON-NLS-1$
        model.getCommands().add(command);
    }

    private void cancelConfirm() {
        setConfirmWindow(null);
    }

    private void cancelImportConfirm() {
        cancelConfirm();
        getWindow().stopProgress();

        if (fileConnection != null) {
            Frontend.getInstance().runAction(VdcActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(fileConnection, hostId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        cleanConnection(storageListModel.fileConnection, storageListModel.hostId);
                        storageListModel.fileConnection = null;
                    }
                },
                this);
        }
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e) {
        super.itemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage) {
            StorageDomain storage = (StorageDomain) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.firstOrNull(Linq.<StorageDomain> cast(getItems()),
                    new Linq.IdPredicate<>(storage.getId())));
        }
    }

    @Override
    protected void updateDetailsAvailability() {
        if (getSelectedItem() != null) {
            StorageDomain storage = getSelectedItem();
            boolean isBackupStorage = storage.getStorageDomainType() == StorageDomainType.ImportExport;
            boolean isDataStorage =
                    storage.getStorageDomainType().isDataDomain();
            boolean isImageStorage =
                     storage.getStorageDomainType() == StorageDomainType.Image ||
                     storage.getStorageDomainType() == StorageDomainType.ISO;
            boolean isDataCenterAvailable = storage.getStorageType() != StorageType.GLANCE;
            boolean isGeneralAvailable = storage.getStorageType() != StorageType.GLANCE;
            boolean isCinderStorage = storage.getStorageType().isCinderDomain();

            boolean isRegsiterEntityListModelSelected =
                    getActiveDetailModel() == vmRegisterListModel
                            || getActiveDetailModel() == templateRegisterListModel
                            || getActiveDetailModel() == diskImageRegisterListModel;
            boolean isRegisterSubtabsAvailable = isDataStorage && storage.getStatus() != StorageDomainStatus.Unattached &&
                    (storage.isContainsUnregisteredEntities() || isRegsiterEntityListModelSelected);

            generalModel.setIsAvailable(isGeneralAvailable);
            dcListModel.setIsAvailable(isDataCenterAvailable);

            vmBackupModel.setIsAvailable(isBackupStorage);
            templateBackupModel.setIsAvailable(isBackupStorage);

            vmListModel.setIsAvailable(isDataStorage);
            templateListModel.setIsAvailable(isDataStorage);
            vmRegisterListModel.setIsAvailable(isRegisterSubtabsAvailable);
            templateRegisterListModel.setIsAvailable(isRegisterSubtabsAvailable);
            diskImageRegisterListModel.setIsAvailable(isRegisterSubtabsAvailable);
            diskListModel.setIsAvailable(isDataStorage || isCinderStorage);
            registerDiskListModel.setIsAvailable(isCinderStorage);
            snapshotListModel.setIsAvailable(isDataStorage || isCinderStorage);
            diskProfileListModel.setIsAvailable(isDataStorage);

            isoListModel.setIsAvailable(isImageStorage);
        }
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        ArrayList<StorageDomain> items =
                getSelectedItems() != null ? Linq.<StorageDomain> cast(getSelectedItems())
                        : new ArrayList<StorageDomain>();

        StorageDomain item = getSelectedItem();

        getNewDomainCommand().setIsAvailable(true);

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && isEditAvailable(item));

        getRemoveCommand().setIsExecutionAllowed(items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && Linq.findAllStorageDomainsBySharedStatus(items, StorageDomainSharedStatus.Unattached).size() == items.size());

        getDestroyCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && item.getStatus() != StorageDomainStatus.Active);

        getScanDisksCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && item.getStatus() == StorageDomainStatus.Active
                && item.getStorageDomainType().isDataDomain());

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage);

        getNewDomainCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
        getDestroyCommand().setIsAvailable(isAvailable);
        getScanDisksCommand().setIsAvailable(isAvailable);
    }

    private boolean isEditAvailable(StorageDomain storageDomain) {
        if (storageDomain == null || storageDomain.getStorageType().isCinderDomain()) {
            return false;
        }

        boolean isEditAvailable;
        boolean isActive = storageDomain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active
                || storageDomain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Mixed;
        boolean isInMaintenance = storageDomain.getStatus() == StorageDomainStatus.Maintenance
                || storageDomain.getStatus() == StorageDomainStatus.PreparingForMaintenance;
        boolean isUnattached = storageDomain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached
                || storageDomain.getStatus() == StorageDomainStatus.Detaching;
        boolean isInternalDomain = storageDomain.getStorageDomainType().isInternalDomain();
        boolean isBlockStorage = storageDomain.getStorageType().isBlockDomain();

        isEditAvailable = isActive || isBlockStorage || ((isInMaintenance || isUnattached) && isInternalDomain);
        return isEditAvailable;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewDomainCommand()) {
            newDomain();
        }
        else if (command == getImportDomainCommand()) {
            importDomain();
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getDestroyCommand()) {
            destroy();
        }
        else if (command == getScanDisksCommand()) {
            scanDisks();
        }
        else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        }
        else if ("CancelImportConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelImportConfirm();
        }
        else if ("OnImport".equals(command.getName())) { //$NON-NLS-1$
            onImport();
        }
        else if ("OnImportFile".equals(command.getName())) { //$NON-NLS-1$
            if (getConfirmWindow() != null && !((ConfirmationModel) getConfirmWindow()).validate()) {
                return;
            }
            cancelConfirm();
            getExistingStorageDomainList();
        }
        else if ("OnImportSan".equals(command.getName())) { //$NON-NLS-1$
            if (getConfirmWindow() != null && !((ConfirmationModel) getConfirmWindow()).validate()) {
                return;
            }
            cancelConfirm();
            onImportSanDomainApprove();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("OnDestroy".equals(command.getName())) { //$NON-NLS-1$
            onDestroy();
        }
        else if ("OnSaveSanStorage".equals(command.getName())) { //$NON-NLS-1$
            onSaveSanStorage();
        }
    }

    private void saveBaseStorageProperties(StorageModel model) {
        boolean isNew = model.getStorage() == null;
        storageDomain.setStorageType(isNew ? storageModel.getType() : storageDomain.getStorageType());
        storageDomain.setStorageDomainType(isNew ? storageModel.getRole() : storageDomain.getStorageDomainType());
        storageDomain.setDescription(model.getDescription().getEntity());
        storageDomain.setComment(model.getComment().getEntity());
        saveCommonStorageProperties(model);
    }

    private void saveCommonStorageProperties(StorageModel model) {
        storageDomain.setStorageName(model.getName().getEntity());
        saveDefaultedStorageProperties(model, storageDomain);
    }

    private void saveDefaultedStorageProperties(StorageModel model, StorageDomainStatic storageDomainStatic) {
        storageDomainStatic.setWipeAfterDelete(model.getWipeAfterDelete().getEntity());
        storageDomainStatic.setWarningLowSpaceIndicator(model.getWarningLowSpaceIndicator().getEntity());
        storageDomainStatic.setCriticalSpaceActionBlocker(model.getCriticalSpaceActionBlocker().getEntity());
    }

    private void savePosixStorage(TaskContext context) {

        this.context = context;

        StorageDomain selectedItem = getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        PosixStorageModel posixModel = (PosixStorageModel) storageModel;
        path = posixModel.getPath().getEntity();

        storageDomain = isNew ? new StorageDomainStatic() : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());
        saveBaseStorageProperties(model);
        storageDomain.setStorageFormat(model.getFormat().getSelectedItem());

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<StorageDomain> storages = (ArrayList<StorageDomain>) returnValue;

                    if (storages != null && storages.size() > 0) {
                        handleDomainAlreadyExists(storageListModel, storages);
                    } else {
                        storageListModel.saveNewPosixStorage();
                    }
                }
            }), null, path);
        } else {
            StorageDomain storageDomain = getSelectedItem();
            if (isPathEditable(storageDomain)) {
                updatePath();
            }
            updateStorageDomain();
        }
    }

    private void updateStorageDomain() {
        Frontend.getInstance().runAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(this.storageDomain), new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
            }
        }, this);
    }

    public void saveNewPosixStorage() {

        StorageModel model = (StorageModel) getWindow();
        PosixStorageModel posixModel = (PosixStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections connection = new StorageServerConnections();
        connection.setConnection(path);
        connection.setStorageType(posixModel.getType());
        connection.setVfsType(posixModel.getVfsType().getEntity());
        connection.setMountOptions(posixModel.getMountOptions().getEntity());
        this.connection = connection;

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(posixModel.getAddStorageDomainVdcAction());

        parameters.add(new StorageServerConnectionParametersBase(this.connection, host.getId()));
        StorageDomainManagementParameter parameter = new StorageDomainManagementParameter(storageDomain);
        parameter.setVdsId(host.getId());
        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
        parameter.setStoragePoolId(dataCenter.getId());
        parameters.add(parameter);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());
                storageListModel.connection.setId((String) vdcReturnValueBase.getActionReturnValue());

            }
        };

        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageId = vdcReturnValueBase.getActionReturnValue();

                // Attach storage to data center as necessary.
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                    storageListModel.attachStorageToDataCenter(storageListModel.storageId, dataCenter.getId(), storageModel.getActivateDomain().getEntity());
                }

                storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
            }
        };

        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
                storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);
            }
        };

        Frontend.getInstance().runMultipleActions(actionTypes,
            parameters,
            new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1, callback2})),
            failureCallback,
            this);
    }

    private void saveNfsStorage(TaskContext context) {
        this.context = context;

        StorageDomain selectedItem = getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = nfsModel.getPath().getEntity();

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());

        saveBaseStorageProperties(model);
        storageDomain.setStorageFormat(model.getFormat().getSelectedItem());

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<StorageDomain> storages = (ArrayList<StorageDomain>) returnValue;
                    if (storages != null && storages.size() > 0) {
                        handleDomainAlreadyExists(storageListModel, storages);
                    } else {
                        storageListModel.saveNewNfsStorage();
                    }
                }
            }), null, path);
        }
        else {
            StorageDomain storageDomain = getSelectedItem();
            if (isPathEditable(storageDomain)) {
                updatePath();
            }
            updateStorageDomain();
        }
    }


    private void updatePath() {
        StorageModel model = (StorageModel) getWindow();
        VDS host = model.getHost().getSelectedItem();

        Guid hostId = Guid.Empty;
        if (host != null) {
            hostId = host.getId();
        }
        IStorageModel storageModel = model.getCurrentStorageItem();
        connection = new StorageServerConnections();
        connection.setId(storageDomain.getStorage());
        connection.setConnection(path);
        connection.setStorageType(storageModel.getType());

        if (storageModel.getType().equals(StorageType.NFS)) {
            updateNFSProperties(storageModel);
        }
        else if (storageModel instanceof PosixStorageModel) {
            updatePosixProperties(storageModel);
        }

        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId);
        Frontend.getInstance().runAction(VdcActionType.UpdateStorageServerConnection, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

                    }
                }, this);
    }

    private void updateNFSProperties(IStorageModel storageModel) {
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        if (isConnectionOverriden()) {
            connection.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
            connection.setNfsRetrans(nfsModel.getRetransmissions().asConvertible().nullableShort());
            connection.setNfsTimeo(nfsModel.getTimeout().asConvertible().nullableShort());
            connection.setMountOptions(nfsModel.getMountOptions().getEntity());
        }

    }

    private void updatePosixProperties(IStorageModel storageModel) {
        PosixStorageModel posixModel = (PosixStorageModel) storageModel;
        connection.setVfsType(posixModel.getVfsType().getEntity().toString());
        if (posixModel.getMountOptions().getEntity() != null) {
            connection.setMountOptions(posixModel.getMountOptions().getEntity().toString());
        }

    }

    public void saveNewNfsStorage() {
        StorageModel model = (StorageModel) getWindow();
        NfsStorageModel nfsModel = (NfsStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections storageConnection = new StorageServerConnections();
        storageConnection.setConnection(path);
        storageConnection.setStorageType(nfsModel.getType());
        if (isConnectionOverriden()) {
            storageConnection.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
            storageConnection.setNfsRetrans(nfsModel.getRetransmissions().asConvertible().nullableShort());
            storageConnection.setNfsTimeo(nfsModel.getTimeout().asConvertible().nullableShort());
            storageConnection.setMountOptions(nfsModel.getMountOptions().getEntity());
        }
        connection = storageConnection;

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddNFSStorageDomain);
        actionTypes.add(VdcActionType.DisconnectStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
        tempVar2.setStoragePoolId(dataCenter.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());
                storageListModel.connection.setId((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageId = vdcReturnValueBase.getActionReturnValue();

            }
        };
        IFrontendActionAsyncCallback callback3 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                StorageModel storageModel = (StorageModel) storageListModel.getWindow();

                // Attach storage to data center as necessary.
                StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                    storageListModel.attachStorageToDataCenter(storageListModel.storageId, dataCenter.getId(), storageModel.getActivateDomain().getEntity());
                }

                storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
                storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);

            }
        };
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(callback1, callback2, callback3)),
                failureCallback,
                this);
    }

    public void saveNewSanStorage() {
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        boolean force = sanModel.isForce();

        ArrayList<String> lunIds = new ArrayList<>();
        for (LunModel lun : sanModel.getAddedLuns()) {
            lunIds.add(lun.getLunId());
        }

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storageDomain);
        params.setVdsId(host.getId());
        params.setLunIds(lunIds);
        params.setForce(force);
        Frontend.getInstance().runAction(VdcActionType.AddSANStorageDomain, params,
            new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                        storageListModel.storageModel = storageModel.getCurrentStorageItem();
                        if (!result.getReturnValue().getSucceeded()) {
                            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);
                            return;
                        }

                        StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                            VdcReturnValueBase returnValue = result.getReturnValue();
                            Guid storageId = returnValue.getActionReturnValue();
                            storageListModel.attachStorageToDataCenter(storageId, dataCenter.getId(), storageModel.getActivateDomain().getEntity());
                        }

                    storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
                }
            }, this);
    }

    private void saveLocalStorage(TaskContext context) {
        this.context = context;

        StorageDomain selectedItem = getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        VDS host = model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = localModel.getPath().getEntity();

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());

        saveBaseStorageProperties(model);

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {

                    StorageListModel storageListModel = (StorageListModel) target;
                    ArrayList<StorageDomain> storages = (ArrayList<StorageDomain>) returnValue;
                    if (storages != null && storages.size() > 0) {
                        handleDomainAlreadyExists(storageListModel, storages);
                    } else {
                        storageListModel.saveNewLocalStorage();
                    }

                }
            }), host.getStoragePoolId(), path);
        }
        else {
            StorageDomain storageDomain = getSelectedItem();
            if (isPathEditable(storageDomain)) {
                updatePath();
            }
            updateStorageDomain();
        }
    }

    private void handleDomainAlreadyExists(StorageListModel storageListModel, ArrayList<StorageDomain> storages) {
        String storageName = storages.get(0).getStorageName();

        onFinish(storageListModel.context,
            false,
            storageListModel.storageModel,
            ConstantsManager.getInstance().getMessages().createFailedDomainAlreadyExistStorageMsg(storageName));
    }

    public void saveNewLocalStorage() {
        StorageModel model = (StorageModel) getWindow();
        LocalStorageModel localModel = (LocalStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setConnection(path);
        tempVar.setStorageType(localModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddLocalStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.removeConnection = true;

                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                storageListModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());
                storageListModel.connection.setId((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();
                storageListModel.removeConnection = false;

                storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                StorageListModel storageListModel = (StorageListModel) result.getState();

                if (storageListModel.removeConnection) {
                    storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
                    storageListModel.removeConnection = false;
                }

                storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);

            }
        };
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1, callback2})),
                failureCallback,
                this);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model) {
        onFinish(context, isSucceeded, model, null);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model, String message) {
        context.invokeUIThread(this,
                new ArrayList<>(Arrays.asList(new Object[]{"Finish", isSucceeded, model, message}))); //$NON-NLS-1$
    }

    private void saveSanStorage(TaskContext context) {
        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getCurrentStorageItem();
        StorageDomain storage = getSelectedItem();

        boolean isNew = model.getStorage() == null;

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(storage.getStorageStaticData());

        storageDomain.setStorageType(isNew ? sanModel.getType() : storageDomain.getStorageType());

        storageDomain.setStorageDomainType(isNew ? sanModel.getRole() : storageDomain.getStorageDomainType());

        storageDomain.setStorageFormat(isNew ? sanModel.getContainer()
                .getFormat()
                .getSelectedItem() : storageDomain.getStorageFormat());

        storageDomain.setDescription(model.getDescription().getEntity());
        storageDomain.setComment(model.getComment().getEntity());
        saveCommonStorageProperties(model);

        if (isNew) {
            saveNewSanStorage();
        }
        else {
            Frontend.getInstance().runAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain), new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    StorageModel storageModel = (StorageModel) getWindow();
                    SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getCurrentStorageItem();
                    boolean force = sanStorageModel.isForce();
                    StorageDomain storageDomain1 = storageListModel.getSelectedItem();
                    ArrayList<String> lunIds = new ArrayList<>();

                    for (LunModel lun : sanStorageModel.getAddedLuns()) {
                        lunIds.add(lun.getLunId());
                    }

                    if (lunIds.size() > 0) {
                        Frontend.getInstance().runAction(VdcActionType.ExtendSANStorageDomain,
                            new ExtendSANStorageDomainParameters(storageDomain1.getId(), lunIds, force),
                            null, this);
                    }

                    ArrayList<String> lunToRefreshIds = new ArrayList<>();
                    for (LunModel lun : sanStorageModel.getLunsToRefresh()) {
                        lunToRefreshIds.add(lun.getLunId());
                    }

                    if (lunToRefreshIds.size() > 0) {
                        Frontend.getInstance().runAction(VdcActionType.RefreshLunsSize,
                                new ExtendSANStorageDomainParameters(storageDomain1.getId(), lunToRefreshIds, false),
                                null, this);
                    }

                    storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
                }
            }, this);
        }
    }

    private void attachStorageToDataCenter(Guid storageId, Guid dataCenterId, Boolean activateDomain) {
        AttachStorageDomainToPoolParameters params = new AttachStorageDomainToPoolParameters(storageId, dataCenterId);
        if (activateDomain != null) {
            params.setActivate(activateDomain);
        }
        Frontend.getInstance().runAction(VdcActionType.AttachStorageDomainToPool, params, null, this);
    }

    private void importFileStorage(TaskContext context) {
        this.context = context;

        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        StorageModel model = (StorageModel) getWindow();

        storageModel = model.getCurrentStorageItem();
        hostId = (Guid) data.get(1);
        path = (String) data.get(2);
        domainType = (StorageDomainType) data.get(3);
        storageType = (StorageType) data.get(4);

        importFileStorageInit();
    }

    private void importSanStorage(final TaskContext context) {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        storageModel = model.getCurrentStorageItem();
        ImportSanStorageModel importSanStorageModel = (ImportSanStorageModel) storageModel;
        checkSanDomainAttachedToDc("OnImportSan", importSanStorageModel.getStorageDomains().getSelectedItems()); //$NON-NLS-1$
    }

    private void onImportSanDomainApprove() {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        hostId = (Guid) data.get(1);

        ImportSanStorageModel importSanStorageModel = (ImportSanStorageModel) storageModel;
        final List<StorageDomain> storageDomains = importSanStorageModel.getStorageDomains().getSelectedItems();

        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<>(items.size());
        List<IFrontendActionAsyncCallback> callbacks = new LinkedList<>();

        for (final StorageDomain storageDomain : storageDomains) {
            StorageDomainStatic staticData = storageDomain.getStorageStaticData();
            saveDefaultedStorageProperties((StorageModel) getWindow(), staticData);
            StorageDomainManagementParameter parameters =
                    new StorageDomainManagementParameter(staticData);
            parameters.setVdsId(hostId);
            parametersList.add(parameters);

            callbacks.add(new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {
                    VdcReturnValueBase returnValue = result.getReturnValue();
                    boolean success = returnValue != null && returnValue.getSucceeded();

                    if (success) {
                        StorageModel model = (StorageModel) getWindow();
                        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                            attachStorageToDataCenter(storageDomain.getId(), dataCenter.getId(), model.getActivateDomain().getEntity());
                        }

                        boolean isLastDomain = storageDomain == storageDomains.get(storageDomains.size() - 1);
                        if (isLastDomain) {
                            onFinish(context, true, storageModel);
                        }
                    }
                    else {
                        onFinish(context, false, storageModel);
                    }
                }
            });
        }

        Frontend.getInstance().runMultipleActions(VdcActionType.AddExistingBlockStorageDomain, parametersList, callbacks);
    }

    public void importFileStorageInit() {
        if (fileConnection != null) {
            // Clean nfs connection
            Frontend.getInstance().runAction(VdcActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(fileConnection, hostId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success) {
                            storageListModel.fileConnection = null;
                        }
                        storageListModel.importFileStoragePostInit();

                    }
                },
                this);
        }
        else {
            importFileStoragePostInit();
        }
    }

    public void importFileStoragePostInit() {
        Guid storagePoolId = null;
        StoragePool dataCenter = storageModel.getContainer().getDataCenter().getSelectedItem();
        if (dataCenter != null && !dataCenter.getId().equals(Guid.Empty)) {
            storagePoolId = dataCenter.getId();
        }

        // Check storage domain existence
        AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                StorageListModel storageListModel = (StorageListModel) target;
                ArrayList<StorageDomain> storages = (ArrayList<StorageDomain>) returnValue;

                if (storages != null && storages.size() > 0) {

                    String storageName = storages.get(0).getStorageName();
                    onFinish(storageListModel.context,
                        false,
                        storageListModel.storageModel,
                        ConstantsManager.getInstance().getMessages().importFailedDomainAlreadyExistStorageMsg(storageName));
                } else {
                    StorageServerConnections tempVar = new StorageServerConnections();
                    storageModel = storageListModel.storageModel;
                    tempVar.setConnection(storageListModel.path);
                    tempVar.setStorageType(storageListModel.storageType);
                    if (storageModel instanceof NfsStorageModel) {
                        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
                        if (isConnectionOverriden()) {
                            tempVar.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
                            tempVar.setNfsRetrans(nfsModel.getRetransmissions().asConvertible().nullableShort());
                            tempVar.setNfsTimeo(nfsModel.getTimeout().asConvertible().nullableShort());
                        }
                    }
                    if (storageModel instanceof PosixStorageModel) {
                        PosixStorageModel posixModel = (PosixStorageModel) storageModel;
                        tempVar.setVfsType(posixModel.getVfsType().getEntity());
                        tempVar.setMountOptions(posixModel.getMountOptions().getEntity());
                    }
                    storageListModel.fileConnection = tempVar;
                    importFileStorageConnect();
                }
            }
        }), storagePoolId, path);
    }

    public void importFileStorageConnect() {
        Frontend.getInstance().runAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(fileConnection, hostId),
            new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {
                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    VdcReturnValueBase returnVal = result.getReturnValue();
                    boolean success = returnVal != null && returnVal.getSucceeded();
                    if (success) {
                        storageListModel.fileConnection.setId((String) returnVal.getActionReturnValue());
                        if (storageModel.getRole() == StorageDomainType.Data) {
                            checkFileDomainAttachedToDc("OnImportFile", storageListModel.fileConnection); //$NON-NLS-1$
                        } else {
                            getExistingStorageDomainList();
                        }
                    }
                    else {
                        postImportFileStorage(storageListModel.context,
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

    private void getExistingStorageDomainList() {
        AsyncDataProvider.getInstance().getExistingStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        StorageListModel storageListModel = (StorageListModel) target;
                        ArrayList<StorageDomain> domains = (ArrayList<StorageDomain>) returnValue;
                        if (domains != null && !domains.isEmpty()) {
                            storageListModel.storageDomainsToAdd = domains;
                            addExistingFileStorageDomain();
                        }
                        else {
                            String errorMessage = domains == null ?
                                    ConstantsManager.getInstance().getConstants()
                                            .failedToRetrieveExistingStorageDomainInformationMsg() :
                                    ConstantsManager.getInstance().getConstants()
                                            .thereIsNoStorageDomainUnderTheSpecifiedPathMsg();

                            postImportFileStorage(storageListModel.context,
                                    false,
                                    storageListModel.storageModel,
                                    errorMessage);

                            storageListModel.cleanConnection(storageListModel.fileConnection, storageListModel.hostId);
                        }
                    }
                }),
        hostId,
        domainType,
        storageType,
        path);
    }

    public void addExistingFileStorageDomain() {
        StorageDomain sdToAdd = Linq.firstOrNull(storageDomainsToAdd);
        StorageDomainStatic sdsToAdd = sdToAdd.getStorageStaticData();
        storageDomain = sdsToAdd;
        saveBaseStorageProperties((StorageModel) getWindow());

        StorageDomainManagementParameter params = new StorageDomainManagementParameter(sdsToAdd);
        params.setVdsId(hostId);
        Frontend.getInstance().runAction(VdcActionType.AddExistingFileStorageDomain, params, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                Object[] array = (Object[]) result.getState();
                StorageListModel storageListModel = (StorageListModel) array[0];
                StorageDomain sdToAdd1 = (StorageDomain) array[1];
                VdcReturnValueBase returnVal = result.getReturnValue();

                boolean success = returnVal != null && returnVal.getSucceeded();
                if (success) {

                    StorageModel model = (StorageModel) storageListModel.getWindow();
                    StoragePool dataCenter = model.getDataCenter().getSelectedItem();
                    if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                        storageListModel.attachStorageToDataCenter(sdToAdd1.getId(), dataCenter.getId(), model.getActivateDomain().getEntity());
                        onFinish(storageListModel.context, true, storageListModel.storageModel, null);
                    } else {
                        postImportFileStorage(storageListModel.context, true, storageListModel.storageModel, null);
                    }

                } else {
                    postImportFileStorage(storageListModel.context, false, storageListModel.storageModel, ""); //$NON-NLS-1$
                    cleanConnection(fileConnection, hostId);
                }
            }
        }, new Object[] {this, sdToAdd});
    }

    public void postImportFileStorage(TaskContext context, boolean isSucceeded, IStorageModel model, String message) {
        Frontend.getInstance().runAction(VdcActionType.DisconnectStorageServerConnection,
            new StorageServerConnectionParametersBase(fileConnection, hostId),
            new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {

                    VdcReturnValueBase returnValue = result.getReturnValue();
                    boolean success = returnValue != null && returnValue.getSucceeded();
                    if (success) {
                        fileConnection = null;
                    }
                    Object[] array = (Object[]) result.getState();
                    onFinish((TaskContext) array[0],
                        (Boolean) array[1],
                        (IStorageModel) array[2],
                        (String) array[3]);

                }
            },
            new Object[] {context, isSucceeded, model, message});
    }

    private void checkSanDomainAttachedToDc(String commandName, List<StorageDomain> storageDomains) {
        checkDomainAttachedToDc(commandName, storageDomains, null);
    }

    private void checkFileDomainAttachedToDc(String commandName, StorageServerConnections storageServerConnections) {
        checkDomainAttachedToDc(commandName, null, storageServerConnections);
    }

    private void checkDomainAttachedToDc(String commandName, List<StorageDomain> storageDomains,
                                         StorageServerConnections storageServerConnections) {
        final StorageModel storageModel = (StorageModel) getWindow();
        StoragePool storagePool = storageModel.getDataCenter().getSelectedItem();

        final UICommand okCommand = UICommand.createDefaultOkUiCommand(commandName, this);

        if (storagePool.getId().equals(Guid.Empty)) {
            okCommand.execute();
            return;
        }

        VDS host = storageModel.getHost().getSelectedItem();

        AsyncDataProvider.getInstance().getStorageDomainsWithAttachedStoragePoolGuid(
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        List<StorageDomainStatic> attachedStorageDomains = (List<StorageDomainStatic>) returnValue;
                        if (!attachedStorageDomains.isEmpty()) {
                            ConfirmationModel model = new ConfirmationModel();
                            setConfirmWindow(model);

                            model.setTitle(ConstantsManager.getInstance().getConstants().storageDomainsAttachedToDataCenterWarningTitle());
                            model.setMessage(ConstantsManager.getInstance().getConstants().storageDomainsAttachedToDataCenterWarningMessage());
                            model.setHelpTag(HelpTag.import_storage_domain_confirmation);
                            model.setHashName("import_storage_domain_confirmation"); //$NON-NLS-1$

                            List<String> stoageDomainNames = new ArrayList<>();
                            for (StorageDomainStatic domain : attachedStorageDomains) {
                                stoageDomainNames.add(domain.getStorageName());
                            }
                            model.setItems(stoageDomainNames);

                            UICommand cancelCommand = createCancelCommand("CancelImportConfirm"); //$NON-NLS-1$
                            model.getCommands().add(okCommand);
                            model.getCommands().add(cancelCommand);
                        } else {
                            okCommand.execute();
                        }
                    }
                }), storagePool, storageDomains, storageServerConnections, host.getId());
    }

    @Override
    public void run(TaskContext context) {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if ("SaveNfs".equals(key)) { //$NON-NLS-1$
            saveNfsStorage(context);
        }
        else if ("SaveLocal".equals(key)) { //$NON-NLS-1$
            saveLocalStorage(context);
        }
        else if ("SavePosix".equals(key)) { //$NON-NLS-1$
            savePosixStorage(context);
        }
        else if ("SaveSan".equals(key)) { //$NON-NLS-1$
            saveSanStorage(context);
        }
        else if ("ImportFile".equals(key)) { //$NON-NLS-1$
            importFileStorage(context);
        }
        else if ("ImportSan".equals(key)) { //$NON-NLS-1$
            importSanStorage(context);
        }
        else if ("Finish".equals(key)) { //$NON-NLS-1$
            if (getWindow() == null) {
                return;
            }

            getWindow().stopProgress();

            if ((Boolean) data.get(1)) {
                cancel();
            }
            else {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
        }
    }


    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value) {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged() {
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "StorageListModel"; //$NON-NLS-1$
    }

    @Override
    protected void openReport() {

        final ReportModel reportModel = super.createReportModel();

        List<StorageDomain> items =
                getSelectedItems() != null && getSelectedItem() != null ? getSelectedItems()
                        : new ArrayList<StorageDomain>();
        StorageDomain storage = items.iterator().next();

        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                List<StoragePool> dataCenters = (List<StoragePool>) returnValue;
                for (StoragePool dataCenter : dataCenters) {
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
        ArrayList<StorageDomain> items =
                getSelectedItems() != null ? Linq.<StorageDomain> cast(getSelectedItems())
                        : new ArrayList<StorageDomain>();

        if (idParamName != null) {
            for (StorageDomain item : items) {
                if (isMultiple) {
                    reportModel.addResourceId(idParamName, item.getId().toString());
                } else {
                    reportModel.setResourceId(idParamName, item.getId().toString());
                }
            }
        }
    }

    private UICommand createCancelCommand(String commandName) {
        return UICommand.createCancelUiCommand(commandName, this);
    }

    private boolean isConnectionOverriden() {
        StorageModel model = (StorageModel) getWindow();
        NfsStorageModel nfsModel = (NfsStorageModel) model.getCurrentStorageItem();
        return nfsModel.getVersion().getSelectedItem().getEntity() != null
                || nfsModel.getRetransmissions().asConvertible().nullableShort() != null
                || nfsModel.getTimeout().asConvertible().nullableShort() != null
                || nfsModel.getMountOptions().getEntity() != null;
    }
}
