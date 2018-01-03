package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncCallback;
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
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MultipleHostsModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.NewSharedMacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;

public class ClusterListModel<E> extends ListWithSimpleDetailsModel<E, Cluster> {

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final UIMessages messages = ConstantsManager.getInstance().getMessages();

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

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand() {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value) {
        privateGuideCommand = value;
    }

    private UICommand resetEmulatedMachineCommand;

    public UICommand getResetEmulatedMachineCommand() {
        return resetEmulatedMachineCommand;
    }

    private void setResetEmulatedMachineCommand(UICommand value) {
        resetEmulatedMachineCommand = value;
    }

    private UICommand privateAddMultipleHostsCommand;

    public UICommand getAddMultipleHostsCommand() {
        return privateAddMultipleHostsCommand;
    }

    private void setAddMultipleHostsCommand(UICommand value) {
        privateAddMultipleHostsCommand = value;
    }

    private UICommand addMacPoolCommand;

    public UICommand getAddMacPoolCommand() {
        return addMacPoolCommand;
    }

    private void setAddMacPoolCommand(UICommand addMacPoolCommand) {
        this.addMacPoolCommand = addMacPoolCommand;
    }

    private Object privateGuideContext;

    public Object getGuideContext() {
        return privateGuideContext;
    }

    public void setGuideContext(Object value) {
        privateGuideContext = value;
    }

    private final ClusterServiceModel serviceModel;

    public ClusterServiceModel getServiceModel() {
        return serviceModel;
    }

    private final ClusterVmListModel vmListModel;

    public ClusterVmListModel getVmListModel() {
        return vmListModel;
    }

    private final ClusterGlusterHookListModel glusterHookListModel;

    public ClusterGlusterHookListModel getGlusterHookListModel() {
        return glusterHookListModel;
    }

    private final ClusterAffinityGroupListModel affinityGroupListModel;

    public ClusterAffinityGroupListModel getAffinityGroupListModel() {
        return affinityGroupListModel;
    }

    private final ClusterAffinityLabelListModel affinityLabelListModel;

    public ClusterAffinityLabelListModel getAffinityLabelListModel() {
        return affinityLabelListModel;
    }

    private final CpuProfileListModel cpuProfileListModel;

    public CpuProfileListModel getCpuProfileListModel() {
        return cpuProfileListModel;
    }

    private final ClusterGeneralModel generalModel;

    public ClusterGeneralModel getGeneralModel() {
        return generalModel;
    }

    private final ClusterNetworkListModel networkListModel;

    public ClusterNetworkListModel getNetworkListModel() {
        return networkListModel;
    }

    private final ClusterHostListModel hostListModel;

    public ClusterHostListModel getHostListModel() {
        return hostListModel;
    }

    private final PermissionListModel<Cluster> permissionListModel;

    public PermissionListModel<Cluster> getPermissionListModel() {
        return permissionListModel;
    }

