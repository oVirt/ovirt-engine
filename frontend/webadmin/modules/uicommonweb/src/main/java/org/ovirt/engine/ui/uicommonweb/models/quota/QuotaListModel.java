package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class QuotaListModel<E> extends ListWithSimpleDetailsModel<E, Quota> {

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

    private final QuotaClusterListModel clusterListModel;

    public QuotaClusterListModel getClusterListModel() {
        return clusterListModel;
    }

    private final QuotaStorageListModel storageListModel;

    public QuotaStorageListModel getStorageListModel() {
        return storageListModel;
    }

    private final QuotaVmListModel vmListModel;

    public QuotaVmListModel getVmListModel() {
        return vmListModel;
    }

    private final QuotaTemplateListModel templateListModel;

    public QuotaTemplateListModel getTemplateListModel() {
        return templateListModel;
    }

    private final QuotaUserListModel userListModel;

    public QuotaUserListModel getUserListModel() {
        return userListModel;
    }

    private final QuotaPermissionListModel permissionListModel;

    public QuotaPermissionListModel getPermissionListModel() {
        return permissionListModel;
    }

    private final QuotaEventListModel eventListModel;

    public QuotaEventListModel getEventListModel() {
        return eventListModel;
    }

    @Inject
    public QuotaListModel(final QuotaClusterListModel quotaClusterListModel,
            final QuotaStorageListModel quotaStorageListModel,
            final QuotaVmListModel quotaVmListModel,
            final QuotaTemplateListModel quotaTemplateListModel,
            final QuotaUserListModel quotaUserListModel,
            final QuotaPermissionListModel quotaPermissionListModel,
            final QuotaEventListModel quotaEventListModel) {
        this.clusterListModel = quotaClusterListModel;
        this.storageListModel = quotaStorageListModel;
        this.vmListModel = quotaVmListModel;
        this.templateListModel = quotaTemplateListModel;
        this.userListModel = quotaUserListModel;
        this.permissionListModel = quotaPermissionListModel;
        this.eventListModel = quotaEventListModel;

        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());
        setApplicationPlace(WebAdminApplicationPlaces.quotaMainPlace);

        setDefaultSearchString(SearchStringMapping.QUOTA_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
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

    private void setDetailList() {
        List<HasEntity<Quota>> list = new ArrayList<>();
        list.add(clusterListModel);
        list.add(storageListModel);
        list.add(vmListModel);
        list.add(templateListModel);
        list.add(userListModel);
        list.add(permissionListModel);
        list.add(eventListModel);

        setDetailModels(list);
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
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.Quota,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
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
        qModel.startProgress();

        if (populateDataCenter) {
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenterList -> {
                if (dataCenterList == null || dataCenterList.size() == 0) {
                    return;
                }
                QuotaModel quotaModel = (QuotaModel) getWindow();
                quotaModel.getDataCenter().setItems(dataCenterList);
                quotaModel.getDataCenter().setSelectedItem(dataCenterList.get(0));

            }));
        }

        qModel.getDataCenter().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            StoragePool selectedDataCenter = qModel.getDataCenter().getSelectedItem();
            if (selectedDataCenter == null) {
                return;
            }
            AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(clusterList -> {
                if (clusterList == null || clusterList.size() == 0) {
                    qModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaCluster>());
                    return;
                }
                ArrayList<QuotaCluster> quotaClusterList = new ArrayList<>();
                QuotaCluster quotaCluster;
                for (Cluster cluster : clusterList) {
                    quotaCluster = new QuotaCluster();
                    quotaCluster.setClusterId(cluster.getId());
                    quotaCluster.setClusterName(cluster.getName());
                    quotaCluster.setMemSizeMB(null);
                    quotaCluster.setMemSizeMBUsage((long) 0);
                    quotaCluster.setVirtualCpu(null);
                    quotaCluster.setVirtualCpuUsage(0);
                    quotaClusterList.add(quotaCluster);
                }
                qModel.getAllDataCenterClusters().setItems(quotaClusterList);

            }), selectedDataCenter.getId());
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(storageList -> {
                if (storageList == null || storageList.size() == 0) {
                    qModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                    qModel.stopProgress();
                    return;
                }
                ArrayList<QuotaStorage> quotaStorageList = new ArrayList<>();
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
            }), selectedDataCenter.getId());

        });

        UICommand command = UICommand.createDefaultOkUiCommand("OnCreateQuota", this); //$NON-NLS-1$
        qModel.getCommands().add(command);
        qModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
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
        Quota quota = model.getEntity();
        quota.setQuotaName(model.getName().getEntity());
        quota.setDescription(model.getDescription().getEntity());
        quota.setStoragePoolId(model.getDataCenter().getSelectedItem().getId());

        quota.setGraceClusterPercentage(model.getGraceClusterAsInteger());
        quota.setGraceStoragePercentage(model.getGraceStorageAsInteger());
        quota.setThresholdClusterPercentage(model.getThresholdClusterAsInteger());
        quota.setThresholdStoragePercentage(model.getThresholdStorageAsInteger());

        if (model.getGlobalClusterQuota().getEntity()) {
            QuotaCluster quotaCluster;
            for (QuotaCluster iter : model.getQuotaClusters().getItems()) {
                quota.setGlobalQuotaCluster(new QuotaCluster());
                quota.getGlobalQuotaCluster().setMemSizeMB(iter.getMemSizeMB());
                quota.getGlobalQuotaCluster().setVirtualCpu(iter.getVirtualCpu());
                quota.getQuotaClusters().clear();
            }
        } else {
            quota.setGlobalQuotaCluster(null);
            ArrayList<QuotaCluster> quotaClusterList = new ArrayList<>();
            QuotaCluster quotaCluster;
            for (QuotaCluster iter : model.getAllDataCenterClusters().getItems()) {
                quotaCluster = iter;
                if (quotaCluster.getMemSizeMB() != null) {
                    quotaClusterList.add(quotaCluster);
                }
            }
            quota.setQuotaClusters(quotaClusterList);
        }

        if (model.getGlobalStorageQuota().getEntity()) {
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : model.getQuotaStorages().getItems()) {
                quota.setGlobalQuotaStorage(new QuotaStorage());
                quota.getGlobalQuotaStorage().setStorageSizeGB(iter.getStorageSizeGB());
                quota.getQuotaStorages().clear();
            }
        } else {
            quota.setGlobalQuotaStorage(null);
            ArrayList<QuotaStorage> quotaStorageList = new ArrayList<>();
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : model.getAllDataCenterStorages().getItems()) {
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

        ActionType actionType = ActionType.AddQuota;
        if (!quota.getId().equals(Guid.Empty)) {
            actionType = ActionType.UpdateQuota;
        }
        Frontend.getInstance().runAction(actionType,
                parameters,
                result -> setWindow(null));

        quota.setId(guid);
    }

    private boolean hasUnlimitedSpecificQuota() {
        QuotaModel model = (QuotaModel) getWindow();
        if (model.getSpecificClusterQuota().getEntity()) {
            for (QuotaCluster quotaCluster : model.getAllDataCenterClusters().getItems()) {
                if (QuotaCluster.UNLIMITED_MEM.equals(quotaCluster.getMemSizeMB())
                        || QuotaCluster.UNLIMITED_VCPU.equals(quotaCluster.getVirtualCpu())) {
                    return true;
                }
            }
        }

        if (model.getSpecificStorageQuota().getEntity()) {
            for (QuotaStorage quotaStorage : model.getAllDataCenterStorages().getItems()) {
                if (QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void editQuota(boolean isClone) {
        Quota outer_quota = getSelectedItem();
        final QuotaModel qModel = new QuotaModel();
        qModel.getName().setEntity(outer_quota.getQuotaName());

        qModel.getGraceCluster().setEntity(outer_quota.getGraceClusterPercentage());
        qModel.getThresholdCluster().setEntity(outer_quota.getThresholdClusterPercentage());
        qModel.getGraceStorage().setEntity(outer_quota.getGraceStoragePercentage());
        qModel.getThresholdStorage().setEntity(outer_quota.getThresholdStoragePercentage());

        qModel.getDescription().setEntity(outer_quota.getDescription());
        qModel.setTitle(isClone ? ConstantsManager.getInstance().getConstants().cloneQuotaTitle()
                : ConstantsManager.getInstance().getConstants().editQuotaTitle());
        if (isClone) {
            qModel.setHelpTag(HelpTag.clone_quota);
            qModel.setHashName("clone_quota"); //$NON-NLS-1$
        } else {
            qModel.setHelpTag(HelpTag.edit_quota);
            qModel.setHashName("edit_quota"); //$NON-NLS-1$
        }

        UICommand command;

        if (!isClone) {
            command = UICommand.createDefaultOkUiCommand("OnCreateQuota", this); //$NON-NLS-1$
        } else {
            command = UICommand.createDefaultOkUiCommand("onCloneQuota", this); //$NON-NLS-1$
            qModel.getName().setEntity(COPY_OF + outer_quota.getQuotaName());
            qModel.getDescription().setEntity(""); //$NON-NLS-1$
            qModel.getCopyPermissions().setIsAvailable(true);
        }
        qModel.getCommands().add(command);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        qModel.getCommands().add(cancelCommand);

        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            final Quota quota = returnValue.getReturnValue();
            qModel.setEntity(quota);
            if (quota.getGlobalQuotaCluster() != null) {
                QuotaCluster cluster =
                        ((ArrayList<QuotaCluster>) qModel.getQuotaClusters().getItems()).get(0);
                cluster.setMemSizeMB(quota.getGlobalQuotaCluster().getMemSizeMB());
                cluster.setVirtualCpu(quota.getGlobalQuotaCluster().getVirtualCpu());
                cluster.setMemSizeMBUsage(quota.getGlobalQuotaCluster().getMemSizeMBUsage());
                cluster.setVirtualCpuUsage(quota.getGlobalQuotaCluster().getVirtualCpuUsage());
                qModel.getGlobalClusterQuota().setEntity(true);
            }
            if (quota.getGlobalQuotaStorage() != null) {
                QuotaStorage storage = ((ArrayList<QuotaStorage>) qModel.getQuotaStorages().getItems()).get(0);
                storage.setStorageSizeGB(quota.getGlobalQuotaStorage().getStorageSizeGB());
                storage.setStorageSizeGBUsage(quota.getGlobalQuotaStorage().getStorageSizeGBUsage());
                qModel.getGlobalStorageQuota().setEntity(true);
            }

            setWindow(qModel);
            qModel.startProgress();

            qModel.getDataCenter().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
                StoragePool selectedDataCenter = qModel.getDataCenter().getSelectedItem();
                AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(clusterList -> {
                    if (clusterList == null || clusterList.size() == 0) {
                        qModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaCluster>());
                        if (quota.getGlobalQuotaCluster() == null) {
                            qModel.getSpecificClusterQuota().setEntity(true);
                        }
                        return;
                    }
                    ArrayList<QuotaCluster> quotaClusterList = new ArrayList<>();
                    QuotaCluster quotaCluster;
                    for (Cluster cluster : clusterList) {
                        quotaCluster = new QuotaCluster();
                        quotaCluster.setClusterId(cluster.getId());
                        quotaCluster.setClusterName(cluster.getName());
                        quotaCluster.setQuotaId(quota.getId());
                        boolean containCluster = false;
                        for (QuotaCluster iter : quota.getQuotaClusters()) {
                            if (quotaCluster.getClusterId().equals(iter.getClusterId())) {
                                quotaCluster.setQuotaClusterId(iter.getQuotaClusterId());
                                quotaCluster.setMemSizeMB(iter.getMemSizeMB());
                                quotaCluster.setVirtualCpu(iter.getVirtualCpu());
                                quotaCluster.setMemSizeMBUsage(iter.getMemSizeMBUsage());
                                quotaCluster.setVirtualCpuUsage(iter.getVirtualCpuUsage());
                                containCluster = true;
                                break;
                            }
                        }
                        if (!containCluster) {
                            quotaCluster.setMemSizeMB(null);
                            quotaCluster.setVirtualCpu(null);
                            quotaCluster.setMemSizeMBUsage((long) 0);
                            quotaCluster.setVirtualCpuUsage(0);
                        }
                        quotaClusterList.add(quotaCluster);
                    }
                    qModel.getAllDataCenterClusters().setItems(quotaClusterList);
                    if (quota.getGlobalQuotaCluster() == null) {
                        qModel.getSpecificClusterQuota().setEntity(true);
                    }
                }), selectedDataCenter.getId());
                AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                                storageList -> {
                                    if (storageList == null || storageList.size() == 0) {
                                        qModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                                        if (quota.getGlobalQuotaStorage() == null) {
                                            qModel.getSpecificStorageQuota().setEntity(true);
                                        }
                                        qModel.stopProgress();
                                        return;
                                    }
                                    ArrayList<QuotaStorage> quotaStorageList = new ArrayList<>();
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
                                }),
                        selectedDataCenter.getId());

            });

            ArrayList<StoragePool> dataCenterList = new ArrayList<>();
            StoragePool dataCenter = new StoragePool();
            dataCenter.setId(quota.getStoragePoolId());
            dataCenter.setName(quota.getStoragePoolName());
            dataCenterList.add(dataCenter);
            qModel.getDataCenter().setItems(dataCenterList);
            qModel.getDataCenter().setSelectedItem(dataCenter);
            qModel.getDataCenter().setIsChangeable(false);

        });

        IdQueryParameters quotaParameters = new IdQueryParameters(outer_quota.getId());
        Frontend.getInstance().runQuery(QueryType.GetQuotaByQuotaId,
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
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onCreateQuotaInternal(false);
        }
    }

    public void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> prms = new ArrayList<>();
        for (Quota a : getSelectedItems()) {
            IdParameters idParameters = new IdParameters(a.getId());
            prms.add(idParameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveQuota, prms,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeQuotasTitle());
        model.setHelpTag(HelpTag.remove_quota);
        model.setHashName("remove_quota"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (Quota a : getSelectedItems()) {
            list.add(a.getQuotaName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getCreateCommand())) {
            createQuota();
        } else if (command.equals(getEditCommand())) {
            editQuota(false);
        } else if (command.getName().equals("OnCreateQuota")) { //$NON-NLS-1$
            onCreateQuota();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        } else if (command.equals(getRemoveCommand())) {
            remove();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemove();
        } else if (command.equals(getCloneCommand())) {
            editQuota(true);
        } else if (command.getName().equals("onCloneQuota")) { //$NON-NLS-1$
            onCreateQuotaInternal(true);
        } else if (command.getName().equals("OnCreateQuotaInternal")) { //$NON-NLS-1$
            setConfirmWindow(null);
            onCreateQuotaInternal(false);
        } else if (command.getName().equals("CancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
        }
    }

    @Override
    protected String getListName() {
        return "QuotaListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("quota"); //$NON-NLS-1$
    }

    public UICommand getCloneCommand() {
        return cloneCommand;
    }

    public void setCloneCommand(UICommand cloneQuotaCommand) {
        this.cloneCommand = cloneQuotaCommand;
    }

}
