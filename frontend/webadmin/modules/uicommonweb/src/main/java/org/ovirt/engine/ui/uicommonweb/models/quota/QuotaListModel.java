package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public class QuotaListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {

    private static final String COPY_OF = "Copy_of_"; //$NON-NLS-1$

    private UICommand createCommand;
    private UICommand removeCommand;
    private UICommand editCommand;
    private UICommand cloneCommand;

    public UICommand getCreateCommand() {
        return createCommand;
    }

    public void setCreateCommand(UICommand createQuotaCommand) {
        this.createCommand = createQuotaCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    public void setRemoveCommand(UICommand removeQuotaCommand) {
        this.removeCommand = removeQuotaCommand;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    public void setEditCommand(UICommand editQuotaCommand) {
        this.editCommand = editQuotaCommand;
    }

    public QuotaListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());

        setDefaultSearchString("Quota:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.QUOTA_OBJ_NAME, SearchObjects.QUOTA_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setCreateCommand(new UICommand("Create", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setCloneCommand(new UICommand("Clone", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        updateActionAvailability();
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
    protected void initDetailModels() {
        super.initDetailModels();
        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new QuotaClusterListModel());
        list.add(new QuotaStorageListModel());
        list.add(new QuotaVmListModel());
        list.add(new QuotaTemplateListModel());
        list.add(new QuotaUserListModel());
        list.add(new QuotaPermissionListModel());
        list.add(new QuotaEventListModel());

        setDetailModels(list);
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.Quota,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    private void updateActionAvailability() {
        List items =
                getSelectedItems() != null && getSelectedItem() != null ? getSelectedItems()
                        : new ArrayList();
        getEditCommand().setIsExecutionAllowed(items.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(items.size() > 0);
        getCloneCommand().setIsExecutionAllowed(items.size() == 1);
    }

    protected void createQuota() {
        createQuota(true);
    }

    protected void createQuota(boolean populateDataCenter) {
        final QuotaModel qModel = new QuotaModel();
        qModel.setTitle(ConstantsManager.getInstance().getConstants().newQuotaTitle());
        qModel.setHelpTag(HelpTag.new_quota);
        qModel.setHashName("new_quota"); //$NON-NLS-1$
        Quota newQuota = new Quota();
        qModel.setEntity(newQuota);
        setWindow(qModel);
        qModel.startProgress(null);

        if (populateDataCenter) {
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {

                @Override
                public void onSuccess(Object model, Object returnValue) {
                    ArrayList<StoragePool> dataCenterList = (ArrayList<StoragePool>) returnValue;
                    if (dataCenterList == null || dataCenterList.size() == 0) {
                        return;
                    }
                    QuotaListModel quotaListModel = (QuotaListModel) model;
                    QuotaModel quotaModel = (QuotaModel) quotaListModel.getWindow();
                    quotaModel.getDataCenter().setItems(dataCenterList);
                    quotaModel.getDataCenter().setSelectedItem(dataCenterList.get(0));

                    if (quotaListModel.getSystemTreeSelectedItem() != null
                            && quotaListModel.getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter) {
                        StoragePool selectDataCenter =
                                (StoragePool) quotaListModel.getSystemTreeSelectedItem().getEntity();

                        quotaModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenterList,
                                new Linq.DataCenterPredicate(selectDataCenter.getId())));
                        quotaModel.getDataCenter().setIsChangable(false);
                    }
                }
            }));
        }

        qModel.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                StoragePool selectedDataCenter = qModel.getDataCenter().getSelectedItem();
                if (selectedDataCenter == null) {
                    return;
                }
                AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) returnValue;
                        if (clusterList == null || clusterList.size() == 0) {
                            qModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaVdsGroup>());
                            return;
                        }
                        ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
                        QuotaVdsGroup quotaVdsGroup;
                        for (VDSGroup vdsGroup : clusterList) {
                            quotaVdsGroup = new QuotaVdsGroup();
                            quotaVdsGroup.setVdsGroupId(vdsGroup.getId());
                            quotaVdsGroup.setVdsGroupName(vdsGroup.getName());
                            quotaVdsGroup.setMemSizeMB(null);
                            quotaVdsGroup.setMemSizeMBUsage((long) 0);
                            quotaVdsGroup.setVirtualCpu(null);
                            quotaVdsGroup.setVirtualCpuUsage(0);
                            quotaClusterList.add(quotaVdsGroup);
                        }
                        qModel.getAllDataCenterClusters().setItems(quotaClusterList);

                    }
                }), selectedDataCenter.getId());
                AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<StorageDomain> storageList = (ArrayList<StorageDomain>) returnValue;
                        if (storageList == null || storageList.size() == 0) {
                            qModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                            qModel.stopProgress();
                            return;
                        }
                        ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
                        QuotaStorage quotaStorage;
                        for (StorageDomain storage : storageList) {
                            if (!storage.getStorageDomainType().isDataDomain()) {
                                continue;
                            }
                            quotaStorage = new QuotaStorage();
                            quotaStorage.setStorageId(storage.getId());
                            quotaStorage.setStorageName(storage.getStorageName());
                            quotaStorage.setStorageSizeGB(null);
                            quotaStorage.setStorageSizeGBUsage((double) 0);
                            quotaStorageList.add(quotaStorage);
                        }
                        qModel.getAllDataCenterStorages().setItems(quotaStorageList);
                        qModel.stopProgress();
                    }
                }), selectedDataCenter.getId());

            }
        });

        UICommand command = new UICommand("OnCreateQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        qModel.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        qModel.getCommands().add(command);
    }

    private void cancel() {
        setWindow(null);
        setConfirmWindow(null);
    }

    private void onCreateQuotaInternal(boolean isClone) {
        QuotaModel model = (QuotaModel) getWindow();
        if (!model.validate()) {
            return;
        }
        Quota quota = (Quota) model.getEntity();
        quota.setQuotaName(model.getName().getEntity());
        quota.setDescription(model.getDescription().getEntity());
        quota.setStoragePoolId(model.getDataCenter().getSelectedItem().getId());

        quota.setGraceVdsGroupPercentage(model.getGraceClusterAsInteger());
        quota.setGraceStoragePercentage(model.getGraceStorageAsInteger());
        quota.setThresholdVdsGroupPercentage(model.getThresholdClusterAsInteger());
        quota.setThresholdStoragePercentage(model.getThresholdStorageAsInteger());

        if (model.getGlobalClusterQuota().getEntity()) {
            QuotaVdsGroup quotaVdsGroup;
            for (QuotaVdsGroup iter : (ArrayList<QuotaVdsGroup>) model.getQuotaClusters().getItems()) {
                quota.setGlobalQuotaVdsGroup(new QuotaVdsGroup());
                quota.getGlobalQuotaVdsGroup().setMemSizeMB(iter.getMemSizeMB());
                quota.getGlobalQuotaVdsGroup().setVirtualCpu(iter.getVirtualCpu());
                quota.getQuotaVdsGroups().clear();
            }
        } else {
            quota.setGlobalQuotaVdsGroup(null);
            ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
            QuotaVdsGroup quotaVdsGroup;
            for (QuotaVdsGroup iter : (ArrayList<QuotaVdsGroup>) model.getAllDataCenterClusters().getItems()) {
                quotaVdsGroup = iter;
                if (quotaVdsGroup.getMemSizeMB() != null) {
                    quotaClusterList.add(quotaVdsGroup);
                }
            }
            quota.setQuotaVdsGroups(quotaClusterList);
        }

        if (model.getGlobalStorageQuota().getEntity()) {
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : (ArrayList<QuotaStorage>) model.getQuotaStorages().getItems()) {
                quota.setGlobalQuotaStorage(new QuotaStorage());
                quota.getGlobalQuotaStorage().setStorageSizeGB(iter.getStorageSizeGB());
                quota.getQuotaStorages().clear();
            }
        } else {
            quota.setGlobalQuotaStorage(null);
            ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : (ArrayList<QuotaStorage>) model.getAllDataCenterStorages().getItems()) {
                quotaStorage = iter;
                if (quotaStorage.getStorageSizeGB() != null) {
                    quotaStorageList.add(quotaStorage);
                }
            }
            quota.setQuotaStorages(quotaStorageList);
        }

        Guid guid = quota.getId();

        QuotaCRUDParameters parameters = new QuotaCRUDParameters(quota);
        if (isClone) {
            parameters.setCopyPermissions(model.getCopyPermissions().getEntity());
            parameters.setQuotaId(quota.getId());
            quota.setId(Guid.Empty);
        }

        VdcActionType actionType = VdcActionType.AddQuota;
        if (!quota.getId().equals(Guid.Empty)) {
            actionType = VdcActionType.UpdateQuota;
        }
        Frontend.getInstance().runAction(actionType,
                parameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        setWindow(null);
                    }
                });

        quota.setId(guid);
    }

    private boolean hasUnlimitedSpecificQuota() {
        QuotaModel model = (QuotaModel) getWindow();
        if (model.getSpecificClusterQuota().getEntity()) {
            for (QuotaVdsGroup quotaVdsGroup : (ArrayList<QuotaVdsGroup>) model.getAllDataCenterClusters().getItems()) {
                if (QuotaVdsGroup.UNLIMITED_MEM.equals(quotaVdsGroup.getMemSizeMB())
                        || QuotaVdsGroup.UNLIMITED_VCPU.equals(quotaVdsGroup.getVirtualCpu())) {
                    return true;
                }
            }
        }

        if (model.getSpecificStorageQuota().getEntity()) {
            for (QuotaStorage quotaStorage : (ArrayList<QuotaStorage>) model.getAllDataCenterStorages().getItems()) {
                if (QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void editQuota(boolean isClone) {
        Quota outer_quota = (Quota) getSelectedItem();
        final QuotaModel qModel = new QuotaModel();
        qModel.getName().setEntity(outer_quota.getQuotaName());

        qModel.getGraceCluster().setEntity(outer_quota.getGraceVdsGroupPercentage());
        qModel.getThresholdCluster().setEntity(outer_quota.getThresholdVdsGroupPercentage());
        qModel.getGraceStorage().setEntity(outer_quota.getGraceStoragePercentage());
        qModel.getThresholdStorage().setEntity(outer_quota.getThresholdStoragePercentage());

        qModel.getDescription().setEntity(outer_quota.getDescription());
        qModel.setTitle(isClone ? ConstantsManager.getInstance().getConstants().cloneQuotaTitle()
                : ConstantsManager.getInstance().getConstants().editQuotaTitle());
        if (isClone) {
            qModel.setHelpTag(HelpTag.clone_quota);
            qModel.setHashName("clone_quota"); //$NON-NLS-1$
        }
        else {
            qModel.setHelpTag(HelpTag.edit_quota);
            qModel.setHashName("edit_quota"); //$NON-NLS-1$
        }

        UICommand command = null;

        if (!isClone) {
            command = new UICommand("OnCreateQuota", this); //$NON-NLS-1$
        } else {
            command = new UICommand("onCloneQuota", this); //$NON-NLS-1$
            qModel.getName().setEntity(COPY_OF + outer_quota.getQuotaName());
            qModel.getDescription().setEntity(""); //$NON-NLS-1$
            qModel.getCopyPermissions().setIsAvailable(true);
        }
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        qModel.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        qModel.getCommands().add(command);

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                final Quota quota = (Quota) ((VdcQueryReturnValue) returnValue).getReturnValue();
                qModel.setEntity(quota);
                if (quota.getGlobalQuotaVdsGroup() != null) {
                    QuotaVdsGroup cluster =
                            ((ArrayList<QuotaVdsGroup>) qModel.getQuotaClusters().getItems()).get(0);
                    cluster.setMemSizeMB(quota.getGlobalQuotaVdsGroup().getMemSizeMB());
                    cluster.setVirtualCpu(quota.getGlobalQuotaVdsGroup().getVirtualCpu());
                    cluster.setMemSizeMBUsage(quota.getGlobalQuotaVdsGroup().getMemSizeMBUsage());
                    cluster.setVirtualCpuUsage(quota.getGlobalQuotaVdsGroup().getVirtualCpuUsage());
                    qModel.getGlobalClusterQuota().setEntity(true);
                }
                if (quota.getGlobalQuotaStorage() != null) {
                    QuotaStorage storage = ((ArrayList<QuotaStorage>) qModel.getQuotaStorages().getItems()).get(0);
                    storage.setStorageSizeGB(quota.getGlobalQuotaStorage().getStorageSizeGB());
                    storage.setStorageSizeGBUsage(quota.getGlobalQuotaStorage().getStorageSizeGBUsage());
                    qModel.getGlobalStorageQuota().setEntity(true);
                }

                setWindow(qModel);
                qModel.startProgress(null);

                qModel.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        StoragePool selectedDataCenter = qModel.getDataCenter().getSelectedItem();
                        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this, new INewAsyncCallback() {

                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) returnValue;
                                if (clusterList == null || clusterList.size() == 0) {
                                    qModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaVdsGroup>());
                                    if (quota.getGlobalQuotaVdsGroup() == null) {
                                        qModel.getSpecificClusterQuota().setEntity(true);
                                    }
                                    return;
                                }
                                ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
                                QuotaVdsGroup quotaVdsGroup;
                                for (VDSGroup vdsGroup : clusterList) {
                                    quotaVdsGroup = new QuotaVdsGroup();
                                    quotaVdsGroup.setVdsGroupId(vdsGroup.getId());
                                    quotaVdsGroup.setVdsGroupName(vdsGroup.getName());
                                    quotaVdsGroup.setQuotaId(quota.getId());
                                    boolean containCluster = false;
                                    for (QuotaVdsGroup iter : quota.getQuotaVdsGroups()) {
                                        if (quotaVdsGroup.getVdsGroupId().equals(iter.getVdsGroupId())) {
                                            quotaVdsGroup.setQuotaVdsGroupId(iter.getQuotaVdsGroupId());
                                            quotaVdsGroup.setMemSizeMB(iter.getMemSizeMB());
                                            quotaVdsGroup.setVirtualCpu(iter.getVirtualCpu());
                                            quotaVdsGroup.setMemSizeMBUsage(iter.getMemSizeMBUsage());
                                            quotaVdsGroup.setVirtualCpuUsage(iter.getVirtualCpuUsage());
                                            containCluster = true;
                                            break;
                                        }
                                    }
                                    if (!containCluster) {
                                        quotaVdsGroup.setMemSizeMB(null);
                                        quotaVdsGroup.setVirtualCpu(null);
                                        quotaVdsGroup.setMemSizeMBUsage((long) 0);
                                        quotaVdsGroup.setVirtualCpuUsage(0);
                                    }
                                    quotaClusterList.add(quotaVdsGroup);
                                }
                                qModel.getAllDataCenterClusters().setItems(quotaClusterList);
                                if (quota.getGlobalQuotaVdsGroup() == null) {
                                    qModel.getSpecificClusterQuota().setEntity(true);
                                }
                            }
                        }), selectedDataCenter.getId());
                        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                                new INewAsyncCallback() {

                                    @Override
                                    public void onSuccess(Object model, Object returnValue) {
                                        ArrayList<StorageDomain> storageList = (ArrayList<StorageDomain>) returnValue;

                                        if (storageList == null || storageList.size() == 0) {
                                            qModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                                            if (quota.getGlobalQuotaStorage() == null) {
                                                qModel.getSpecificStorageQuota().setEntity(true);
                                            }
                                            qModel.stopProgress();
                                            return;
                                        }
                                        ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
                                        QuotaStorage quotaStorage;
                                        for (StorageDomain storage : storageList) {
                                            if (!storage.getStorageDomainType().isDataDomain()) {
                                                continue;
                                            }
                                            quotaStorage = new QuotaStorage();
                                            quotaStorage.setStorageId(storage.getId());
                                            quotaStorage.setStorageName(storage.getStorageName());
                                            quotaStorage.setQuotaId(quota.getId());
                                            boolean containStorage = false;
                                            for (QuotaStorage iter : quota.getQuotaStorages()) {
                                                if (quotaStorage.getStorageId().equals(iter.getStorageId())) {
                                                    quotaStorage.setQuotaStorageId(iter.getQuotaStorageId());
                                                    quotaStorage.setStorageSizeGB(iter.getStorageSizeGB());
                                                    quotaStorage.setStorageSizeGBUsage(iter.getStorageSizeGBUsage());
                                                    containStorage = true;
                                                    break;
                                                }
                                            }
                                            if (!containStorage) {
                                                quotaStorage.setStorageSizeGB(null);
                                                quotaStorage.setStorageSizeGBUsage(0.0);
                                            }
                                            quotaStorageList.add(quotaStorage);
                                        }
                                        qModel.getAllDataCenterStorages().setItems(quotaStorageList);
                                        if (quota.getGlobalQuotaStorage() == null) {
                                            qModel.getSpecificStorageQuota().setEntity(true);
                                        }
                                        qModel.stopProgress();
                                    }
                                }),
                                selectedDataCenter.getId());

                    }
                });

                ArrayList<StoragePool> dataCenterList = new ArrayList<StoragePool>();
                StoragePool dataCenter = new StoragePool();
                dataCenter.setId(quota.getStoragePoolId());
                dataCenter.setName(quota.getStoragePoolName());
                dataCenterList.add(dataCenter);
                qModel.getDataCenter().setItems(dataCenterList);
                qModel.getDataCenter().setSelectedItem(dataCenter);
                qModel.getDataCenter().setIsChangable(false);

            }
        };

        IdQueryParameters quotaParameters = new IdQueryParameters(outer_quota.getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetQuotaByQuotaId,
                quotaParameters,
                asyncQuery);

    }

    private void onCreateQuota() {
        if (hasUnlimitedSpecificQuota()) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .changeDCQuotaEnforcementModeTitle());
            confirmModel.setHelpTag(HelpTag.set_unlimited_specific_quota);
            confirmModel.setHashName("set_unlimited_specific_quota"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutToCreateUnlimitedSpecificQuotaMsg());

            UICommand tempVar = new UICommand("OnCreateQuotaInternal", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            tempVar2.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onCreateQuotaInternal(false);
        }
    }

    public void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();
        QuotaCRUDParameters crudParameters;
        for (Quota a : Linq.<Quota> cast(getSelectedItems()))
        {
            crudParameters = new QuotaCRUDParameters();
            crudParameters.setQuotaId(a.getId());
            prms.add(crudParameters);
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveQuota, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeQuotasTitle());
        model.setHelpTag(HelpTag.remove_quota);
        model.setHashName("remove_quota"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        for (Quota a : Linq.<Quota> cast(getSelectedItems()))
        {
            list.add(a.getQuotaName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getCreateCommand())) {
            createQuota();
        }
        else if (command.equals(getEditCommand())) {
            editQuota(false);
        }
        else if (command.getName().equals("OnCreateQuota")) { //$NON-NLS-1$
            onCreateQuota();
        }
        else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        }
        else if (command.equals(getRemoveCommand())) {
            remove();
        }
        else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemove();
        }
        else if (command.equals(getCloneCommand())) {
            editQuota(true);
        }
        else if (command.getName().equals("onCloneQuota")) { //$NON-NLS-1$
            onCreateQuotaInternal(true);
        }
        else if (command.getName().equals("OnCreateQuotaInternal")) { //$NON-NLS-1$
            setConfirmWindow(null);
            onCreateQuotaInternal(false);
        }
        else if (command.getName().equals("CancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
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
        search();
    }

    @Override
    protected String getListName() {
        return "QuotaListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("quota"); //$NON-NLS-1$
    }

    public UICommand getCloneCommand() {
        return cloneCommand;
    }

    public void setCloneCommand(UICommand cloneQuotaCommand) {
        this.cloneCommand = cloneQuotaCommand;
    }

}