    @Inject
    public ClusterListModel(final ClusterVmListModel clusterVmListModel,
            final ClusterServiceModel clusterServiceModel,
            final ClusterGlusterHookListModel clusterGlusterHookListModel,
            final ClusterAffinityGroupListModel clusterAffinityGroupListModel,
            final CpuProfileListModel cpuProfileListModel,
            final ClusterGeneralModel clusterGeneralModel,
            final ClusterNetworkListModel clusterNetworkListModel,
            final ClusterHostListModel clusterHostListModel,
            final PermissionListModel<Cluster> permissionListModel,
            final ClusterAffinityLabelListModel clusterAffinityLabelListModel) {
        this.vmListModel = clusterVmListModel;
        this.serviceModel = clusterServiceModel;
        this.glusterHookListModel = clusterGlusterHookListModel;
        this.affinityGroupListModel = clusterAffinityGroupListModel;
        this.cpuProfileListModel = cpuProfileListModel;
        this.affinityLabelListModel = clusterAffinityLabelListModel;
        this.generalModel = clusterGeneralModel;
        this.networkListModel = clusterNetworkListModel;
        this.hostListModel = clusterHostListModel;
        this.permissionListModel = permissionListModel;
        setDetailList();

        setTitle(ConstantsManager.getInstance().getConstants().clustersTitle());
        setHelpTag(HelpTag.clusters);
        setApplicationPlace(WebAdminApplicationPlaces.clusterMainPlace);
        setHashName("clusters"); //$NON-NLS-1$

        setDefaultSearchString(SearchStringMapping.CLUSTER_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setAddMacPoolCommand(new UICommand("AddMacPool", this)); //$NON-NLS-1$
        getAddMacPoolCommand().setTitle(ConstantsManager.getInstance().getConstants().addMacPoolButton());
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$
        setResetEmulatedMachineCommand(new UICommand("ResetEmulatedMachine", this)); //$NON-NLS-1$
        setAddMultipleHostsCommand(new UICommand("AddHosts", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void guide() {
        ClusterGuideModel model = new ClusterGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newClusterGuideMeTitle());
        model.setHelpTag(HelpTag.new_cluster___guide_me);
        model.setHashName("new_cluster_-_guide_me"); //$NON-NLS-1$

        if (getGuideContext() == null) {
            Cluster cluster = getSelectedItem();
            setGuideContext(cluster.getId());
        }

        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                returnValue -> {
                    ClusterGuideModel clusterGuideModel = (ClusterGuideModel) getWindow();
                    clusterGuideModel.setEntity(returnValue);

                    UICommand tempVar = new UICommand("Cancel", ClusterListModel.this); //$NON-NLS-1$
                    tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                    tempVar.setIsDefault(true);
                    tempVar.setIsCancel(true);
                    clusterGuideModel.getCommands().add(tempVar);
                }), (Guid) getGuideContext());
    }

    private void setDetailList() {
        List<HasEntity<Cluster>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(networkListModel);
        list.add(hostListModel);
        list.add(vmListModel);
        list.add(serviceModel);
        list.add(glusterHookListModel);
        list.add(cpuProfileListModel);
        list.add(permissionListModel);
        list.add(affinityGroupListModel);
        list.add(affinityLabelListModel);
        setDetailModels(list);
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        Cluster cluster = getSelectedItem();
        boolean clusterSupportsVirtService = cluster != null && cluster.supportsVirtService();
        boolean clusterSupportsGlusterService = cluster != null && cluster.supportsGlusterService();

        getVmListModel().setIsAvailable(clusterSupportsVirtService);
        getServiceModel().setIsAvailable(clusterSupportsGlusterService);
        getGlusterHookListModel().setIsAvailable(clusterSupportsGlusterService);
        getAffinityGroupListModel().setIsAvailable(clusterSupportsVirtService);
        getCpuProfileListModel().setIsAvailable(clusterSupportsVirtService);
        getAffinityLabelListModel().setIsAvailable(clusterSupportsVirtService);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("cluster"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.Cluster,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        ClusterModel clusterModel = new ClusterModel();
        clusterModel.setAddMacPoolCommand(addMacPoolCommand);
        clusterModel.init(false);
        setWindow(clusterModel);
        clusterModel.setTitle(ConstantsManager.getInstance().getConstants().newClusterTitle());
        clusterModel.setHelpTag(HelpTag.new_cluster);
        clusterModel.setHashName("new_cluster"); //$NON-NLS-1$
        clusterModel.setIsNew(true);
        clusterModel.getMigrationBandwidthLimitType().setItems(Arrays.asList(MigrationBandwidthLimitType.values()));
        clusterModel.getMigrationBandwidthLimitType().setSelectedItem(MigrationBandwidthLimitType.DEFAULT);

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {
            ClusterModel cModel = (ClusterModel) getWindow();

            cModel.getDataCenter().setItems(dataCenters, Linq.firstOrNull(dataCenters));
            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", ClusterListModel.this); //$NON-NLS-1$
            cModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", ClusterListModel.this); //$NON-NLS-1$
            cModel.getCommands().add(tempVar2);
        }));
        clusterModel.refreshMigrationPolicies();
    }

