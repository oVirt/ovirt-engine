package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.ReduceSANStorageDomainDevicesCommandParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.SwitchMasterStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

import com.google.inject.Inject;

public class StorageListModel extends ListWithSimpleDetailsModel<Void, StorageDomain> implements ITaskTarget {

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

    private UICommand updateOvfsCommand;

    public UICommand getUpdateOvfsCommand() {
        return updateOvfsCommand;
    }

    private void setUpdateOvfsCommand(UICommand value) {
        updateOvfsCommand = value;
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

    private UICommand switchMasterCommand;

    public UICommand getSwitchMasterCommand() {
        return switchMasterCommand;
    }

    private void setSwitchMasterCommand(UICommand value) {
        switchMasterCommand = value;
    }

    @Inject
    public StorageListModel(final StorageGeneralModel storageGeneralModel,
            final StorageDataCenterListModel storageDataCenterListModel,
            final VmBackupModel storageVmBackupModel,
            final TemplateBackupModel storageTemplateBackupModel,
            final StorageRegisterVmListModel storageRegisterVmListModel,
            final StorageRegisterTemplateListModel storageRegisterTemplateListModel,
            final StorageRegisterDiskImageListModel storageRegisterDiskImageListModel,
            final StorageVmListModel storageVmListModel,
            final StorageTemplateListModel storageTemplateListModel,
            final StorageIsoListModel storageIsoListModel,
            final StorageDiskListModel storageDiskListModel,
            final StorageSnapshotListModel storageSnapshotListModel,
            final DiskProfileListModel diskProfileListModel,
            final StorageEventListModel storageEventListModel,
            final PermissionListModel<StorageDomain> permissionListModel,
            final StorageDRListModel storageDRListModel,
            final StorageLeaseListModel storageLeaseListModel) {
        this.generalModel = storageGeneralModel;
        this.dcListModel = storageDataCenterListModel;
        this.vmBackupModel = storageVmBackupModel;
        this.templateBackupModel = storageTemplateBackupModel;
        this.vmRegisterListModel = storageRegisterVmListModel;
        this.templateRegisterListModel = storageRegisterTemplateListModel;
        this.diskImageRegisterListModel = storageRegisterDiskImageListModel;
        this.vmListModel = storageVmListModel;
        this.templateListModel = storageTemplateListModel;
        this.isoListModel = storageIsoListModel;
        this.diskListModel = storageDiskListModel;
        this.snapshotListModel = storageSnapshotListModel;
        this.diskProfileListModel = diskProfileListModel;
        this.drListModel = storageDRListModel;
        this.leaseListModel = storageLeaseListModel;
        this.eventListModel = storageEventListModel;
        this.permissionListModel = permissionListModel;

        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setApplicationPlace(WebAdminApplicationPlaces.storageMainPlace);

        setDefaultSearchString(SearchStringMapping.STORAGE_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewDomainCommand(new UICommand("NewDomain", this)); //$NON-NLS-1$
        setImportDomainCommand(new UICommand("ImportDomain", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setUpdateOvfsCommand(new UICommand("UpdateOvfs", this)); //$NON-NLS-1$
        setDestroyCommand(new UICommand("Destroy", this)); //$NON-NLS-1$
        setScanDisksCommand(new UICommand("ScanDisks", this)); //$NON-NLS-1$
        setSwitchMasterCommand(new UICommand("SwitchMaster", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList() {
        generalModel.setIsAvailable(false);
        dcListModel.setIsAvailable(false);
        vmBackupModel.setIsAvailable(false);
        templateBackupModel.setIsAvailable(false);
        vmRegisterListModel.setIsAvailable(false);
        templateRegisterListModel.setIsAvailable(false);
        diskImageRegisterListModel.setIsAvailable(false);
        vmListModel.setIsAvailable(false);
        templateListModel.setIsAvailable(false);
        isoListModel.setIsAvailable(false);
        diskListModel.setIsAvailable(false);
        snapshotListModel.setIsAvailable(false);
        diskProfileListModel.setIsAvailable(false);
        drListModel.setIsAvailable(false);
        leaseListModel.setIsAvailable(false);

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
        list.add(leaseListModel);
        list.add(isoListModel);
        list.add(diskListModel);
        list.add(snapshotListModel);
        list.add(diskProfileListModel);
        list.add(drListModel);
        list.add(eventListModel);
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
    private final StorageSnapshotListModel snapshotListModel;
    private final DiskProfileListModel diskProfileListModel;
    private final StorageDRListModel drListModel;
    private final StorageLeaseListModel leaseListModel;
    private final StorageEventListModel eventListModel;
    private final PermissionListModel<StorageDomain> permissionListModel;

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
        super.syncSearch(QueryType.Search, tempVar);
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

        // putting all Data domains at the beginning on purpose (so when choosing the
        // first selectable storage type/function, it will be a Data one, if relevant).

        List<IStorageModel> items = AsyncDataProvider.getInstance().getDataStorageModels();
        items.addAll(AsyncDataProvider.getInstance().getIsoStorageModels());

        items.addAll(AsyncDataProvider.getInstance().getExportStorageModels());

        items.addAll(AsyncDataProvider.getInstance().getManagedBlockStorageModels());

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

        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editDomainTitle());
        model.setHelpTag(HelpTag.edit_domain);
        model.setHashName("edit_domain"); //$NON-NLS-1$
        model.setStorage(storage);
        model.getName().setEntity(storage.getStorageName());
        model.getDescription().setEntity(storage.getDescription());
        model.getComment().setEntity(storage.getComment());
        model.setOriginalName(storage.getStorageName());

        model.getDataCenter().setIsChangeable(false);
        model.getFormat().setIsChangeable(false);

        boolean isStorageNameEditable = model.isStorageActive() || model.isNewStorage();
        boolean isStorageInMaintenance = !model.isNewStorage() &&
                model.getStorage().getStatus() == StorageDomainStatus.Maintenance;
        model.getHost().setIsChangeable(false);
        model.getName().setIsChangeable(isStorageNameEditable);
        //set the field domain type to non editable
        model.getAvailableStorageTypeItems().setIsChangeable(false);
        model.getAvailableStorageDomainTypeItems().setIsChangeable(false);
        model.setIsChangeable(isStorageNameEditable && !isStorageInMaintenance);

        model.getWarningLowSpaceIndicator().setEntity(storage.getWarningLowSpaceIndicator());
        model.getWarningLowSpaceSize().setEntity(
                ConstantsManager.getInstance().getMessages().bracketsWithGB(storage.getWarningLowSpaceSize()));
        model.getWarningLowSpaceSize().setIsAvailable(true);
        model.getCriticalSpaceActionBlocker().setEntity(storage.getCriticalSpaceActionBlocker());
        model.getWarningLowConfirmedSpaceIndicator().setEntity(storage.getWarningLowConfirmedSpaceIndicator());

        IStorageModel item = prepareStorageForEdit(storage, model);

        model.setStorageModels(new ArrayList<>(Arrays.asList(new IStorageModel[]{item})));
        model.setCurrentStorageItem(item);

        model.initialize();

        UICommand command;
        command = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(command);

        command = createCancelCommand("Cancel"); //$NON-NLS-1$
        model.getCommands().add(command);

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
        case MANAGED_BLOCK_STORAGE:
            return new ManagedBlockStorageModel();
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
        } else if (model.getCurrentStorageItem() instanceof GlusterStorageModel) {
            GlusterStorageModel glusterModel = (GlusterStorageModel) model.getCurrentStorageItem();
            glusterModel.setMessage(null);

            // Check checkbox is selected or not
            if (glusterModel.getLinkGlusterVolume().getEntity()
                    && glusterModel.getGlusterVolumes().getSelectedItem() != null) {
                GlusterBrickEntity brick = glusterModel.getGlusterVolumes().getSelectedItem().getBricks().get(0);
                if (brick != null) {
                    String server =
                            brick.getNetworkId() != null && StringHelper.isNotNullOrEmpty(brick.getNetworkAddress())
                                    ? brick.getNetworkAddress()
                                    : brick.getServerName();
                    path = server + ":/" //$NON-NLS-1$
                            + glusterModel.getGlusterVolumes().getSelectedItem().getName();
                }
            } else if (!glusterModel.getLinkGlusterVolume().getEntity()) {
                path = glusterModel.getPath().getEntity();
            }
            if (StringHelper.isNotNullOrEmpty(path)) {
                Task.create(this,
                        new ArrayList<>(Arrays.asList(new Object[] { "ImportFile", //$NON-NLS-1$
                                host.getId(), path, glusterModel.getRole(), glusterModel.getType(),
                                model.getActivateDomain().getEntity() })))
                        .run();
            } else {
                return;
            }
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

        AsyncDataProvider.getInstance().isStorageDomainNameUnique(new AsyncQuery<>(isNameUnique -> {

            final StorageModel storageModel = (StorageModel) getWindow();

            String name1 = storageModel.getName().getEntity();
            String tempVar = storageModel.getOriginalName();
            String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$

            if (!isNameUnique && name1.compareToIgnoreCase(originalName) != 0) {
                storageModel.getName()
                    .getInvalidityReasons()
                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                storageModel.getName().setIsValid(false);
                postStorageNameValidation();
            } else {

                AsyncDataProvider.getInstance().getStorageDomainMaxNameLength(new AsyncQuery<>(nameMaxLength -> {
                    RegexValidation tempVar2 = new RegexValidation();
                    tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                    tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                                .nameCanContainOnlyMsg(nameMaxLength));
                    storageModel.getName().validateEntity(new IValidation[] {
                            new NotEmptyValidation(), tempVar2});
                    postStorageNameValidation();

                }));
            }

        }), name);
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
            Frontend.getInstance().runAction(ActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, hostId, false),
                null, this);
        }

    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        final RemoveStorageModel model = new RemoveStorageModel();
        StorageDomain storageDomain = getSelectedItem();
        if (storageDomain != null && storageDomain.getStorageType().isManagedBlockStorage()) {
            model.getFormat().setIsChangeable(false);
        }
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeStoragesTitle());
        model.setHelpTag(HelpTag.remove_storage);
        model.setHashName("remove_storage"); //$NON-NLS-1$

        StorageDomain storage = getSelectedItem();
        boolean localFsOnly = storage.getStorageType() == StorageType.LOCALFS;

        AsyncDataProvider.getInstance().getHostsForStorageOperation(new AsyncQuery<>(hosts -> {

            model.getHostList().setItems(hosts);
            model.getHostList().setSelectedItem(Linq.firstOrNull(hosts));

            if (hosts.isEmpty()) {
                UICommand tempVar = createCancelCommand("Cancel"); //$NON-NLS-1$
                tempVar.setIsDefault(true);
                model.getCommands().add(tempVar);
            } else {

                UICommand command;
                command = UICommand.createDefaultOkUiCommand("OnRemove", StorageListModel.this); //$NON-NLS-1$
                model.getCommands().add(command);

                command = createCancelCommand("Cancel"); //$NON-NLS-1$
                model.getCommands().add(command);
            }

        }), null, localFsOnly);
    }

    private void updateOvfs() {
        StorageDomain storage = getSelectedItem();
        if (storage != null) {
            StorageDomainParametersBase params = new StorageDomainParametersBase(storage.getId());
            Frontend.getInstance().runAction(ActionType.UpdateOvfStoreForStorageDomain, params, null, this);
        }
        cancel();
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

            Frontend.getInstance().runAction(ActionType.RemoveStorageDomain, tempVar, null, this);
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

        Frontend.getInstance().runMultipleAction(ActionType.ForceRemoveStorageDomain,
                new ArrayList<>(Arrays.asList(new ActionParametersBase[]{new StorageDomainParametersBase(storageDomain.getId())})),
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                },
                model);
    }

    private void scanDisks() {
        StorageDomain storageDomain = getSelectedItem();
        if (storageDomain != null) {
            Frontend.getInstance().runAction(ActionType.ScanStorageForUnregisteredDisks,
                    new StorageDomainParametersBase(storageDomain.getStoragePoolId(), storageDomain.getId()));
        }
    }

    private void switchMaster() {
        StorageDomain storageDomain = getSelectedItem();
        if (storageDomain != null) {
            Frontend.getInstance().runAction(ActionType.SwitchMasterStorageDomain,
                    new SwitchMasterStorageDomainCommandParameters(storageDomain.getStoragePoolId(),
                            storageDomain.getId()));
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
        } else if (model.getCurrentStorageItem() instanceof LocalStorageModel) {
            saveLocalStorage();
        } else if (model.getCurrentStorageItem() instanceof PosixStorageModel) {
            savePosixStorage();
        } else if (model.getCurrentStorageItem() instanceof ManagedBlockStorageModel) {
            saveManagedBlockStorage();
        } else {
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

    private void saveManagedBlockStorage() {

        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveManagedBlock"}))).run(); //$NON-NLS-1$
    }

    private void saveSanStorage() {
        StorageModel storageModel = (StorageModel) getWindow();
        final SanStorageModelBase sanStorageModelBase = (SanStorageModelBase) storageModel.getCurrentStorageItem();

        Guid hostId = sanStorageModelBase.getContainer().getHost().getSelectedItem().getId();
        if (sanStorageModelBase.getAddedLuns().isEmpty()) {
            onSaveSanStorage();
            return;
        }
        Set<String> unkownStatusLuns =
                sanStorageModelBase.getAddedLuns().stream().map(LunModel::getLunId).collect(Collectors.toSet());

        Frontend.getInstance()
                .runQuery(QueryType.GetDeviceList,
                        new GetDeviceListQueryParameters(hostId,
                                sanStorageModelBase.getType(),
                                true,
                                unkownStatusLuns,
                                false),
                        new AsyncQuery<QueryReturnValue>(response -> {
                            if (response.getSucceeded()) {
                                List<LUNs> checkedLuns = (ArrayList<LUNs>) response.getReturnValue();
                                postGetLunsMessages(sanStorageModelBase.getUsedLunsMessages(checkedLuns));
                            } else {
                                sanStorageModelBase.setGetLUNsFailure(
                                        ConstantsManager.getInstance()
                                                .getConstants()
                                                .couldNotRetrieveLUNsLunsFailure());
                            }
                        }, true));
    }

    private void postGetLunsMessages(ArrayList<String> usedLunsMessages) {

        if (usedLunsMessages.isEmpty()) {
            onSaveSanStorage();
        } else {
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
        SanStorageModelBase sanStorageModelBase = (SanStorageModelBase) storageModel.getCurrentStorageItem();
        sanStorageModelBase.setForce(true);

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
            Frontend.getInstance().runAction(ActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(fileConnection, hostId, false),
                    result -> {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        cleanConnection(storageListModel.fileConnection, storageListModel.hostId);
                        storageListModel.fileConnection = null;
                    },
                this);
        }
    }

    private void cancel() {
        setWindow(null);
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
            boolean isManagedBlockStorage = storage.getStorageType().isManagedBlockStorage();
            boolean isGlusterStorage = storage.getStorageType() == StorageType.GLUSTERFS;

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
            diskListModel.setIsAvailable(isDataStorage || isManagedBlockStorage);
            snapshotListModel.setIsAvailable(isDataStorage);
            diskProfileListModel.setIsAvailable(isDataStorage);
            drListModel.setIsAvailable(isGlusterStorage);
            leaseListModel.setIsAvailable(isDataStorage);

            isoListModel.setIsAvailable(isImageStorage);
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        fireModelChangeRelevantForActionsEvent();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        fireModelChangeRelevantForActionsEvent();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            fireModelChangeRelevantForActionsEvent();
        }
    }

    @Override
    protected void onModelChangeRelevantForActions() {
        // NOTE: Plugin API buttons listen for this event when they are added.  Nothing
        //       special needs to be done for them to be updated as long as the event
        //       is fired properly.
        super.onModelChangeRelevantForActions();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        List<StorageDomain> items = getSelectedItems() != null ? getSelectedItems() : new ArrayList<StorageDomain>();

        StorageDomain item = getSelectedItem();

        getNewDomainCommand().setIsAvailable(true);

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && isEditAvailable(item));

        getRemoveCommand().setIsExecutionAllowed(items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && items.get(0).getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached);

        getDestroyCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && item.getStatus() != StorageDomainStatus.Active);

        getScanDisksCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && item.getStatus() == StorageDomainStatus.Active
                && item.getStorageDomainType().isDataDomain());

        getSwitchMasterCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && !items.get(0).getStorageType().isOpenStackDomain()
                && !items.get(0).getStorageType().isManagedBlockStorage()
                && item.getStatus() == StorageDomainStatus.Active
                && item.getStorageDomainType().isDataDomain()
                && !item.isBackup());

        getUpdateOvfsCommand().setIsExecutionAllowed(item != null && items.size() == 1
                && item.getStorageDomainType().isDataDomain()
                && item.getStatus() == StorageDomainStatus.Active);
        getNewDomainCommand().setIsAvailable(true);
        getRemoveCommand().setIsAvailable(true);
        getDestroyCommand().setIsAvailable(true);
        getScanDisksCommand().setIsAvailable(true);
        getUpdateOvfsCommand().setIsAvailable(true);
        getSwitchMasterCommand().setIsAvailable(true);

    }

    private boolean isEditAvailable(StorageDomain storageDomain) {
        if (storageDomain == null) {
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
        } else if (command == getImportDomainCommand()) {
            importDomain();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getUpdateOvfsCommand()) {
            updateOvfs();
        } else if (command == getDestroyCommand()) {
            destroy();
        } else if (command == getScanDisksCommand()) {
            scanDisks();
        } else if (command == getSwitchMasterCommand()) {
            switchMaster();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        } else if ("CancelImportConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelImportConfirm();
        } else if ("OnImport".equals(command.getName())) { //$NON-NLS-1$
            onImport();
        } else if ("OnImportFile".equals(command.getName())) { //$NON-NLS-1$
            if (getConfirmWindow() != null && !((ConfirmationModel) getConfirmWindow()).validate()) {
                return;
            }
            cancelConfirm();
            showFormatUpgradeConfirmIfRequired("OnImportFilePostConfirm"); //$NON-NLS-1$
        } else if ("OnImportSan".equals(command.getName())) { //$NON-NLS-1$
            if (getConfirmWindow() != null && !((ConfirmationModel) getConfirmWindow()).validate()) {
                return;
            }
            cancelConfirm();
            showFormatUpgradeConfirmIfRequired("OnImportSanPostConfirm"); //$NON-NLS-1$
        } else if ("OnImportFilePostConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
            addExistingFileStorageDomain();
        } else if ("OnImportSanPostConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
            onImportSanDomainApprove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnDestroy".equals(command.getName())) { //$NON-NLS-1$
            onDestroy();
        } else if ("OnSaveSanStorage".equals(command.getName())) { //$NON-NLS-1$
            onSaveSanStorage();
        } else if ("SaveNewNfsStorage".equals(command.getName())) { //$NON-NLS-1$
            saveNewNfsStorage();
            cancelConfirm();
        } else if ("SaveNewPosixStorage".equals(command.getName())) { //$NON-NLS-1$
            saveNewPosixStorage();
            cancelConfirm();
        } else if ("HandleISOForNFS".equals(command.getName())){ //$NON-NLS-1$
            handleIsoDomain("SaveNewNfsStorage"); //$NON-NLS-1$
        } else if ("HandleISOForPosix".equals(command.getName())) { //$NON-NLS-1$
            handleIsoDomain("SaveNewPosixStorage"); //$NON-NLS-1$
        } else if ("CancelAll".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
            cancel();
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
        storageDomainStatic.setDiscardAfterDelete(model.getDiscardAfterDelete().getEntity());
        storageDomainStatic.setWarningLowSpaceIndicator(model.getWarningLowSpaceIndicator().getEntity());
        storageDomainStatic.setCriticalSpaceActionBlocker(model.getCriticalSpaceActionBlocker().getEntity());
        storageDomainStatic.setWarningLowConfirmedSpaceIndicator(model.getWarningLowConfirmedSpaceIndicator().getEntity());
        storageDomainStatic.setBackup(model.getBackup().getEntity());
    }

    private void savePosixStorage(TaskContext context) {

        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        final PosixStorageModel posixModel = (PosixStorageModel) storageModel;
        path = posixModel.getPath().getEntity();

        setStorageProperties();

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery<>(storages -> {

                if (storages != null && storages.size() > 0) {
                    posixModel.getPath().setIsValid(false);
                    handleDomainAlreadyExists(storages.get(0).getStorageName());
                } else if (storageDomain.getStorageDomainType() == StorageDomainType.ISO) {
                    UICommand handleISOType = UICommand.createDefaultOkUiCommand("HandleISOForPosix", this); //$NON-NLS-1$
                    handleISOType.execute();
                } else {
                    saveNewPosixStorage();
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

    private void saveManagedBlockStorage(TaskContext context) {

        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        final ManagedBlockStorageModel managedBlockStorageModel = (ManagedBlockStorageModel) storageModel;

        setStorageProperties();

        if (isNew) {
            AsyncDataProvider.getInstance().getManagedBlockStorageDomainsByDrivers(new AsyncQuery<>(storages -> {
                if (storages != null && storages.size() > 0) {
                    handleDomainAlreadyExists(storages.get(0).getId().toString());
                } else {
                    saveNewManagedBlockStorage();
                }
            }), managedBlockStorageModel.getDriverOptions().serializeToMap(), managedBlockStorageModel.getDriverSensitiveOptions().serializeToMap());
        } else {
            updateStorageDomain();
        }
    }

    private void updateStorageDomain() {
        Frontend.getInstance().runAction(ActionType.UpdateStorageDomain, new StorageDomainManagementParameter(this.storageDomain),
                result -> {
                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
                }, this);
    }

    public void saveNewManagedBlockStorage() {

        StorageModel model = (StorageModel) getWindow();
        ManagedBlockStorageModel ManagedBlockStorageModel = (ManagedBlockStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(ManagedBlockStorageModel.getAddStorageDomainVdcAction());

        AddManagedBlockStorageDomainParameters parameter = new AddManagedBlockStorageDomainParameters();
        parameter.setStorageDomain(storageDomain);
        parameter.setVdsId(host.getId());
        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
        parameter.setStoragePoolId(dataCenter.getId());
        Map<String, Object> driverOptions = ManagedBlockStorageModel.getDriverOptions().serializeToMap();
        driverOptions.put(AddManagedBlockStorageDomainParameters.VOLUME_BACKEND_NAME, storageDomain.getStorageName());
        parameter.setDriverOptions(driverOptions);
        parameter.setSriverSensitiveOptions(ManagedBlockStorageModel.getDriverSensitiveOptions().serializeToMap());

        parameters.add(parameter);

        IFrontendActionAsyncCallback callback1 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageId = actionReturnValue.getActionReturnValue();

            // Attach storage to data center as necessary.
            StorageModel storageModel = (StorageModel) storageListModel.getWindow();
            StoragePool dataCenter1 = storageModel.getDataCenter().getSelectedItem();
            if (!dataCenter1.getId().equals(StorageModel.UnassignedDataCenterId)) {
                storageListModel.attachStorageToDataCenter(storageListModel.storageId, dataCenter1.getId(), storageModel.getActivateDomain().getEntity());
            }

            storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
        };

        IFrontendActionAsyncCallback failureCallback = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);
        };

        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1})),
                failureCallback,
                this);
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
        if (posixModel instanceof GlusterStorageModel) {
            GlusterStorageModel glusterModel = (GlusterStorageModel) posixModel;
            Guid glusterVolId = null;
            if (glusterModel.getLinkGlusterVolume().getEntity()) {
                glusterVolId = ((GlusterStorageModel) posixModel).getGlusterVolumes().getSelectedItem() != null
                        ? ((GlusterStorageModel) posixModel).getGlusterVolumes().getSelectedItem().getId() : null;
            }
            connection.setGlusterVolumeId(glusterVolId);
        }
        this.connection = connection;

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(ActionType.AddStorageServerConnection);
        actionTypes.add(posixModel.getAddStorageDomainVdcAction());

        parameters.add(new StorageServerConnectionParametersBase(this.connection, host.getId(), false));
        StorageDomainManagementParameter parameter = new StorageDomainManagementParameter(storageDomain);
        parameter.setVdsId(host.getId());
        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
        parameter.setStoragePoolId(dataCenter.getId());
        parameters.add(parameter);

        IFrontendActionAsyncCallback callback1 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageDomain.setStorage((String) actionReturnValue.getActionReturnValue());
            storageListModel.connection.setId((String) actionReturnValue.getActionReturnValue());

        };

        IFrontendActionAsyncCallback callback2 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageId = actionReturnValue.getActionReturnValue();

            // Attach storage to data center as necessary.
            StorageModel storageModel = (StorageModel) storageListModel.getWindow();
            StoragePool dataCenter1 = storageModel.getDataCenter().getSelectedItem();
            if (!dataCenter1.getId().equals(StorageModel.UnassignedDataCenterId)) {
                storageListModel.attachStorageToDataCenter(storageListModel.storageId, dataCenter1.getId(), storageModel.getActivateDomain().getEntity());
            }

            storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
        };

        IFrontendActionAsyncCallback failureCallback = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);
        };

        Frontend.getInstance().runMultipleActions(actionTypes,
            parameters,
            new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1, callback2})),
            failureCallback,
            this);
    }

    private void saveNfsStorage(TaskContext context) {
        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        final NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = nfsModel.getPath().getEntity();

        setStorageProperties();

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery<>(storages -> {

                if (storages != null && storages.size() > 0) {
                    nfsModel.getPath().setIsValid(false);
                    handleDomainAlreadyExists(storages.get(0).getStorageName());
                } else if (storageDomain.getStorageDomainType() == StorageDomainType.ISO){
                    UICommand handleISOType = UICommand.createDefaultOkUiCommand("HandleISOForNFS", this); //$NON-NLS-1$
                    handleISOType.execute();
                } else {
                    saveNewNfsStorage();
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
        } else if (storageModel instanceof PosixStorageModel) {
            updatePosixProperties(storageModel);
        }

        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId, false);
        Frontend.getInstance().runAction(ActionType.UpdateStorageServerConnection, parameters,
                result -> {
                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

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
        if (posixModel.getType().equals(StorageType.GLUSTERFS)) {
            //TBD: set gluster vol id only if managed glustervol selected
            connection.setGlusterVolumeId(null);
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

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(ActionType.AddStorageServerConnection);
        actionTypes.add(ActionType.AddNFSStorageDomain);
        actionTypes.add(ActionType.DisconnectStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId(), false));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        StoragePool dataCenter = model.getDataCenter().getSelectedItem();
        tempVar2.setStoragePoolId(dataCenter.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId(), false));

        IFrontendActionAsyncCallback callback1 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageDomain.setStorage((String) actionReturnValue.getActionReturnValue());
            storageListModel.connection.setId((String) actionReturnValue.getActionReturnValue());

        };
        IFrontendActionAsyncCallback callback2 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageId = actionReturnValue.getActionReturnValue();

        };
        IFrontendActionAsyncCallback callback3 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            StorageModel storageModel = (StorageModel) storageListModel.getWindow();

            // Attach storage to data center as necessary.
            StoragePool dataCenter1 = storageModel.getDataCenter().getSelectedItem();
            if (!dataCenter1.getId().equals(StorageModel.UnassignedDataCenterId)) {
                storageListModel.attachStorageToDataCenter(storageListModel.storageId, dataCenter1.getId(), storageModel.getActivateDomain().getEntity());
            }

            storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

        };
        IFrontendActionAsyncCallback failureCallback = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);

        };
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(callback1, callback2, callback3)),
                failureCallback,
                this);
    }

    public void saveNewSanStorage() {
        StorageModel model = (StorageModel) getWindow();
        SanStorageModelBase sanModel = (SanStorageModelBase) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        boolean force = sanModel.isForce();

        Set<String> lunIds = sanModel.getAddedLuns().stream().map(LunModel::getLunId).collect(Collectors.toSet());

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storageDomain);
        params.setVdsId(host.getId());
        params.setLunIds(lunIds);
        params.setForce(force);
        Frontend.getInstance().runAction(ActionType.AddSANStorageDomain, params,
                result -> {
                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        StorageModel storageModel = (StorageModel) storageListModel.getWindow();
                        storageListModel.storageModel = storageModel.getCurrentStorageItem();
                        if (!result.getReturnValue().getSucceeded()) {
                            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);
                            return;
                        }

                        StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                            ActionReturnValue returnValue = result.getReturnValue();
                            Guid storageId = returnValue.getActionReturnValue();
                            storageListModel.attachStorageToDataCenter(storageId, dataCenter.getId(), storageModel.getActivateDomain().getEntity());
                        }

                    storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);
                }, this);
    }

    private void setStorageProperties() {
        StorageDomain selectedItem = getSelectedItem();
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;

        storageDomain =
                isNew ? new StorageDomainStatic()
                        : (StorageDomainStatic) Cloner.clone(selectedItem.getStorageStaticData());

        saveBaseStorageProperties(model);
        storageDomain.setStorageFormat(model.getFormat().getSelectedItem());
    }

    private void saveLocalStorage(TaskContext context) {
        this.context = context;

        StorageModel model = (StorageModel) getWindow();
        VDS host = model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = localModel.getPath().getEntity();

        setStorageProperties();

        if (isNew) {
            AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery<>(storages -> {

                if (storages != null && storages.size() > 0) {
                    handleDomainAlreadyExists(storages.get(0).getStorageName());
                } else {
                    saveNewLocalStorage();
                }

            }), host.getStoragePoolId(), path);
        } else {
            StorageDomain storageDomain = getSelectedItem();
            if (isPathEditable(storageDomain)) {
                updatePath();
            }
            updateStorageDomain();
        }
    }

    private void handleIsoDomain(String command) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().areYouSureTitle());
        confirmationModel.setMessage(ConstantsManager.getInstance().getMessages().creatingIsoDomainDeprecatedMessage());
        confirmationModel.setAlertType(ConfirmationModel.AlertType.INFO);
        confirmationModel.setHelpTag(HelpTag.create_iso_domain);
        confirmationModel.setHashName("create_iso_domain"); //$NON-NLS-1$
        setConfirmWindow(confirmationModel);

        UICommand onApprove = new UICommand(command, this); //$NON-NLS-1$
        onApprove.setTitle(ConstantsManager.getInstance().getConstants().ok());
        onApprove.setIsDefault(true);
        confirmationModel.getCommands().add(onApprove);

        UICommand cancel = new UICommand("CancelAll", this); //$NON-NLS-1$
        cancel.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancel.setIsCancel(true);
        confirmationModel.getCommands().add(cancel);
    }

    private void handleDomainAlreadyExists(String storageName) {
        onFinish(context,
            false,
            storageModel,
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

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(ActionType.AddStorageServerConnection);
        actionTypes.add(ActionType.AddLocalStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId(), false));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);

        IFrontendActionAsyncCallback callback1 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            storageListModel.removeConnection = true;

            ActionReturnValue actionReturnValue = result.getReturnValue();
            storageListModel.storageDomain.setStorage((String) actionReturnValue.getActionReturnValue());
            storageListModel.connection.setId((String) actionReturnValue.getActionReturnValue());

        };
        IFrontendActionAsyncCallback callback2 = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();
            storageListModel.removeConnection = false;

            storageListModel.onFinish(storageListModel.context, true, storageListModel.storageModel);

        };
        IFrontendActionAsyncCallback failureCallback = result -> {

            StorageListModel storageListModel = (StorageListModel) result.getState();

            if (storageListModel.removeConnection) {
                storageListModel.cleanConnection(storageListModel.connection, storageListModel.hostId);
                storageListModel.removeConnection = false;
            }

            storageListModel.onFinish(storageListModel.context, false, storageListModel.storageModel);

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
        SanStorageModelBase sanModel = (SanStorageModelBase) model.getCurrentStorageItem();
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
        } else {
            Frontend.getInstance().runAction(ActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain), new IFrontendActionAsyncCallback() {
                @Override
                public void executed(FrontendActionAsyncResult result) {

                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    StorageModel storageModel = (StorageModel) getWindow();
                    SanStorageModelBase sanStorageModelBase = (SanStorageModelBase) storageModel.getCurrentStorageItem();
                    boolean force = sanStorageModelBase.isForce();
                    StorageDomain storageDomain1 = storageListModel.getSelectedItem();

                    Set<String> lunIds =
                            sanStorageModelBase.getAddedLuns().stream().map(LunModel::getLunId).collect(Collectors.toSet());

                    if (lunIds.size() > 0) {
                        Frontend.getInstance().runAction(ActionType.ExtendSANStorageDomain,
                            new ExtendSANStorageDomainParameters(storageDomain1.getId(), new HashSet<>(lunIds), force),
                            null, this);
                    }

                    Set<String> lunToRefreshIds = sanStorageModelBase.getLunsToRefresh();

                    if (lunToRefreshIds.size() > 0) {
                        Frontend.getInstance().runAction(ActionType.RefreshLunsSize,
                                new ExtendSANStorageDomainParameters(storageDomain1.getId(), lunToRefreshIds, false),
                                null, this);
                    }

                    if (storageDomain1.getStatus() == StorageDomainStatus.Maintenance) {
                        Set<String> lunsToRemoveIds = sanStorageModelBase.getLunsToRemove();

                        if (lunsToRemoveIds.size() > 0) {
                            Frontend.getInstance().runAction(ActionType.ReduceSANStorageDomainDevices,
                                    new ReduceSANStorageDomainDevicesCommandParameters(storageDomain1.getId(),
                                            new ArrayList<>(lunsToRemoveIds)),
                                    null, this);
                        }
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
        Frontend.getInstance().runAction(ActionType.AttachStorageDomainToPool, params, null, this);
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
        storageDomainsToAdd = importSanStorageModel.getStorageDomains().getSelectedItems();
        checkSanDomainAttachedToDc("OnImportSan", storageDomainsToAdd); //$NON-NLS-1$
    }

    private void onImportSanDomainApprove() {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        hostId = (Guid) data.get(1);

        ImportSanStorageModel importSanStorageModel = (ImportSanStorageModel) storageModel;
        final List<StorageDomain> storageDomains = importSanStorageModel.getStorageDomains().getSelectedItems();

        ArrayList<ActionParametersBase> parametersList = new ArrayList<>(items.size());
        List<IFrontendActionAsyncCallback> callbacks = new LinkedList<>();

        for (final StorageDomain storageDomain : storageDomains) {
            StorageDomainStatic staticData = storageDomain.getStorageStaticData();
            saveDefaultedStorageProperties((StorageModel) getWindow(), staticData);
            StorageDomainManagementParameter parameters =
                    new StorageDomainManagementParameter(staticData);
            parameters.setVdsId(hostId);
            parametersList.add(parameters);

            callbacks.add(result -> {
                ActionReturnValue returnValue = result.getReturnValue();
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
                } else {
                    onFinish(context, false, storageModel);
                }
            });
        }

        Frontend.getInstance().runMultipleActions(ActionType.AddExistingBlockStorageDomain, parametersList, callbacks);
    }

    public void importFileStorageInit() {
        if (fileConnection != null) {
            // Clean nfs connection
            Frontend.getInstance().runAction(ActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(fileConnection, hostId, false),
                    result -> {

                        StorageListModel storageListModel = (StorageListModel) result.getState();
                        ActionReturnValue returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success) {
                            storageListModel.fileConnection = null;
                        }
                        storageListModel.importFileStoragePostInit();

                    },
                this);
        } else {
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
        AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery<>(storages -> {

            if (storages != null && storages.size() > 0) {

                String storageName = storages.get(0).getStorageName();
                onFinish(context,
                    false,
                    storageModel,
                    ConstantsManager.getInstance().getMessages().importFailedDomainAlreadyExistStorageMsg(storageName));
            } else {
                StorageServerConnections tempVar = new StorageServerConnections();
                tempVar.setConnection(path);
                tempVar.setStorageType(storageType);
                if (storageModel instanceof NfsStorageModel) {
                    NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
                    if (isConnectionOverriden()) {
                        tempVar.setNfsVersion((NfsVersion) ((EntityModel) nfsModel.getVersion().getSelectedItem()).getEntity());
                        tempVar.setNfsRetrans(nfsModel.getRetransmissions().asConvertible().nullableShort());
                        tempVar.setNfsTimeo(nfsModel.getTimeout().asConvertible().nullableShort());
                        tempVar.setMountOptions(nfsModel.getMountOptions().getEntity());
                    }
                }
                if (storageModel instanceof PosixStorageModel) {
                    PosixStorageModel posixModel = (PosixStorageModel) storageModel;
                    tempVar.setVfsType(posixModel.getVfsType().getEntity());
                    tempVar.setMountOptions(posixModel.getMountOptions().getEntity());
                }
                fileConnection = tempVar;
                importFileStorageConnect();
            }
        }), storagePoolId, path);
    }

    public void importFileStorageConnect() {
        Frontend.getInstance().runAction(ActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(fileConnection, hostId, false),
                result -> {
                    StorageListModel storageListModel = (StorageListModel) result.getState();
                    ActionReturnValue returnVal = result.getReturnValue();
                    boolean success = returnVal != null && returnVal.getSucceeded();
                    if (success) {
                        storageListModel.fileConnection.setId((String) returnVal.getActionReturnValue());
                        getExistingStorageDomainList();
                    } else {
                        postImportFileStorage(storageListModel.context,
                            false,
                            storageListModel.storageModel,
                            ConstantsManager.getInstance()
                                    .getConstants()
                                    .failedToRetrieveExistingStorageDomainInformationMsg());
                    }
                },
            this);
    }

    private void getExistingStorageDomainList() {
        AsyncDataProvider.getInstance().getExistingStorageDomainList(new AsyncQuery<>(
                        domains -> {
                            if (domains != null && !domains.isEmpty()) {
                                storageDomainsToAdd = domains;
                                if (storageModel.getRole() == StorageDomainType.Data) {
                                    checkFileDomainAttachedToDc("OnImportFile", fileConnection); //$NON-NLS-1$
                                } else {
                                    addExistingFileStorageDomain();
                                }
                            } else {
                                String errorMessage = domains == null ?
                                        ConstantsManager.getInstance().getConstants()
                                                .failedToRetrieveExistingStorageDomainInformationMsg() :
                                        ConstantsManager.getInstance().getConstants()
                                                .thereIsNoStorageDomainUnderTheSpecifiedPathMsg();

                                postImportFileStorage(context,
                                        false,
                                        storageModel,
                                        errorMessage);

                                cleanConnection(fileConnection, hostId);
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

        // clearing cached storage domains
        storageDomainsToAdd = null;

        StorageDomainManagementParameter params = new StorageDomainManagementParameter(sdsToAdd);
        params.setVdsId(hostId);
        Frontend.getInstance().runAction(ActionType.AddExistingFileStorageDomain, params, result -> {

            Object[] array = (Object[]) result.getState();
            StorageListModel storageListModel = (StorageListModel) array[0];
            StorageDomain sdToAdd1 = (StorageDomain) array[1];
            ActionReturnValue returnVal = result.getReturnValue();

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
        }, new Object[] {this, sdToAdd});
    }

    public void postImportFileStorage(TaskContext context, boolean isSucceeded, IStorageModel model, String message) {
        Frontend.getInstance().runAction(ActionType.DisconnectStorageServerConnection,
            new StorageServerConnectionParametersBase(fileConnection, hostId, false),
                result -> {

                    ActionReturnValue returnValue = result.getReturnValue();
                    boolean success = returnValue != null && returnValue.getSucceeded();
                    if (success) {
                        fileConnection = null;
                    }
                    Object[] array = (Object[]) result.getState();
                    onFinish((TaskContext) array[0],
                        (Boolean) array[1],
                        (IStorageModel) array[2],
                        (String) array[3]);

                },
            new Object[] {context, isSucceeded, model, message});
    }

    private void checkSanDomainAttachedToDc(String commandName, List<StorageDomain> storageDomains) {
        checkDomainAttachedToDc(commandName, storageDomains, null);
    }

    private void checkFileDomainAttachedToDc(String commandName, StorageServerConnections storageServerConnections) {
        checkDomainAttachedToDc(commandName, null, storageServerConnections);
    }

    private void showFormatUpgradeConfirmIfRequired(String okCommandName) {
        StorageModel storageModel = (StorageModel) getWindow();
        StoragePool dc = storageModel.getDataCenter().getSelectedItem();

        StorageFormatUpgradeConfirmationModel model = new StorageFormatUpgradeConfirmationModel();
        boolean shouldDisplay = model.initialize(
                storageDomainsToAdd, dc,
                okCommandName, "CancelImportConfirm", this); //$NON-NLS-1$
        if (shouldDisplay) {
            setConfirmWindow(model);
            model.setHelpTag(HelpTag.import_storage_domain_confirmation);
            model.setHashName("import_storage_domain_confirmation"); //$NON-NLS-1$
        } else {
            UICommand okCommand = UICommand.createDefaultOkUiCommand(okCommandName, this);
            okCommand.execute();
        }
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
                new AsyncQuery<>(attachedStorageDomains -> {
                    if (!attachedStorageDomains.isEmpty()) {
                        ConfirmationModel model = new ConfirmationModel();
                        setConfirmWindow(model);

                        model.setTitle(ConstantsManager.getInstance().getConstants().storageDomainsAttachedToDataCenterWarningTitle());
                        model.setMessage(ConstantsManager.getInstance().getConstants().storageDomainsAttachedToDataCenterWarningMessage());
                        model.setHelpTag(HelpTag.import_storage_domain_confirmation);
                        model.setHashName("import_storage_domain_confirmation"); //$NON-NLS-1$

                        List<String> stoageDomainNames = attachedStorageDomains
                                .stream()
                                .map(StorageDomainStatic::getStorageName)
                                .collect(Collectors.toList());

                        model.setItems(stoageDomainNames);

                        UICommand cancelCommand = createCancelCommand("CancelImportConfirm"); //$NON-NLS-1$
                        model.getCommands().add(okCommand);
                        model.getCommands().add(cancelCommand);
                    } else {
                        okCommand.execute();
                    }
                }), storagePool, storageDomains, storageServerConnections, host.getId());
    }

    @Override
    public void run(TaskContext context) {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if ("SaveNfs".equals(key)) { //$NON-NLS-1$
            saveNfsStorage(context);
        } else if ("SaveLocal".equals(key)) { //$NON-NLS-1$
            saveLocalStorage(context);
        } else if ("SavePosix".equals(key)) { //$NON-NLS-1$
            savePosixStorage(context);
        } else if ("SaveSan".equals(key)) { //$NON-NLS-1$
            saveSanStorage(context);
        } else if ("SaveManagedBlock".equals(key)) { //$NON-NLS-1$
            saveManagedBlockStorage(context);
        }else if ("ImportFile".equals(key)) { //$NON-NLS-1$
            importFileStorage(context);
        } else if ("ImportSan".equals(key)) { //$NON-NLS-1$
            importSanStorage(context);
        } else if ("Finish".equals(key)) { //$NON-NLS-1$
            if (getWindow() == null) {
                return;
            }

            getWindow().stopProgress();

            if ((Boolean) data.get(1)) {
                cancel();
            } else {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
        }
    }

    @Override
    protected String getListName() {
        return "StorageListModel"; //$NON-NLS-1$
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

    public StorageGeneralModel getGeneralModel() {
        return generalModel;
    }

    public StorageDataCenterListModel getDcListModel() {
        return dcListModel;
    }

    public StorageRegisterVmListModel getVmRegisterListModel() {
        return vmRegisterListModel;
    }

    public VmBackupModel getVmBackupModel() {
        return vmBackupModel;
    }

    public StorageRegisterTemplateListModel getTemplateRegisterListModel() {
        return templateRegisterListModel;
    }

    public TemplateBackupModel getTemplateBackupModel() {
        return templateBackupModel;
    }

    public StorageRegisterDiskImageListModel getDiskImageRegisterListModel() {
        return diskImageRegisterListModel;
    }

    public StorageVmListModel getVmListModel() {
        return vmListModel;
    }

    public StorageTemplateListModel getTemplateListModel() {
        return templateListModel;
    }

    public StorageIsoListModel getIsoListModel() {
        return isoListModel;
    }

    public StorageDiskListModel getDiskListModel() {
        return diskListModel;
    }

    public StorageSnapshotListModel getSnapshotListModel() {
        return snapshotListModel;
    }

    public StorageLeaseListModel getLeaseListModel() {
        return leaseListModel;
    }

    public DiskProfileListModel getDiskProfileListModel() {
        return diskProfileListModel;
    }

    public StorageDRListModel getDRListModel() {
        return drListModel;
    }

    public StorageEventListModel getEventListModel() {
        return eventListModel;
    }

    public PermissionListModel<StorageDomain> getPermissionListModel() {
        return permissionListModel;
    }

}
