package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsAndReportsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.NewSharedMacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataCenterListModel extends ListWithDetailsAndReportsModel<Void, StoragePool> implements ISupportSystemTreeContext {

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private UICommand privateForceRemoveCommand;

    public UICommand getForceRemoveCommand() {
        return privateForceRemoveCommand;
    }

    private void setForceRemoveCommand(UICommand value) {
        privateForceRemoveCommand = value;
    }

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand() {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value) {
        privateGuideCommand = value;
    }

    private UICommand privateRecoveryStorageCommand;

    public UICommand getRecoveryStorageCommand() {
        return privateRecoveryStorageCommand;
    }

    private void setRecoveryStorageCommand(UICommand value) {
        privateRecoveryStorageCommand = value;
    }

    private UICommand addMacPoolCommand;

    public UICommand getAddMacPoolCommand() {
        return addMacPoolCommand;
    }

    private void setAddMacPoolCommand(UICommand addMacPoolCommand) {
        this.addMacPoolCommand = addMacPoolCommand;
    }

    protected Object[] getSelectedKeys() {
        if (getSelectedItems() == null) {
            return new Object[0];
        }
        else {
            ArrayList<Object> objL = new ArrayList<>();
            for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems())) {
                objL.add(a.getId());
            }
            return objL.toArray(new Object[] {});
        }
    }

    private Object privateGuideContext;

    public Object getGuideContext() {
        return privateGuideContext;
    }

    public void setGuideContext(Object value) {
        privateGuideContext = value;
    }

    final DataCenterQuotaListModel quotaListModel;
    final DataCenterIscsiBondListModel iscsiBondListModel;

    private final Provider<CommonModel> commonModelProvider;

    @Inject
    public DataCenterListModel(Provider<CommonModel> commonModelProvider,
            final DataCenterIscsiBondListModel dataCenterIscsiBondListModel,
            final DataCenterQuotaListModel dataCenterQuotaListModel,
            final DataCenterStorageListModel dataCenterStorageListModel,
            final DataCenterNetworkListModel dataCenterNetworkListModel,
            final DataCenterClusterListModel dataCenterClusterListModel,
            final DataCenterNetworkQoSListModel dataCenterNetworkQoSListModel,
            final DataCenterHostNetworkQosListModel dataCenterHostNetworkQosListModel,
            final DataCenterStorageQosListModel dataCenterStorageQosListModel,
            final DataCenterCpuQosListModel dataCenterCpuQosListModel,
            final PermissionListModel<StoragePool> permissionListModel,
            final DataCenterEventListModel dataCenterEventListModel) {
        this.commonModelProvider = commonModelProvider;
        iscsiBondListModel = dataCenterIscsiBondListModel;
        quotaListModel = dataCenterQuotaListModel;
        setDetailList(dataCenterStorageListModel, dataCenterNetworkListModel, dataCenterClusterListModel,
                dataCenterNetworkQoSListModel, dataCenterHostNetworkQosListModel, dataCenterStorageQosListModel,
                dataCenterCpuQosListModel, permissionListModel, dataCenterEventListModel);
        setTitle(ConstantsManager.getInstance().getConstants().dataCentersTitle());
        setApplicationPlace(WebAdminApplicationPlaces.dataCenterMainTabPlace);

        setDefaultSearchString("DataCenter:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_STORAGE_POOL_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setAddMacPoolCommand(new UICommand("AddMacPool", this)); //$NON-NLS-1$
        getAddMacPoolCommand().setTitle(ConstantsManager.getInstance().getConstants().addMacPoolButton());
        UICommand tempVar = new UICommand("ForceRemove", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(true);
        setForceRemoveCommand(tempVar);
        setRecoveryStorageCommand(new UICommand("RecoveryStorage", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void guide() {
        DataCenterGuideModel model = new DataCenterGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDataCenterGuideMeTitle());
        model.setHelpTag(HelpTag.new_data_center___guide_me);
        model.setHashName("new_data_center_-_guide_me"); //$NON-NLS-1$
        if (getGuideContext() == null) {
            StoragePool dataCenter = getSelectedItem();
            setGuideContext(dataCenter.getId());
        }

        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterListModel dataCenterListModel = (DataCenterListModel) target;
                        DataCenterGuideModel model = (DataCenterGuideModel) dataCenterListModel.getWindow();
                        model.setEntity((StoragePool) returnValue);

                        UICommand tempVar = new UICommand("Cancel", dataCenterListModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                        tempVar.setIsDefault(true);
                        tempVar.setIsCancel(true);
                        model.getCommands().add(tempVar);
                    }
                }), (Guid) getGuideContext());
    }

    private void setDetailList(final DataCenterStorageListModel dataCenterStorageListModel,
            final DataCenterNetworkListModel dataCenterNetworkListModel,
            final DataCenterClusterListModel dataCenterClusterListModel,
            final DataCenterNetworkQoSListModel dataCenterNetworkQoSListModel,
            final DataCenterHostNetworkQosListModel dataCenterHostNetworkQosListModel,
            final DataCenterStorageQosListModel dataCenterStorageQosListModel,
            final DataCenterCpuQosListModel dataCenterCpuQosListModel,
            final PermissionListModel<StoragePool> permissionListModel,
            final DataCenterEventListModel dataCenterEventListModel) {
        List<HasEntity<StoragePool>> list = new ArrayList<>();
        list.add(dataCenterStorageListModel);
        list.add(iscsiBondListModel);
        list.add(dataCenterNetworkListModel);
        list.add(dataCenterClusterListModel);
        quotaListModel.setIsAvailable(false);
        list.add(quotaListModel);
        list.add(dataCenterNetworkQoSListModel);
        list.add(dataCenterHostNetworkQosListModel);
        list.add(dataCenterStorageQosListModel);
        list.add(dataCenterCpuQosListModel);
        list.add(permissionListModel);
        list.add(dataCenterEventListModel);
        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("datacenter"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.StoragePool,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);

    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        DataCenterModel model = new DataCenterModel();
        model.setAddMacPoolCommand(addMacPoolCommand);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDataCenterTitle());
        model.setHelpTag(HelpTag.new_data_center);
        model.setHashName("new_data_center"); //$NON-NLS-1$
        model.setIsNew(true);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void edit() {
        StoragePool dataCenter = getSelectedItem();
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        if (getWindow() != null) {
            return;
        }

        final DataCenterModel model = new DataCenterModel();
        model.setAddMacPoolCommand(addMacPoolCommand);
        setWindow(model);
        model.setEntity(dataCenter);
        model.setDataCenterId(dataCenter.getId());
        model.setTitle(constants.editDataCenterTitle());
        model.setHelpTag(HelpTag.edit_data_center);
        model.setHashName("edit_data_center"); //$NON-NLS-1$
        model.getName().setEntity(dataCenter.getName());

        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter) {
            model.getName().setIsChangeable(false);
            model.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
        }

        model.getDescription().setEntity(dataCenter.getdescription());
        model.getComment().setEntity(dataCenter.getComment());
        model.setOriginalName(dataCenter.getName());

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;

                        if (storageDomainList.size() != 0) {
                            model.getStoragePoolType().setChangeProhibitionReason(
                                    constants.cannotChangeRepositoryTypeWithSDAttached());
                            model.getStoragePoolType().setIsChangeable(false);
                        }

                    }
                }), dataCenter.getId());

        model.getStoragePoolType().setSelectedItem(dataCenter.isLocal());

        model.getQuotaEnforceTypeListModel().setSelectedItem(dataCenter.getQuotaEnforcementType());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void addMacPool(final DataCenterModel dcModel) {
        SharedMacPoolModel macPoolModel = new NewSharedMacPoolModel.ClosingWithSetConfirmWindow(this) {
            @Override
            protected void onActionSucceeded(Guid macPoolId) {
                MacPool macPool = getEntity();
                macPool.setId(macPoolId);
                Collection<MacPool> macPools = new ArrayList<>(dcModel.getMacPoolListModel().getItems());
                macPools.add(macPool);
                dcModel.getMacPoolListModel().setItems(macPools);
                dcModel.getMacPoolListModel().setSelectedItem(macPool);
                DataCenterListModel.this.setConfirmWindow(null);
            }
        };

        macPoolModel.setEntity(new MacPool());
        setConfirmWindow(macPoolModel);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        boolean shouldAddressWarnning = false;
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDataCenterTitle());
        model.setHelpTag(HelpTag.remove_data_center);
        model.setHashName("remove_data_center"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems())) {
            list.add(a.getName());

            // If one of the Data Centers contain Storage Domain, show the warnning.
            if (a.getStatus() != StoragePoolStatus.Uninitialized) {
                shouldAddressWarnning = true;
            }
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
        if (shouldAddressWarnning) {
            model.setNote(ConstantsManager.getInstance().getConstants().removeDataCenterWarnningNote());
        }
    }

    public void forceRemove() {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().forceRemoveDataCenterTitle());
        model.setHelpTag(HelpTag.force_remove_data_center);
        model.setHashName("force_remove_data_center"); //$NON-NLS-1$
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangeable(true);

        ArrayList<String> list = new ArrayList<>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems())) {
            list.add(a.getName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnForceRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void recoveryStorage() {
        final ConfirmationModel windowModel = new ConfirmationModel();
        setWindow(windowModel);
        windowModel.setTitle(ConstantsManager.getInstance().getConstants().dataCenterReInitializeTitle());
        windowModel.setHelpTag(HelpTag.data_center_re_initialize);
        windowModel.setHashName("data_center_re-initialize"); //$NON-NLS-1$
        windowModel.getLatch().setIsAvailable(true);
        windowModel.getLatch().setIsChangeable(true);

        windowModel.startProgress();

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                windowModel.stopProgress();
                List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;
                List<EntityModel> models = new ArrayList<>();
                for (StorageDomain a : storageDomainList) {
                    if (a.getStorageDomainType() == StorageDomainType.Data
                            && (a.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)) {
                        EntityModel tempVar = new EntityModel();
                        tempVar.setEntity(a);
                        models.add(tempVar);
                    }
                }
                windowModel.setItems(models);

                if (models.size() > 0) {
                    EntityModel entityModel = models.size() != 0 ? models.get(0) : null;
                    if (entityModel != null) {
                        entityModel.setIsSelected(true);
                    }
                }

                if (models.isEmpty()) {
                    windowModel.setMessage(ConstantsManager.getInstance()
                                                   .getConstants()
                                                   .thereAreNoCompatibleStorageDomainsAttachThisDcMsg());
                    windowModel.getLatch().setIsAvailable(false);
                    UICommand tempVar2 = new UICommand("Cancel", DataCenterListModel.this); //$NON-NLS-1$
                    tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
                    tempVar2.setIsDefault(true);
                    tempVar2.setIsCancel(true);
                    windowModel.getCommands().add(tempVar2);
                } else {
                    UICommand tempVar3 = UICommand.createDefaultOkUiCommand("OnRecover", DataCenterListModel.this); //$NON-NLS-1$
                    windowModel.getCommands().add(tempVar3);
                    UICommand tempVar4 = UICommand.createCancelUiCommand("Cancel", DataCenterListModel.this); //$NON-NLS-1$
                    windowModel.getCommands().add(tempVar4);
                }

            }
        }));
    }

    public void onRecover() {

        final ConfirmationModel windowModel = (ConfirmationModel) getWindow();
        if (!windowModel.validate()) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;
                for (StorageDomain a : storageDomainList) {
                    if (a.getStorageDomainType() == StorageDomainType.Master) {
                        break;
                    }
                }
                List<StorageDomain> items = new ArrayList<>();
                for (EntityModel a : Linq.<EntityModel> cast(windowModel.getItems())) {
                    if (a.getIsSelected()) {
                        items.add((StorageDomain) a.getEntity());
                    }
                }
                if (items.size() > 0) {
                    if (windowModel.getProgress() != null) {
                        return;
                    }
                    ArrayList<VdcActionParametersBase> parameters =
                            new ArrayList<>();
                    for (StorageDomain a : items) {
                        parameters.add(new ReconstructMasterParameters(getSelectedItem().getId(),
                                a.getId()));
                    }
                    windowModel.startProgress();
                    Frontend.getInstance().runMultipleAction(VdcActionType.RecoveryStoragePool, parameters,
                            new IFrontendMultipleActionAsyncCallback() {
                                @Override
                                public void executed(FrontendMultipleActionAsyncResult result) {

                                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                                    localModel.stopProgress();
                                    cancel();

                                }
                            }, windowModel);
                }
                else {
                    cancel();
                }
            }
        }),
                getSelectedItem().getId());
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems())) {
            parameters.add(new StoragePoolParametersBase(a.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveStoragePool, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void onForceRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        if (!model.validate()) {
            return;
        }
        StoragePoolParametersBase tempVar = new StoragePoolParametersBase(getSelectedItem().getId());
        tempVar.setForceDelete(true);
        VdcActionParametersBase parametersBase = tempVar;
        Frontend.getInstance().runAction(VdcActionType.RemoveStoragePool, parametersBase);
        cancel();
    }

    public void cancel() {
        cancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        updateActionAvailability();
    }

    public void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public void onSave() {
        final DataCenterModel dcModel = (DataCenterModel) getWindow();

        if (!dcModel.validate()) {
            return;
        }

        if ((dcModel.getIsNew() || dcModel.getEntity() == null)
                && dcModel.getQuotaEnforceTypeListModel().getSelectedItem() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            promptNoQuotaInDCMessage();
        }
        else if (!dcModel.getIsNew()
                && getSelectedItem() != null
                && !dcModel.getVersion().getSelectedItem().equals(getSelectedItem().getCompatibilityVersion())) {
            final ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .changeDataCenterCompatibilityVersionTitle());
            confirmModel.setHelpTag(HelpTag.change_data_center_compatibility_version);
            confirmModel.setHashName("change_data_center_compatibility_version"); //$NON-NLS-1$

            final StoragePool sp = getSelectedItem();

            startProgress();

            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object ReturnValue) {
                    List<StorageDomain> storages = ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                    StorageDomain storage = null;
                    for (StorageDomain sd : storages) {
                        if (sd.getStorageDomainType().isDataDomain()) {
                            storage = sd;
                        }
                    }

                    StorageFormatType newFormat = null;
                    StorageFormatType oldFormat = null;
                    if (storage != null) {
                        newFormat = VersionStorageFormatUtil.getForVersion(dcModel.getVersion().getSelectedItem());
                        oldFormat = VersionStorageFormatUtil.getForVersion(sp.getCompatibilityVersion());
                    }

                    if (newFormat == oldFormat) {
                        confirmModel.setMessage(ConstantsManager.getInstance()
                                .getConstants()
                                .youAreAboutChangeDcCompatibilityVersionMsg());
                    } else {
                        Version v = VersionStorageFormatUtil.getEarliestVersionSupported(newFormat);
                        confirmModel.setMessage(ConstantsManager.getInstance()
                                .getMessages()
                                .youAreAboutChangeDcCompatibilityVersionWithUpgradeMsg(v.getValue()));
                    }
                    ((DataCenterListModel) model).stopProgress();
                }
            };
            IdQueryParameters params = new IdQueryParameters(sp.getId());
            Frontend.getInstance().runQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, params, _asyncQuery);

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveInternal", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar2);
        } else if (getSelectedItem() != null
                && getSelectedItem().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.HARD_ENFORCEMENT
                && dcModel.getQuotaEnforceTypeListModel().getSelectedItem() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            checkForQuotaInDC(dcModel.getEntity(), this);
        } else if (dcModel.getIsNew()) {
            //New data center, check for name uniqueness.
            validateDataCenterName(dcModel);
        } else {
            onSaveInternal();
        }
    }

    private void validateDataCenterName(final DataCenterModel dataCenter) {
        Frontend.getInstance().runQuery(VdcQueryType.GetStoragePoolByDatacenterName,
                new NameQueryParameters(dataCenter.getName().getEntity()),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VdcQueryReturnValue result = (VdcQueryReturnValue) returnValue;
                        if (!((Collection<?>)result.getReturnValue()).isEmpty()) {
                            dataCenter.getName().getInvalidityReasons().add(
                                    ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            dataCenter.getName().setIsValid(false);
                            dataCenter.setValidTab(TabName.GENERAL_TAB, false);
                        } else {
                            dataCenter.getName().getInvalidityReasons().clear();
                            dataCenter.getName().setIsValid(true);
                            dataCenter.setValidTab(TabName.GENERAL_TAB, true);
                            onSaveInternal();
                        }
                    }
                }
        ));
    }

    private void checkForQuotaInDC(StoragePool storage_pool, final ICommandTarget commandTarget) {
        IdQueryParameters parameters = new IdQueryParameters(storage_pool.getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetQuotaByStoragePoolId,
                parameters,
                new AsyncQuery(
                        quotaListModel,
                        new INewAsyncCallback() {

                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                if (((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue()).size() == 0) {
                                    promptNoQuotaInDCMessage();
                                } else {
                                    onSaveInternal();
                                }
                            }
                        }));
    }

    private void promptNoQuotaInDCMessage() {
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .changeDCQuotaEnforcementModeTitle());
        confirmModel.setHelpTag(HelpTag.change_data_center_quota_enforcement_mode);
        confirmModel.setHashName("change_data_center_quota_enforcement_mode"); //$NON-NLS-1$
        confirmModel.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .youAreAboutChangeDCQuotaEnforcementMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveInternal", this); //$NON-NLS-1$
        getConfirmWindow().getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
        getConfirmWindow().getCommands().add(tempVar2);
    }

    public void onSaveInternal() {
        DataCenterModel model = (DataCenterModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        StoragePool dataCenter =
                model.getIsNew() ? new StoragePool() : (StoragePool) Cloner.clone(getSelectedItem());

        // cancel confirm window if there is
        cancelConfirmation();

        // Save changes.
        dataCenter.setName(model.getName().getEntity());
        dataCenter.setdescription(model.getDescription().getEntity());
        dataCenter.setComment(model.getComment().getEntity());
        dataCenter.setIsLocal(model.getStoragePoolType().getSelectedItem());
        dataCenter.setCompatibilityVersion(model.getVersion().getSelectedItem());
        dataCenter.setQuotaEnforcementType(model.getQuotaEnforceTypeListModel()
                .getSelectedItem());
        dataCenter.setMacPoolId(model.getMacPoolListModel().getSelectedItem().getId());

        model.startProgress();


        if (model.getIsNew()) {
            // When adding a data center use sync action to be able present a Guide Me dialog afterwards.
            Frontend.getInstance().runAction(VdcActionType.AddEmptyStoragePool,
                new StoragePoolManagementParameter(dataCenter),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        DataCenterListModel localModel = (DataCenterListModel) result.getState();
                        localModel.postOnSaveInternal(result.getReturnValue());
                    }
                },
                this);
        } else {
            // Update the Quota at the corresponding DC object at the system tree.
            // The DC Quota value from the tree is used at MainTabDiskView.
            SystemTreeItemModel itemModel = commonModelProvider.get().getSystemTree().getItemById(dataCenter.getId());
            itemModel.setEntity(dataCenter);

            // Otherwise use async action in order to close dialog immediately.
            Frontend.getInstance().runMultipleAction(VdcActionType.UpdateStoragePool,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(
                    new StoragePoolManagementParameter(dataCenter))
                ),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        DataCenterListModel localModel = (DataCenterListModel) result.getState();
                        localModel.postOnSaveInternal(result.getReturnValue().get(0));
                    }
                },
                this);
        }
    }

    public void postOnSaveInternal(VdcReturnValueBase returnValue) {
        DataCenterModel model = (DataCenterModel) getWindow();

        model.stopProgress();

        cancel();

        if (model.getIsNew() && returnValue != null && returnValue.getSucceeded()) {

            setGuideContext(returnValue.getActionReturnValue());
            updateActionAvailability();
            getGuideCommand().execute();
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e) {
        super.itemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter) {
            StoragePool dataCenter = (StoragePool) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.firstOrNull(Linq.<StoragePool> cast(getItems()),
                    new Linq.IdPredicate<>(dataCenter.getId())));
        }
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();

        if (getSelectedItem() != null) {
            StoragePool storagePool = getSelectedItem();
            quotaListModel.setIsAvailable(storagePool.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
            updateIscsiBondListAvailability(storagePool);
        }
    }

    private void updateActionAvailability() {
        ArrayList<StoragePool> items =
                getSelectedItems() != null ? new ArrayList<>(Linq.<StoragePool> cast(getSelectedItems()))
                        : new ArrayList<StoragePool>();

        boolean isAllDown = true;
        for (StoragePool item : items) {
            if (item.getStatus() == StoragePoolStatus.Up || item.getStatus() == StoragePoolStatus.Contend) {
                isAllDown = false;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && items.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && isAllDown);

        StoragePool storagePoolItem = getSelectedItem();

        getForceRemoveCommand().setIsExecutionAllowed(storagePoolItem != null
                && items.size() == 1
                && storagePoolItem.getStatus() != StoragePoolStatus.Up);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getRecoveryStorageCommand().setIsExecutionAllowed(storagePoolItem != null && items.size() == 1
                && !storagePoolItem.isLocal() && storagePoolItem.getStatus() != StoragePoolStatus.Uninitialized
                && storagePoolItem.getStatus() != StoragePoolStatus.Up);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
        getForceRemoveCommand().setIsAvailable(isAvailable);
    }

    private void updateIscsiBondListAvailability(StoragePool storagePool) {
        AsyncDataProvider.getInstance().getStorageConnectionsByDataCenterIdAndStorageType(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                boolean hasIscsiStorage = false;

                ArrayList<StorageServerConnections> connections = (ArrayList<StorageServerConnections>) returnValue;

                for (StorageServerConnections connection : connections) {
                    if (connection.getStorageType() == StorageType.ISCSI) {
                        hasIscsiStorage = true;
                        break;
                    }
                }

                iscsiBondListModel.setIsAvailable(hasIscsiStorage);
            }
        }), storagePool.getId(), StorageType.ISCSI);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getForceRemoveCommand()) {
            forceRemove();
        }
        else if (command == getGuideCommand()) {
            guide();
        }
        else if (command == getRecoveryStorageCommand()) {
            recoveryStorage();
        }
        else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("OnForceRemove".equals(command.getName())) { //$NON-NLS-1$
            onForceRemove();
        }
        else if ("OnSaveInternal".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternal();
        }
        else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        }
        else if ("OnRecover".equals(command.getName())) { //$NON-NLS-1$
            onRecover();
        }
    }

    @Override
    public void executeCommand(UICommand command, Object... parameters) {
        super.executeCommand(command, parameters);

        if (command == getAddMacPoolCommand()) {
            addMacPool((DataCenterModel) parameters[0]);
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
        return "DataCenterListModel"; //$NON-NLS-1$
    }
}