    public void edit() {
        final Cluster cluster = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final ClusterModel clusterModel = new ClusterModel();
        clusterModel.setAddMacPoolCommand(addMacPoolCommand);
        clusterModel.setEntity(cluster);
        clusterModel.init(true);
        clusterModel.getEnableTrustedService().setEntity(cluster.supportsTrustedService());
        clusterModel.getEnableHaReservation().setEntity(cluster.supportsHaReservation());
        clusterModel.getEnableOptionalReason().setEntity(cluster.isOptionalReasonRequired());
        clusterModel.getEnableHostMaintenanceReason().setEntity(cluster.isMaintenanceReasonRequired());
        setWindow(clusterModel);
        clusterModel.setTitle(ConstantsManager.getInstance().getConstants().editClusterTitle());
        clusterModel.setHelpTag(HelpTag.edit_cluster);
        clusterModel.setHashName("edit_cluster"); //$NON-NLS-1$
        clusterModel.setOriginalName(cluster.getName());
        clusterModel.getName().setEntity(cluster.getName());
        clusterModel.getSwitchType().setSelectedItem(cluster.getRequiredSwitchTypeForCluster());
        clusterModel.getFirewallType().setSelectedItem(cluster.getFirewallType());
        clusterModel.getEnableOvirtService().setEntity(cluster.supportsVirtService());
        clusterModel.getEnableOvirtService().setIsChangeable(true);
        clusterModel.getEnableGlusterService().setEntity(cluster.supportsGlusterService());
        clusterModel.getEnableGlusterService().setIsChangeable(true);
        clusterModel.getEnableKsm().setEntity(cluster.isEnableKsm());
        clusterModel.setKsmPolicyForNuma(cluster.isKsmMergeAcrossNumaNodes());
        clusterModel.getEnableBallooning().setEntity(cluster.isEnableBallooning());
        clusterModel.getArchitecture().setSelectedItem(cluster.getArchitecture());
        clusterModel.getSerialNumberPolicy().setSelectedSerialNumberPolicy(cluster.getSerialNumberPolicy());
        clusterModel.getSerialNumberPolicy().getCustomSerialNumber().setEntity(cluster.getCustomSerialNumber());
        clusterModel.getAutoConverge().setSelectedItem(cluster.getAutoConverge());
        clusterModel.getMigrateCompressed().setSelectedItem(cluster.getMigrateCompressed());
        clusterModel.getGlusterTunedProfile().setSelectedItem(cluster.getGlusterTunedProfile());
        clusterModel.getGlusterTunedProfile().setIsChangeable(cluster.getClusterHostsAndVms().getHosts() == 0);
        clusterModel.getMigrationBandwidthLimitType().setItems(Arrays.asList(MigrationBandwidthLimitType.values()));
        clusterModel.getMigrationBandwidthLimitType().setSelectedItem(cluster.getMigrationBandwidthLimitType() != null
                ? cluster.getMigrationBandwidthLimitType()
                : MigrationBandwidthLimitType.DEFAULT);
        clusterModel.getCustomMigrationNetworkBandwidth().setEntity(cluster.getCustomMigrationNetworkBandwidth());

        if (cluster.supportsTrustedService()) {
            clusterModel.getEnableGlusterService().setIsChangeable(false);
        }
        if (cluster.supportsVirtService()&& !cluster.supportsGlusterService()) {
            clusterModel.getEnableTrustedService().setIsChangeable(true);
        }
        else {
            clusterModel.getEnableTrustedService().setIsChangeable(false);
        }

        clusterModel.getOptimizeForSpeed()
                .setEntity(OptimizationType.OPTIMIZE_FOR_SPEED == cluster.getOptimizationType());
        clusterModel.getAllowOverbooking()
                .setEntity(OptimizationType.ALLOW_OVERBOOKING == cluster.getOptimizationType());

        AsyncDataProvider.getInstance().getAllowClusterWithVirtGlusterEnabled(new AsyncQuery<>(isVirtGlusterAllowed -> {
                    AsyncDataProvider.getInstance().getVolumeList(clusterModel.asyncQuery(volumes -> {
                        if (volumes.size() > 0) {
                            clusterModel.getEnableGlusterService().setIsChangeable(false);
                            if (!isVirtGlusterAllowed) {
                                clusterModel.getEnableOvirtService().setIsChangeable(false);
                            }
                        }
                    }), cluster.getName());
                    if (cluster.getClusterHostsAndVms().getVms() > 0) {
                        clusterModel.getEnableOvirtService().setIsChangeable(false);
                        if (!isVirtGlusterAllowed) {
                            clusterModel.getEnableGlusterService().setIsChangeable(false);
                        }
                    }
                    if (cluster.getClusterHostsAndVms().getHosts() > 0) {
                        clusterModel.getEnableTrustedService().setIsChangeable(false);
                        clusterModel.getEnableTrustedService().setChangeProhibitionReason(
                                ConstantsManager.getInstance()
                                        .getConstants()
                                        .trustedServiceDisabled());
                    }
        }));

        clusterModel.refreshMigrationPolicies();
        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        clusterModel.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        clusterModel.getCommands().add(tempVar2);
    }

