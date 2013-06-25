package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
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
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterFeaturesUtil;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MultipleHostsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

@SuppressWarnings("unused")
public class ClusterListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

    private UICommand privateNewCommand;
    private boolean clusterPolicyFirst;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
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

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand()
    {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value)
    {
        privateGuideCommand = value;
    }

    private UICommand privateAddMultipleHostsCommand;

    public UICommand getAddMultipleHostsCommand()
    {
        return privateAddMultipleHostsCommand;
    }

    private void setAddMultipleHostsCommand(UICommand value)
    {
        privateAddMultipleHostsCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VDSGroup>().Select(a =>
    // a.ID).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            ArrayList<Object> items = new ArrayList<Object>();
            for (Object i : getSelectedItems())
            {
                items.add(((VDSGroup) i).getId());
            }
            return items.toArray(new Object[] {});
        }
    }

    private Object privateGuideContext;

    public Object getGuideContext()
    {
        return privateGuideContext;
    }

    public void setGuideContext(Object value)
    {
        privateGuideContext = value;
    }

    private ClusterPolicyModel clusterPolicyModel;

    public ClusterPolicyModel getClusterPolicyModel() {
        return clusterPolicyModel;
    }

    public void setClusterPolicyModel(ClusterPolicyModel clusterPolicyModel) {
        this.clusterPolicyModel = clusterPolicyModel;
    }

    private ClusterServiceModel clusterServiceModel;

    public ClusterServiceModel getClusterServiceModel() {
        return clusterServiceModel;
    }

    public void setClusterServiceModel(ClusterServiceModel clusterServiceModel) {
        this.clusterServiceModel = clusterServiceModel;
    }

    private ClusterVmListModel clusterVmListModel;

    public ClusterVmListModel getClusterVmListModel() {
        return clusterVmListModel;
    }

    public void setClusterVmListModel(ClusterVmListModel clusterVmListModel) {
        this.clusterVmListModel = clusterVmListModel;
    }

    private ClusterGlusterHookListModel clusterGlusterHookListModel;

    public ClusterGlusterHookListModel getClusterGlusterHookListModel() {
        return clusterGlusterHookListModel;
    }

    public void setClusterGlusterHookListModel(ClusterGlusterHookListModel clusterGlusterHookListModel) {
        this.clusterGlusterHookListModel = clusterGlusterHookListModel;
    }

    public ClusterListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().clustersTitle());
        setHashName("clusters"); //$NON-NLS-1$

        setDefaultSearchString("Cluster:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        getClusterPolicyModel().setEditPolicyCommand(getEditCommand());
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$
        setAddMultipleHostsCommand(new UICommand("AddHosts", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void guide()
    {
        ClusterGuideModel model = new ClusterGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newClusterGuideMeTitle());
        model.setHashName("new_cluster_-_guide_me"); //$NON-NLS-1$

        if (getGuideContext() == null) {
            VDSGroup cluster = (VDSGroup) getSelectedItem();
            setGuideContext(cluster.getId());
        }

        AsyncDataProvider.getClusterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterListModel clusterListModel = (ClusterListModel) target;
                        ClusterGuideModel model = (ClusterGuideModel) clusterListModel.getWindow();
                        model.setEntity((VDSGroup) returnValue);

                        UICommand tempVar = new UICommand("Cancel", clusterListModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                        tempVar.setIsDefault(true);
                        tempVar.setIsCancel(true);
                        model.getCommands().add(tempVar);
                    }
                }), (Guid) getGuideContext());
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

        setClusterVmListModel(new ClusterVmListModel());
        setClusterPolicyModel(new ClusterPolicyModel());
        setClusterServiceModel(new ClusterServiceModel());
        setClusterGlusterHookListModel(new ClusterGlusterHookListModel());

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new ClusterGeneralModel());
        list.add(getClusterPolicyModel());
        list.add(new ClusterNetworkListModel());
        list.add(new ClusterHostListModel());
        list.add(getClusterVmListModel());
        list.add(getClusterServiceModel());
        list.add(getClusterGlusterHookListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        VDSGroup vdsGroup = (VDSGroup) getSelectedItem();
        getClusterVmListModel().setIsAvailable(vdsGroup != null && vdsGroup.supportsVirtService());
        getClusterServiceModel().setIsAvailable(vdsGroup != null && vdsGroup.supportsGlusterService()
                && GlusterFeaturesUtil.isGlusterVolumeServicesSupported(vdsGroup.getcompatibility_version()));
        getClusterGlusterHookListModel().setIsAvailable(vdsGroup != null && vdsGroup.supportsGlusterService()
                && GlusterFeaturesUtil.isGlusterHookSupported(vdsGroup.getcompatibility_version()));
        getClusterPolicyModel().setIsAvailable(false);
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("cluster"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Cluster);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    public void newEntity()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterModel clusterModel = new ClusterModel();
        clusterModel.init(false);
        setWindow(clusterModel);
        clusterModel.setTitle(ConstantsManager.getInstance().getConstants().newClusterTitle());
        clusterModel.setHashName("new_cluster"); //$NON-NLS-1$
        clusterModel.setIsNew(true);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ClusterListModel clModel = (ClusterListModel) model;
                ClusterModel cModel = (ClusterModel) clModel.getWindow();
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;

                cModel.getDataCenter().setItems(dataCenters);

                // Be aware of system tree selection.
                // Strict data center as neccessary.
                if (clModel.getSystemTreeSelectedItem() != null
                        && clModel.getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
                {
                    StoragePool selectDataCenter = (StoragePool) clModel.getSystemTreeSelectedItem().getEntity();

                    cModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters,
                            new Linq.DataCenterPredicate(selectDataCenter.getId())));
                    cModel.getDataCenter().setIsChangable(false);
                }
                else
                {
                    cModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));
                }

                UICommand tempVar = new UICommand("OnSave", clModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                cModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", clModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                cModel.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.getDataCenterList(_asyncQuery);
    }

    public void edit()
    {
        final VDSGroup cluster = (VDSGroup) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final ClusterModel clusterModel = new ClusterModel();
        clusterModel.setEntity(cluster);
        clusterModel.init(true);
        clusterModel.getClusterPolicyModel().setEditClusterPolicyFirst(clusterPolicyFirst);
        clusterModel.getClusterPolicyModel().getEnableTrustedService().setEntity(cluster.supportsTrustedService());
        setWindow(clusterModel);
        clusterModel.setTitle(ConstantsManager.getInstance().getConstants().editClusterTitle());
        clusterModel.setHashName("edit_cluster"); //$NON-NLS-1$
        clusterModel.setOriginalName(cluster.getName());
        clusterModel.getName().setEntity(cluster.getName());
        clusterModel.getEnableOvirtService().setEntity(cluster.supportsVirtService());
        clusterModel.getEnableOvirtService().setIsChangable(true);
        clusterModel.getEnableGlusterService().setEntity(cluster.supportsGlusterService());
        clusterModel.getEnableGlusterService().setIsChangable(true);
        clusterModel.getClusterPolicyModel().setSelectionAlgorithm(cluster.getselection_algorithm());
        clusterModel.getClusterPolicyModel().getOverCommitTime().setEntity(cluster.getcpu_over_commit_duration_minutes());
        clusterModel.getClusterPolicyModel().setOverCommitLowLevel(cluster.getlow_utilization());
        clusterModel.getClusterPolicyModel().setOverCommitHighLevel(cluster.gethigh_utilization());

        clusterModel.getClusterPolicyModel().saveDefaultValues();
        if (cluster.supportsTrustedService())
        {
            clusterModel.getEnableGlusterService().setIsChangable(false);
        }
        if (cluster.supportsVirtService()&& !cluster.supportsGlusterService())
        {
            clusterModel.getClusterPolicyModel().getEnableTrustedService().setIsChangable(true);
        }
        else
        {
            clusterModel.getClusterPolicyModel().getEnableTrustedService().setIsChangable(false);
        }

        AsyncDataProvider.getAllowClusterWithVirtGlusterEnabled(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                final boolean isVirtGlusterAllowed = (Boolean) returnValue;
                AsyncQuery asyncQuery = new AsyncQuery();
                asyncQuery.setModel(clusterModel);
                asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model1, Object result)
                    {
                        ArrayList<GlusterVolumeEntity> volumes = (ArrayList<GlusterVolumeEntity>) result;
                        if (volumes.size() > 0)
                        {
                            clusterModel.getEnableGlusterService().setIsChangable(false);
                            if (!isVirtGlusterAllowed)
                            {
                                clusterModel.getEnableOvirtService().setIsChangable(false);
                            }
                        }
                    }
                };
                AsyncDataProvider.getVolumeList(asyncQuery, cluster.getName());

                AsyncQuery asyncQuery1 = new AsyncQuery();
                asyncQuery1.setModel(clusterModel);
                asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model1, Object result)
                    {
                        ArrayList<VM> vmList = (ArrayList<VM>) result;
                        if (vmList.size() > 0)
                        {
                            clusterModel.getEnableOvirtService().setIsChangable(false);
                            if (!isVirtGlusterAllowed)
                            {
                                clusterModel.getEnableGlusterService().setIsChangable(false);
                            }
                        }
                    }
                };
                AsyncDataProvider.getVmListByClusterName(asyncQuery1, cluster.getName());
                AsyncQuery asyncQuery2 = new AsyncQuery();
                asyncQuery2.setModel(clusterModel);
                asyncQuery2.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model1, Object result)
                    {
                        ArrayList<VDS> vdsList = (ArrayList<VDS>) result;
                        if (vdsList.size() > 0)
                        {
                            clusterModel.getClusterPolicyModel().getEnableTrustedService().setIsChangable(false);
                            clusterModel.getClusterPolicyModel().getEnableTrustedService().setInfo(ConstantsManager.getInstance().getConstants().trustedServiceDisabled());
                        }
                    }
                };
                AsyncDataProvider.getHostListByCluster(asyncQuery2, cluster.getName());
            }
        }));

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster) {
            clusterModel.getName().setIsChangable(false);
            clusterModel.getName().setInfo("Cannot edit Cluster's Name in tree context"); //$NON-NLS-1$
        }

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        clusterModel.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        clusterModel.getCommands().add(tempVar2);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeClusterTitle());
        model.setHashName("remove_cluster"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().clustersMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (VDSGroup a : Linq.<VDSGroup> cast(getSelectedItems()))
        {
            list.add(a.getName());
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

    public void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();
        for (Object a : getSelectedItems())
        {
            prms.add(new VdsGroupParametersBase(((VDSGroup) a).getId()));
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVdsGroup, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void onSave()
    {
        ClusterModel model = (ClusterModel) getWindow();

        boolean validateCpu =
                (model.getIsNew() && (Boolean) model.getEnableOvirtService().getEntity())
                        || (model.getIsEdit() && ((VDSGroup) getSelectedItem()).getcpu_name() != null);

        if (!model.validate(validateCpu))
        {
            return;
        }
        else if (model.getIsNew())
        {
            onPreSaveInternal(model);
        }
        else
        {
            onSaveConfirmCV(model);
        }
    }

    private void onSaveConfirmCV(ClusterModel model)
    {
        if (!((Version) model.getVersion().getSelectedItem()).equals(((VDSGroup) getSelectedItem()).getcompatibility_version())) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .changeClusterCompatibilityVersionTitle());
            confirmModel.setHashName("change_cluster_compatibility_version"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeClusterCompatibilityVersionMsg());

            UICommand tempVar = new UICommand("OnSaveConfirmCpuThreads", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onSaveConfirmCpuThreads();
        }
    }

    private void onSaveConfirmCpuThreads()
    {
        ClusterModel model = (ClusterModel) getWindow();

        // cancel confirm window if there is one
        cancelConfirmation();

        // CPU thread support is being turned off either explicitly or via version change
        if (!((Boolean) model.getVersionSupportsCpuThreads().getEntity() && (Boolean) model.getCountThreadsAsCores().getEntity())
                && ((VDSGroup) getSelectedItem()).getCountThreadsAsCores()) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .disableClusterCpuThreadSupportTitle());
            confirmModel.setHashName("disable_cpu_thread_support"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeClusterCpuThreadSupportMsg());

            UICommand tempVar = new UICommand("OnSaveInternal", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onSaveInternal();
        }
    }

    public void onPreSaveInternal(ClusterModel model)
    {
        if ((Boolean) model.getIsImportGlusterConfiguration().getEntity())
        {
            fetchAndImportClusterHosts(model);
        }
        else
        {
            onSaveInternal();
        }
    }

    public void onSaveInternal()
    {
        ClusterModel model = (ClusterModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        // cancel confirm window if there is
        cancelConfirmation();

        onSaveInternalWithModel(model);
    }

    private void onSaveInternalWithModel(final ClusterModel model) {
        VDSGroup cluster = model.getIsNew() ? new VDSGroup() : (VDSGroup) Cloner.clone(getSelectedItem());

        Version version = (Version) model.getVersion().getSelectedItem();

        cluster.setName((String) model.getName().getEntity());
        cluster.setdescription((String) model.getDescription().getEntity());
        cluster.setStoragePoolId(((StoragePool) model.getDataCenter().getSelectedItem()).getId());
        if (model.getCPU().getSelectedItem() != null)
        {
            cluster.setcpu_name(((ServerCpu) model.getCPU().getSelectedItem()).getCpuName());
        }
        cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
        cluster.setCountThreadsAsCores(Boolean.TRUE.equals(model.getVersionSupportsCpuThreads().getEntity())
                && Boolean.TRUE.equals(model.getCountThreadsAsCores().getEntity()));
        cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0); //$NON-NLS-1$
        cluster.setcompatibility_version(version);
        cluster.setMigrateOnError(model.getMigrateOnErrorOption());
        cluster.setVirtService((Boolean) model.getEnableOvirtService().getEntity());
        cluster.setGlusterService((Boolean) model.getEnableGlusterService().getEntity());
        cluster.setselection_algorithm(model.getClusterPolicyModel().getSelectionAlgorithm());
        cluster.setTrustedService((Boolean) model.getClusterPolicyModel().getEnableTrustedService().getEntity());
        if (model.getClusterPolicyModel().getOverCommitTime().getIsAvailable())
        {
            cluster.setcpu_over_commit_duration_minutes(Integer.parseInt(model.getClusterPolicyModel().getOverCommitTime()
                    .getEntity()
                    .toString()));
        }        cluster.setlow_utilization(model.getClusterPolicyModel().getOverCommitLowLevel());
        cluster.sethigh_utilization(model.getClusterPolicyModel().getOverCommitHighLevel());
        cluster.setClusterPolicyId(((ClusterPolicy) model.getClusterPolicy().getSelectedItem()).getId());
        cluster.setClusterPolicyProperties(KeyValueModel.convertProperties(model.getCustomPropertySheet().getEntity()));

        model.startProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddVdsGroup : VdcActionType.UpdateVdsGroup,
                new VdsGroupOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        ClusterListModel localModel = (ClusterListModel) result.getState();
                        if ((Boolean) model.getIsImportGlusterConfiguration().getEntity()) {
                            localModel.postOnSaveInternalWithImport(result.getReturnValue());
                        }
                        else {
                            localModel.postOnSaveInternal(result.getReturnValue());
                        }
                    }
                },
                this);
    }

    private void fetchAndImportClusterHosts(final ClusterModel clusterModel)
    {
        getWindow().startProgress(null);
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.setHandleFailure(true);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                getWindow().stopProgress();

                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) result;
                if (returnValue == null) {
                    onEmptyGlusterHosts(clusterModel);
                    return;
                }
                else if (!returnValue.getSucceeded()) {
                    clusterModel.setMessage(Frontend.getAppErrorsTranslator()
                            .TranslateErrorTextSingle(returnValue.getExceptionString()));
                    return;
                }

                Map<String, String> hostMap = (Map<String, String>) returnValue.getReturnValue();
                if (hostMap == null)
                {
                    onEmptyGlusterHosts(clusterModel);
                    return;
                }
                if (hostMap.containsValue(null) || hostMap.containsValue(""))//$NON-NLS-1$
                {
                    onGlusterHostsWithoutFingerprint(hostMap, clusterModel);
                    return;
                }
                ArrayList<EntityModel> list = new ArrayList<EntityModel>();
                for (Map.Entry<String, String> host : hostMap.entrySet())
                {
                    HostDetailModel hostModel = new HostDetailModel(host.getKey(), host.getValue());
                    hostModel.setName(host.getKey());
                    hostModel.setPassword("");//$NON-NLS-1$
                    EntityModel entityModel = new EntityModel(hostModel);
                    list.add(entityModel);
                }
                importClusterHosts(clusterModel, list);
            }
        };
        AsyncDataProvider.getGlusterHosts(aQuery,
                (String) clusterModel.getGlusterHostAddress().getEntity(),
                (String) clusterModel.getGlusterHostPassword().getEntity(),
                (String) clusterModel.getGlusterHostFingerprint().getEntity());
    }

    private void onEmptyGlusterHosts(ClusterModel clusterModel)
    {
        clusterModel.setMessage(ConstantsManager.getInstance().getConstants().emptyGlusterHosts());
    }

    private void onGlusterHostsWithoutFingerprint(Map<String, String> hostMap, ClusterModel clusterModel)
    {
        ArrayList<String> problematicHosts = new ArrayList<String>();
        for (Map.Entry<String, String> host : hostMap.entrySet())
        {
            if (host.getValue() == null || host.getValue().equals("")) //$//$NON-NLS-1$
            {
                problematicHosts.add(host.getKey());
            }
        }

        clusterModel.setMessage(ConstantsManager.getInstance().getMessages().unreachableGlusterHosts(problematicHosts));
    }

    private void importClusterHosts(ClusterModel clusterModel, ArrayList<EntityModel> hostList)
    {
        setWindow(null);
        getAddMultipleHostsCommand().execute();

        final MultipleHostsModel hostsModel = new MultipleHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().addMultipleHostsTitle());
        hostsModel.setHashName("add_hosts"); //$NON-NLS-1$
        hostsModel.setClusterModel(clusterModel);
        hostsModel.getHosts().setItems(hostList);

        UICommand command = new UICommand("OnSaveHosts", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        hostsModel.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        hostsModel.getCommands().add(command);
    }

    private void onSaveHosts()
    {
        MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if(hostsModel == null)
        {
            return;
        }
        if (!hostsModel.validate())
        {
            return;
        }
        if (hostsModel.getClusterModel().getClusterId() != null)
        {
            addHosts(hostsModel);
        }
        else
        {
            onSaveInternalWithModel(hostsModel.getClusterModel());
        }
    }

    public void postOnSaveInternal(VdcReturnValueBase returnValue)
    {
        ClusterModel model = (ClusterModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();

            if (model.getIsNew())
            {
                setGuideContext(returnValue.getActionReturnValue());
                updateActionAvailability();
                getGuideCommand().execute();
            }
        }
    }

    public void postOnSaveInternalWithImport(VdcReturnValueBase returnValue)
    {
        MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if (returnValue != null && returnValue.getSucceeded())
        {
            hostsModel.getClusterModel().setClusterId((Guid) returnValue.getActionReturnValue());
            addHosts(hostsModel);
        }
    }

    private void addHosts(final MultipleHostsModel hostsModel) {
        hostsModel.startProgress(null);
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>();
        for (Object object : hostsModel.getHosts().getItems()) {
            HostDetailModel hostDetailModel = (HostDetailModel) ((EntityModel) object).getEntity();

            VDS host = new VDS();
            host.setVdsName(hostDetailModel.getName());
            host.setHostName(hostDetailModel.getAddress());
            host.setSSHKeyFingerprint(hostDetailModel.getFingerprint());
            host.setPort(54321);

            host.setVdsGroupId((Guid) hostsModel.getClusterModel().getClusterId());
            host.setpm_enabled(false);

            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setRootPassword(hostDetailModel.getPassword());
            parameters.setOverrideFirewall(false);
            parameters.setRebootAfterInstallation(hostsModel.getClusterModel().getEnableOvirtService().getIsSelected());

            parametersList.add(parameters);
        }


        Frontend.RunMultipleAction(VdcActionType.AddVds,
                parametersList,
                true,
                new IFrontendMultipleActionAsyncCallback() {

            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                        hostsModel.stopProgress();
                        boolean isAllCanDoPassed = true;
                        for (VdcReturnValueBase returnValueBase : result.getReturnValue())
                        {
                            isAllCanDoPassed = isAllCanDoPassed && returnValueBase.getCanDoAction();
                            if (!isAllCanDoPassed)
                            {
                                break;
                            }
                        }
                        if (isAllCanDoPassed)
                        {
                            cancel();
                        }
            }
        }, null);
    }

    public void cancel()
    {
        cancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        updateActionAvailability();
    }

    public void cancelConfirmation()
    {
        setConfirmWindow(null);
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

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster)
        {
            VDSGroup cluster = (VDSGroup) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.firstOrDefault(Linq.<VDSGroup> cast(getItems()),
                    new Linq.ClusterPredicate(cluster.getId())));
        }
    }

    private void updateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null &&
                (getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster || getSystemTreeSelectedItem().getType() == SystemTreeItemType.Cluster_Gluster));

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    @Override
    public void executeCommand(UICommand command, Object... parameters) {
        if (command == getEditCommand() && parameters.length > 0 && Boolean.TRUE.equals(parameters[0]))
        {
            this.clusterPolicyFirst = true;
            super.executeCommand(command, parameters);
            this.clusterPolicyFirst = false;
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getGuideCommand())
        {
            guide();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveConfirmCpuThreads")) //$NON-NLS-1$
        {
            onSaveConfirmCpuThreads();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternal")) //$NON-NLS-1$
        {
            onSaveInternal();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation")) //$NON-NLS-1$
        {
            cancelConfirmation();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveHosts")) //$NON-NLS-1$
        {
            onSaveHosts();
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
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged()
    {
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "ClusterListModel"; //$NON-NLS-1$
    }
}