    private void addMacPool(final ClusterModel clusterModel) {
        SharedMacPoolModel macPoolModel = new NewSharedMacPoolModel.ClosingWithSetConfirmWindow(this) {
            @Override
            protected void onActionSucceeded(Guid macPoolId) {
                MacPool macPool = getEntity();
                macPool.setId(macPoolId);
                Collection<MacPool> macPools = new ArrayList<>(clusterModel.getMacPoolListModel().getItems());
                macPools.add(macPool);
                clusterModel.getMacPoolListModel().setItems(macPools);
                clusterModel.getMacPoolListModel().setSelectedItem(macPool);
                ClusterListModel.this.setConfirmWindow(null);
            }
        };

        macPoolModel.setEntity(new MacPool());
        setConfirmWindow(macPoolModel);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeClusterTitle());
        model.setHelpTag(HelpTag.remove_cluster);
        model.setHashName("remove_cluster"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (Cluster a : getSelectedItems()) {
            list.add(a.getName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void resetEmulatedMachine() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().resetClusterEmulatedMachineTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().resetClusterEmulatedMachineMessage());
        model.setHelpTag(HelpTag.reset_emulated_machine_cluster);
        model.setHashName("reset_cluster_emulated_machine"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (Cluster cluster : getSelectedItems()) {
            list.add(cluster.getName());
        }
        model.setItems(list);

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnResetClusterEmulatedMachine", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public void onResetClusterEmulatedMachine() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> prms = new ArrayList<>();
        for (Cluster cluster : getSelectedItems()) {
            ManagementNetworkOnClusterOperationParameters currentParam = new ManagementNetworkOnClusterOperationParameters(cluster);
            currentParam.setForceResetEmulatedMachine(true);
            prms.add(currentParam);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.UpdateCluster, prms,
                result -> {

                    model.stopProgress();
                    cancel();

                });
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> prms = new ArrayList<>();
        for (Object a : getSelectedItems()) {
            prms.add(new ClusterParametersBase(((Cluster) a).getId()));
        }

        selectNextItem();
        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveCluster, prms,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    if (result.getReturnValue().stream().anyMatch(rv -> !rv.isValid())) {
                        restorePreviousSelectedItem();
                    }
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    public void onSave() {
        ClusterModel model = (ClusterModel) getWindow();

        if (model.getIsNew()) {
            onPreSaveInternal(model);
        }
        else {
            onSaveConfirmCV(model);
        }
    }

    private void onSaveConfirmCV(ClusterModel model) {
        if (!model.getVersion().getSelectedItem().equals(getSelectedItem().getCompatibilityVersion())) {
            final ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(constants.changeClusterCompatibilityVersionTitle());
            confirmModel.setHelpTag(HelpTag.change_cluster_compatibility_version);
            confirmModel.setHashName("change_cluster_compatibility_version"); //$NON-NLS-1$
            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveConfirmCpuThreads", this); //$NON-NLS-1$
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
            getConfirmWindow().getCommands().add(tempVar2);

            checkForActiveVms(model, confirmModel);
        } else {
            onSaveConfirmCpuThreads();
        }
    }

    private void checkForActiveVms(ClusterModel model, final ConfirmationModel confirmModel) {
        Guid clusterId = model.getEntity().getId();
        Frontend.getInstance().runQuery(QueryType.GetNumberOfActiveVmsInClusterByClusterId,
                new IdQueryParameters(clusterId), new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
                    Integer numOfActiveVms = returnValue.getReturnValue();
                    if (numOfActiveVms != 0) {
                        confirmModel.setMessage(messages.thereAreActiveVMsRequiringRestart(numOfActiveVms));
                    }
                    checkForNonResponsiveHosts(confirmModel);
        }));
    }

    private void onSaveConfirmCpuThreads() {
        ClusterModel model = (ClusterModel) getWindow();

        // cancel confirm window if there is one
        cancelConfirmation();

        // CPU thread support is being turned off either explicitly or via version change
        if (!model.getVersionSupportsCpuThreads().getEntity() && model.getCountThreadsAsCores().getEntity()
                && getSelectedItem().getCountThreadsAsCores()) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .disableClusterCpuThreadSupportTitle());
            confirmModel.setHelpTag(HelpTag.disable_cpu_thread_support);
            confirmModel.setHashName("disable_cpu_thread_support"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeClusterCpuThreadSupportMsg());

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveConfirmGenericWarnings", this); //$NON-NLS-1$
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onSaveConfirmGenericWarnings();
        }
    }

    private void onSaveConfirmGenericWarnings() {
        ClusterModel model = (ClusterModel) getWindow();
        cancelConfirmation();

        Cluster cluster = buildCluster(model);
        AsyncDataProvider.getInstance().getClusterEditWarnings(new AsyncQuery<>(warnings -> {
            if (!warnings.isEmpty()) {
                ClusterWarningsModel confirmWindow = new ClusterWarningsModel();
                confirmWindow.init(warnings);
                confirmWindow.getCommands().add(UICommand.createDefaultOkUiCommand("OnSaveInternal", ClusterListModel.this)); //$NON-NLS-1$
                confirmWindow.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", ClusterListModel.this)); //$NON-NLS-1$

                setConfirmWindow(confirmWindow);
            } else {
                onSaveInternal();
            }

        }), model.getClusterId(), cluster);
    }

    public void onPreSaveInternal(ClusterModel model) {
        if (model.getIsImportGlusterConfiguration().getEntity()) {
            fetchAndImportClusterHosts(model);
        }
        else {
            onSaveInternal();
        }
    }

    public void onSaveInternal() {
        ClusterModel model = (ClusterModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        // cancel confirm window if there is
        cancelConfirmation();

        onSaveInternalWithModel(model);
    }

    private Cluster buildCluster(ClusterModel model) {
        Cluster cluster = model.getIsNew() ? new Cluster() : (Cluster) Cloner.clone(getSelectedItem());

        Version version = model.getVersion().getSelectedItem();

        cluster.setName(model.getName().getEntity());
        cluster.setDescription(model.getDescription().getEntity());
        cluster.setComment(model.getComment().getEntity());
        cluster.setStoragePoolId(model.getDataCenter().getSelectedItem().getId());
        if (model.getCPU().getSelectedItem() != null) {
            cluster.setCpuName(model.getCPU().getSelectedItem().getCpuName());
        }
        cluster.setMaxVdsMemoryOverCommit(model.getMemoryOverCommit());
        cluster.setCountThreadsAsCores(Boolean.TRUE.equals(model.getVersionSupportsCpuThreads().getEntity())
                && Boolean.TRUE.equals(model.getCountThreadsAsCores().getEntity()));
        cluster.setEnableKsm(Boolean.TRUE.equals(model.getEnableKsm().getEntity()));
        cluster.setKsmMergeAcrossNumaNodes(model.getKsmPolicyForNuma());
        cluster.setEnableBallooning(Boolean.TRUE.equals(model.getEnableBallooning().getEntity()));
        cluster.setTransparentHugepages(true); //$NON-NLS-1$
        cluster.setCompatibilityVersion(version);
        cluster.setRequiredSwitchTypeForCluster(model.getSwitchType().getSelectedItem());
        cluster.setFirewallType(model.getFirewallType().getSelectedItem());
        if (model.getDefaultNetworkProvider().getSelectedItem() != null) {
            cluster.setDefaultNetworkProviderId(model.getDefaultNetworkProvider().getSelectedItem().getId());
        }
        cluster.setMigrateOnError(model.getMigrateOnErrorOption());
        cluster.setVirtService(model.getEnableOvirtService().getEntity());
        cluster.setGlusterService(model.getEnableGlusterService().getEntity());
        for (AdditionalFeature feature : model.getAdditionalClusterFeatures().getSelectedItem()) {
            cluster.getAddtionalFeaturesSupported().add(new SupportedAdditionalClusterFeature(cluster.getId(),
                    true,
                    feature));
        }
        cluster.setTrustedService(model.getEnableTrustedService().getEntity());
        cluster.setHaReservation(model.getEnableHaReservation().getEntity());
        cluster.setOptionalReasonRequired(model.getEnableOptionalReason().getEntity());
        cluster.setMaintenanceReasonRequired(model.getEnableHostMaintenanceReason().getEntity());
        cluster.setClusterPolicyId(model.getClusterPolicy().getSelectedItem().getId());
        cluster.setClusterPolicyProperties(KeyValueModel.convertProperties(model.getCustomPropertySheet().serialize()));
        if (model.getOptimizeForSpeed().getEntity()) {
            cluster.setOptimizationType(OptimizationType.OPTIMIZE_FOR_SPEED);
        } else if (model.getAllowOverbooking().getEntity()) {
            cluster.setOptimizationType(OptimizationType.ALLOW_OVERBOOKING);
        } else {
            cluster.setOptimizationType(OptimizationType.NONE);
        }

        if (model.getCPU().getSelectedItem() == null) {
            cluster.setArchitecture(model.getArchitecture().getSelectedItem());
        } else {
            cluster.setArchitecture(null);
        }

        if (model.getSpiceProxyEnabled().getEntity()) {
            cluster.setSpiceProxy(model.getSpiceProxy().getEntity());
        }

        if (model.getMigrationPolicies().getSelectedItem() != null) {
            cluster.setMigrationPolicyId(model.getMigrationPolicies().getSelectedItem().getId());
        }

        cluster.getFencingPolicy().setFencingEnabled(model.getFencingEnabledModel().getEntity());
        cluster.getFencingPolicy().setSkipFencingIfSDActive(model.getSkipFencingIfSDActiveEnabled().getEntity());
        cluster.getFencingPolicy().setSkipFencingIfConnectivityBroken(model.getSkipFencingIfConnectivityBrokenEnabled().getEntity());
        cluster.getFencingPolicy().setHostsWithBrokenConnectivityThreshold(model.getHostsWithBrokenConnectivityThreshold().getSelectedItem().intValue());
        cluster.getFencingPolicy().setSkipFencingIfGlusterBricksUp(model.getSkipFencingIfGlusterBricksUp().getEntity());
        cluster.getFencingPolicy().setSkipFencingIfGlusterQuorumNotMet(model.getSkipFencingIfGlusterQuorumNotMet().getEntity());

        cluster.setSerialNumberPolicy(model.getSerialNumberPolicy().getSelectedSerialNumberPolicy());
        cluster.setCustomSerialNumber(model.getSerialNumberPolicy().getCustomSerialNumber().getEntity());

        cluster.setAutoConverge(model.getAutoConverge().getSelectedItem());
        cluster.setMigrateCompressed(model.getMigrateCompressed().getSelectedItem());
        if (model.getEnableGlusterService().getEntity()) {
            cluster.setGlusterTunedProfile(model.getGlusterTunedProfile().getSelectedItem());
        }
        cluster.getAdditionalRngSources().clear();
        if (Boolean.TRUE.equals(model.getRngHwrngSourceRequired().getEntity())) {
            cluster.getAdditionalRngSources().add(VmRngDevice.Source.HWRNG);
        }
        cluster.setMigrationBandwidthLimitType(model.getMigrationBandwidthLimitType().getSelectedItem());
        cluster.setCustomMigrationNetworkBandwidth(
                MigrationBandwidthLimitType.CUSTOM.equals(model.getMigrationBandwidthLimitType().getSelectedItem())
                ? model.getCustomMigrationNetworkBandwidth().getEntity()
                : null);

        cluster.setMacPoolId(model.getMacPoolListModel().getSelectedItem().getId());

        return cluster;
    }

    private void onSaveInternalWithModel(final ClusterModel model) {
        Cluster cluster = buildCluster(model);

        model.startProgress();

        final Network managementNetwork = model.getManagementNetwork().getSelectedItem();
        final ManagementNetworkOnClusterOperationParameters clusterOperationParameters =
                new ManagementNetworkOnClusterOperationParameters(cluster, managementNetwork.getId());
        final ActionType actionType = model.getIsNew() ? ActionType.AddCluster : ActionType.UpdateCluster;
        Frontend.getInstance().runAction(
                actionType,
                clusterOperationParameters,
                result -> {

                    ClusterListModel<Void> localModel = (ClusterListModel<Void>) result.getState();
                    if (model.getIsImportGlusterConfiguration().getEntity()) {
                        localModel.postOnSaveInternalWithImport(result.getReturnValue());
                    } else {
                        localModel.postOnSaveInternal(result.getReturnValue());
                    }
                },
                this);
    }

    private void fetchAndImportClusterHosts(final ClusterModel clusterModel) {
        getWindow().startProgress();
        AsyncQuery<QueryReturnValue> aQuery = new AsyncQuery<>(result -> {
            getWindow().stopProgress();

            QueryReturnValue returnValue = result;
            if (returnValue == null) {
                onEmptyGlusterHosts(clusterModel);
                return;
            }
            else if (!returnValue.getSucceeded()) {
                clusterModel.setMessage(Frontend.getInstance().getAppErrorsTranslator()
                        .translateErrorTextSingle(returnValue.getExceptionString()));
                return;
            }

            Map<String, String> hostMap = returnValue.getReturnValue();
            if (hostMap == null) {
                onEmptyGlusterHosts(clusterModel);
                return;
            }
            if (hostMap.containsValue(null) || hostMap.containsValue("")){ //$NON-NLS-1$
                onGlusterHostsWithoutFingerprint(hostMap, clusterModel);
                return;
            }
            ArrayList<EntityModel<HostDetailModel>> list = new ArrayList<>();
            for (Map.Entry<String, String> host : hostMap.entrySet()) {
                HostDetailModel hostModel = new HostDetailModel(host.getKey(), host.getValue());
                hostModel.setName(host.getKey());
                hostModel.setPassword("");//$NON-NLS-1$
                EntityModel<HostDetailModel> entityModel = new EntityModel<>(hostModel);
                list.add(entityModel);
            }
            importClusterHosts(clusterModel, list);
        });
        aQuery.setHandleFailure(true);
        AsyncDataProvider.getInstance().getGlusterHosts(aQuery,
                clusterModel.getGlusterHostAddress().getEntity(),
                clusterModel.getGlusterHostPassword().getEntity(),
                clusterModel.getGlusterHostFingerprint().getEntity());
    }

    private void onEmptyGlusterHosts(ClusterModel clusterModel) {
        clusterModel.setMessage(ConstantsManager.getInstance().getConstants().emptyGlusterHosts());
    }

    private void onGlusterHostsWithoutFingerprint(Map<String, String> hostMap, ClusterModel clusterModel) {
        ArrayList<String> problematicHosts = new ArrayList<>();
        for (Map.Entry<String, String> host : hostMap.entrySet()) {
            if (host.getValue() == null || host.getValue().equals("")) { //$NON-NLS-1$
                problematicHosts.add(host.getKey());
            }
        }

        clusterModel.setMessage(ConstantsManager.getInstance().getMessages().unreachableGlusterHosts(problematicHosts));
    }

    private void importClusterHosts(ClusterModel clusterModel, ArrayList<EntityModel<HostDetailModel>> hostList) {
        setWindow(null);
        getAddMultipleHostsCommand().execute();

        final MultipleHostsModel hostsModel = new MultipleHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().addMultipleHostsTitle());
        hostsModel.setHelpTag(HelpTag.add_hosts);
        hostsModel.setHashName("add_hosts"); //$NON-NLS-1$
        hostsModel.setClusterModel(clusterModel);
        hostsModel.getHosts().setItems(hostList);

        UICommand command = UICommand.createOkUiCommand("OnSaveHosts", this); //$NON-NLS-1$
        hostsModel.getCommands().add(command);

        hostsModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onSaveHosts() {
        MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if(hostsModel == null) {
            return;
        }
        if (!hostsModel.validate()) {
            return;
        }
        if (hostsModel.getClusterModel().getClusterId() != null) {
            addHosts(hostsModel);
        }
        else {
            onSaveInternalWithModel(hostsModel.getClusterModel());
        }
    }

    public void postOnSaveInternal(ActionReturnValue returnValue) {
        ClusterModel model = (ClusterModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();

            if (model.getIsNew()) {
                setGuideContext(returnValue.getActionReturnValue());
                updateActionAvailability();
                getGuideCommand().execute();
            }
        }
    }

    public void postOnSaveInternalWithImport(ActionReturnValue returnValue) {
        MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if (returnValue != null && returnValue.getSucceeded()) {
            hostsModel.getClusterModel().setClusterId((Guid) returnValue.getActionReturnValue());
            addHosts(hostsModel);
        }
    }

    private void addHosts(final MultipleHostsModel hostsModel) {
        hostsModel.startProgress();
        ArrayList<ActionParametersBase> parametersList = new ArrayList<>();
        for (Object object : hostsModel.getHosts().getItems()) {
            HostDetailModel hostDetailModel = (HostDetailModel) ((EntityModel) object).getEntity();

            VDS host = new VDS();
            host.setVdsName(hostDetailModel.getName());
            host.setHostName(hostDetailModel.getAddress());
            host.setSshKeyFingerprint(hostDetailModel.getFingerprint());
            host.setPort(54321);
            host.setSshPort(22); // TODO: get from UI, till then using defaults.
            host.setSshUsername("root"); //$NON-NLS-1$

            host.setClusterId(hostsModel.getClusterModel().getClusterId());
            host.setPmEnabled(false);

            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setPassword(hostDetailModel.getPassword());
            parameters.setOverrideFirewall(hostsModel.isConfigureFirewall());

            parametersList.add(parameters);
        }

        // Todo: calling the runMultipleAction() with isRunOnlyIfAllValidationPass=false
        // becuase this flag is now supported.
        // should check what is the required behaviour and return to true if required.
        Frontend.getInstance().runMultipleAction(ActionType.AddVds,
                parametersList,
                false,
                result -> {
                    hostsModel.stopProgress();
                    boolean isAllValidatePassed = true;
                    for (ActionReturnValue returnValueBase : result.getReturnValue()) {
                        isAllValidatePassed = returnValueBase.isValid();
                        if (!isAllValidatePassed) {
                            break;
                        }
                    }
                    if (isAllValidatePassed) {
                        cancel();
                    }
                }, null);
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

    private void updateActionAvailability() {
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);

        getResetEmulatedMachineCommand().setIsExecutionAllowed(
                getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void executeCommand(UICommand command, Object... parameters) {
        if (command == getEditCommand() && parameters.length > 0 && Boolean.TRUE.equals(parameters[0])) {
            super.executeCommand(command, parameters);
        } else if (command == getAddMacPoolCommand()) {
            super.executeCommand(command, parameters);
            addMacPool((ClusterModel) parameters[0]);
        }
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
        else if (command == getGuideCommand()) {
            guide();
        }
        else if (command == getResetEmulatedMachineCommand()) {
            resetEmulatedMachine();
        }
        else if ("OnResetClusterEmulatedMachine".equals(command.getName())) { //$NON-NLS-1$
            onResetClusterEmulatedMachine();
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
        else if ("OnSaveConfirmCpuThreads".equals(command.getName())) { //$NON-NLS-1$
            onSaveConfirmCpuThreads();
        }
        else if ("OnSaveConfirmGenericWarnings".equals(command.getName())) { //$NON-NLS-1$
            onSaveConfirmGenericWarnings();
        }
        else if ("OnSaveInternal".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternal();
        }
        else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        }
        else if ("OnSaveHosts".equals(command.getName())) { //$NON-NLS-1$
            onSaveHosts();
        }
    }

    @Override
    protected String getListName() {
        return "ClusterListModel"; //$NON-NLS-1$
    }

    /**
     * Checks if in selected cluster are some non responsive hosts. If so, it adds warning about upgrading cluster level
     * when some hosts are non responsive
     */
    @SuppressWarnings("unchecked")
    private void checkForNonResponsiveHosts(final ConfirmationModel confirmModel) {
        startProgress();
        Frontend.getInstance().runQuery(QueryType.GetHostsByClusterId,
                new IdQueryParameters(getSelectedItem().getId()),
                new AsyncQuery<>(returnValue -> {
                    List<VDS> hosts = null;
                    if (returnValue instanceof List) {
                        hosts = (List<VDS>) returnValue;
                    } else if (returnValue instanceof QueryReturnValue
                            && ((QueryReturnValue) returnValue).getReturnValue() instanceof List) {
                        hosts = ((QueryReturnValue) returnValue).getReturnValue();
                    }

                    boolean foundNRHosts = false;
                    if (hosts != null) {
                        for (VDS host : hosts) {
                            if (VDSStatus.NonResponsive == host.getStatus()) {
                                foundNRHosts = true;
                                break;
                            }
                        }
                    }

                    String existingMsg = confirmModel.getMessage() == null ? "" : confirmModel.getMessage();
                    if (foundNRHosts) {
                        confirmModel.setMessage(existingMsg +
                                constants.youAreAboutChangeClusterCompatibilityVersionNonResponsiveHostsMsg());
                    } else {
                        confirmModel.setMessage(existingMsg +
                                constants.youAreAboutChangeClusterCompatibilityVersionMsg());
                    }

                    stopProgress();
                }));
    }

}
