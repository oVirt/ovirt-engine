package org.ovirt.engine.ui.uicommonweb.models.volumes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.ListUtils.Predicate;
import org.ovirt.engine.core.common.utils.ListUtils.PredicateFilter;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeProfileStatisticsModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeRebalanceStatusModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class VolumeListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {

    public static final Integer REPLICATE_COUNT_DEFAULT = 2;
    public static final Integer STRIPE_COUNT_DEFAULT = 4;

    private UICommand newVolumeCommand;

    public UICommand getNewVolumeCommand()
    {
        return newVolumeCommand;
    }

    private void setNewVolumeCommand(UICommand value)
    {
        newVolumeCommand = value;
    }

    private UICommand removeVolumeCommand;

    public UICommand getRemoveVolumeCommand()
    {
        return removeVolumeCommand;
    }

    private void setRemoveVolumeCommand(UICommand value)
    {
        removeVolumeCommand = value;
    }

    private UICommand startCommand;
    private UICommand stopCommand;
    private UICommand startRebalanceCommand;
    private UICommand stopRebalanceCommand;
    private UICommand optimizeForVirtStoreCommand;
    private UICommand startVolumeProfilingCommand;
    private UICommand showVolumeProfileDetailsCommand;
    private UICommand stopVolumeProfilingCommand;

    public UICommand getStartRebalanceCommand() {
        return startRebalanceCommand;
    }

    public void setStartRebalanceCommand(UICommand startRebalanceCommand) {
        this.startRebalanceCommand = startRebalanceCommand;
    }

    public UICommand getStopRebalanceCommand() {
        return stopRebalanceCommand;
    }

    public void setStopRebalanceCommand(UICommand stopRebalanceCommand) {
        this.stopRebalanceCommand = stopRebalanceCommand;
    }
    private UICommand statusRebalanceCommand;

    public UICommand getStatusRebalanceCommand() {
        return statusRebalanceCommand;
    }

    public void setStatusRebalanceCommand(UICommand statusRebalanceCommand) {
        this.statusRebalanceCommand = statusRebalanceCommand;
    }

    public UICommand getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(UICommand startCommand) {
        this.startCommand = startCommand;
    }

    public UICommand getStopCommand() {
        return stopCommand;
    }

    public void setStopCommand(UICommand stopCommand) {
        this.stopCommand = stopCommand;
    }

    public UICommand getOptimizeForVirtStoreCommand() {
        return optimizeForVirtStoreCommand;
    }

    public void setOptimizeForVirtStoreCommand(UICommand optimizeForVirtStoreCommand) {
        this.optimizeForVirtStoreCommand = optimizeForVirtStoreCommand;
    }

    private final VolumeBrickListModel brickListModel;

    public VolumeBrickListModel getBrickListModel() {
        return this.brickListModel;
    }

    private final VolumeGeoRepListModel geoRepListModel;

    public VolumeGeoRepListModel getGeoRepListModel() {
        return geoRepListModel;
    }

    public UICommand getStartVolumeProfilingCommand() {
        return startVolumeProfilingCommand;
    }

    public void setStartVolumeProfilingCommand(UICommand startVolumeProfilingCommand) {
        this.startVolumeProfilingCommand = startVolumeProfilingCommand;
    }

    public UICommand getShowVolumeProfileDetailsCommand() {
        return showVolumeProfileDetailsCommand;
    }

    public void setShowVolumeProfileDetailsCommand(UICommand showVolumeProfileDetailsCommand) {
        this.showVolumeProfileDetailsCommand = showVolumeProfileDetailsCommand;
    }

    public UICommand getStopVolumeProfilingCommand() {
        return stopVolumeProfilingCommand;
    }

    public void setStopVolumeProfilingCommand(UICommand stopVolumeProfilingCommand) {
        this.stopVolumeProfilingCommand = stopVolumeProfilingCommand;
    }

    @Inject
    public VolumeListModel(final VolumeBrickListModel volumeBrickListModel, final VolumeGeneralModel volumeGeneralModel,
            final VolumeParameterListModel volumeParameterListModel, final PermissionListModel permissionListModel,
            final VolumeEventListModel volumeEventListModel, final VolumeGeoRepListModel geoRepListModel) {
        this.brickListModel = volumeBrickListModel;
        this.geoRepListModel = geoRepListModel;
        setDetailList(volumeGeneralModel, volumeParameterListModel, permissionListModel, volumeEventListModel);
        setTitle(ConstantsManager.getInstance().getConstants().volumesTitle());

        setDefaultSearchString("Volumes:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.GLUSTER_VOLUME_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.GlusterOnly);

        setNewVolumeCommand(new UICommand("Create Volume", this)); //$NON-NLS-1$
        setRemoveVolumeCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setStartCommand(new UICommand("Start", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setStartRebalanceCommand(new UICommand("StartRebalance", this)); //$NON-NLS-1$
        setStopRebalanceCommand(new UICommand("StopRebalace", this)); //$NON-NLS-1$
        setStatusRebalanceCommand(new UICommand("StatusRebalance", this)); //$NON-NLS-1$
        setStartVolumeProfilingCommand(new UICommand("startProfiling", this));//$NON-NLS-1$
        setShowVolumeProfileDetailsCommand(new UICommand("showProfileDetails", this));//$NON-NLS-1$
        setStopVolumeProfilingCommand(new UICommand("stopProfiling", this));//$NON-NLS-1$
        setOptimizeForVirtStoreCommand(new UICommand("OptimizeForVirtStore", this)); //$NON-NLS-1$

        getRemoveVolumeCommand().setIsExecutionAllowed(false);
        getStartCommand().setIsExecutionAllowed(false);
        getStopCommand().setIsExecutionAllowed(false);
        getStartRebalanceCommand().setIsExecutionAllowed(false);
        getStopRebalanceCommand().setIsExecutionAllowed(false);
        getStartVolumeProfilingCommand().setIsExecutionAllowed(false);
        getStopVolumeProfilingCommand().setIsExecutionAllowed(false);
        getShowVolumeProfileDetailsCommand().setIsExecutionAllowed(true);

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final VolumeGeneralModel volumeGeneralModel,
            final VolumeParameterListModel volumeParameterListModel, final PermissionListModel permissionListModel,
            final VolumeEventListModel volumeEventListModel) {

        List<EntityModel> list = new ArrayList<EntityModel>();
        list.add(volumeGeneralModel);
        list.add(volumeParameterListModel);
        list.add(getBrickListModel());
        list.add(getGeoRepListModel());
        list.add(permissionListModel);
        list.add(volumeEventListModel);
        setDetailModels(list);
    }

    private void newVolume() {
        if (getWindow() != null) {
            return;
        }

        VolumeModel volumeModel = new VolumeModel();
        volumeModel.setHelpTag(HelpTag.new_volume);
        volumeModel.setHashName("new_volume"); //$NON-NLS-1$
        volumeModel.setTitle(ConstantsManager.getInstance().getConstants().newVolumeTitle());
        setWindow(volumeModel);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                VolumeListModel volumeListModel = (VolumeListModel) model;
                VolumeModel innerVolumeModel = (VolumeModel) volumeListModel.getWindow();
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;
                final UIConstants constants = ConstantsManager.getInstance().getConstants();

                if (volumeListModel.getSystemTreeSelectedItem() != null)
                {
                    switch (volumeListModel.getSystemTreeSelectedItem().getType())
                    {
                    case Volumes:
                    case Cluster:
                    case Cluster_Gluster:
                        VDSGroup cluster = (VDSGroup) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        for (StoragePool dc : dataCenters)
                        {
                            if (dc.getId().equals(cluster.getStoragePoolId()))
                            {
                                innerVolumeModel.getDataCenter()
                                        .setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] {dc})));
                                innerVolumeModel.getDataCenter().setSelectedItem(dc);
                                break;
                            }
                        }
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setChangeProhibitionReason(
                                constants.cannotChangeDCInTreeContext());
                        innerVolumeModel.getCluster().setItems(Arrays.asList(cluster));
                        innerVolumeModel.getCluster().setSelectedItem(cluster);
                        innerVolumeModel.getCluster().setIsChangable(false);
                        innerVolumeModel.getCluster().setChangeProhibitionReason(
                                constants.cannotChangeClusterInTreeContext());
                        break;
                    case Clusters:
                    case DataCenter:
                        StoragePool selectDataCenter =
                                (StoragePool) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        innerVolumeModel.getDataCenter()
                                .setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { selectDataCenter })));
                        innerVolumeModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setChangeProhibitionReason(
                                constants.cannotChangeDCInTreeContext());
                        break;
                    default:
                        innerVolumeModel.getDataCenter().setItems(dataCenters);
                        innerVolumeModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));
                        break;
                    }
                }
                else
                {
                    innerVolumeModel.getDataCenter().setItems(dataCenters);
                    innerVolumeModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));
                }

                UICommand command = new UICommand("onCreateVolume", volumeListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                innerVolumeModel.getCommands().add(command);
                command = new UICommand("Cancel", volumeListModel);  //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                command.setIsCancel(true);
                innerVolumeModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.getInstance().getDataCenterByClusterServiceList(_asyncQuery, false, true);

    }

    private void removeVolume() {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeVolumesTitle());
        model.setHelpTag(HelpTag.remove_volume);
        model.setHashName("remove_volume"); //$NON-NLS-1$
        model.setNote(ConstantsManager.getInstance().getConstants().removeVolumesWarning());

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<String>();
        for (GlusterVolumeEntity item : Linq.<GlusterVolumeEntity> cast(getSelectedItems()))
        {
            list.add(item.getName());
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

    private void onRemoveVolume() {
        if (getWindow() == null)
        {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.DeleteGlusterVolume, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                }, model);
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.GlusterVolume, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
        GlusterVolumeEntity selectedVolume = (GlusterVolumeEntity)provideDetailModelEntity(getSelectedItem());
        getBrickListModel().setVolumeEntity(selectedVolume);
        getGeoRepListModel().setEntity(selectedVolume);
    }

    @Override
    protected void selectedItemsChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {

        boolean allowStart = true;
        boolean allowStop = true;
        boolean allowRemove = true;
        boolean allowStartRebalance = true;
        boolean allowStopRebalance = true;
        boolean allowStatusRebalance = true;
        boolean allowOptimize = true;
        boolean allowStartProfiling = false;
        boolean allowStopProfiling = false;
        boolean allowProfileStatisticsDetails = false;

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowStart = false;
            allowStop = false;
            allowRemove = false;
            allowStartRebalance = false;
            allowStopRebalance = false;
            allowStatusRebalance = false;
            allowOptimize = false;
        }
        else {
            List<GlusterVolumeEntity> list = Linq.<GlusterVolumeEntity> cast(getSelectedItems());
            allowStartProfiling = isStartProfileAvailable(list);
            allowStopProfiling = isStopProfileAvailable(list);
            for (GlusterVolumeEntity volume : list) {
                if (volume.getStatus() == GlusterStatus.UP) {
                    allowStart = false;
                    allowRemove = false;
                }
                else if (volume.getStatus() == GlusterStatus.DOWN) {
                    allowStop = false;
                    allowStartRebalance = false;
                }

                GlusterAsyncTask asyncTask = volume.getAsyncTask();
                if (asyncTask != null) {
                    allowStartRebalance =
                            allowStartRebalance &&
                                    asyncTask.getStatus() == null ? asyncTask.getJobStatus() != JobExecutionStatus.STARTED
                                    : asyncTask.getStatus() != JobExecutionStatus.STARTED;
                }
            }

            if (list.size() == 1) {
                GlusterVolumeEntity volumeEntity = list.get(0);
                GlusterAsyncTask asyncTask = volumeEntity.getAsyncTask();
                allowStopRebalance =
                        volumeEntity.getStatus() == GlusterStatus.UP && asyncTask != null
                                && asyncTask.getType() == GlusterTaskType.REBALANCE
                                && asyncTask.getStatus() == JobExecutionStatus.STARTED;
            }
            else {
                allowStopRebalance = false;
            }
            allowStatusRebalance = getRebalanceStatusAvailability(getSelectedItems());
            allowProfileStatisticsDetails = getProfileStatisticsAvailability(list);
        }
        getStartCommand().setIsExecutionAllowed(allowStart);
        getStopCommand().setIsExecutionAllowed(allowStop);
        getRemoveVolumeCommand().setIsExecutionAllowed(allowRemove);
        getStartRebalanceCommand().setIsExecutionAllowed(allowStartRebalance);
        getStopRebalanceCommand().setIsExecutionAllowed(allowStopRebalance);
        getStatusRebalanceCommand().setIsExecutionAllowed(allowStatusRebalance);
        getOptimizeForVirtStoreCommand().setIsExecutionAllowed(allowOptimize);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Volume);

        getNewVolumeCommand().setIsAvailable(isAvailable);
        getRemoveVolumeCommand().setIsAvailable(isAvailable);
        getStartVolumeProfilingCommand().setIsExecutionAllowed(allowStartProfiling);
        getStopVolumeProfilingCommand().setIsExecutionAllowed(allowStopProfiling);
        getShowVolumeProfileDetailsCommand().setIsExecutionAllowed(allowProfileStatisticsDetails);
    }

    private boolean isStopProfileAvailable(List<GlusterVolumeEntity> list) {
        if (getSelectedItems().size() == 0) {
            return false;
        } else {
            for (GlusterVolumeEntity volumeEntity : list) {
                if (volumeEntity.getStatus() == GlusterStatus.DOWN) {
                    return false;
                }
                if ((volumeEntity.getOptionValue("diagnostics.latency-measurement") == null)|| (!(volumeEntity.getOptionValue("diagnostics.latency-measurement").equals("on")))) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isStartProfileAvailable(List<GlusterVolumeEntity> list) {
        if (getSelectedItems().size() == 0) {
            return false;
        } else {
            for (GlusterVolumeEntity volumeEntity : list) {
                if (volumeEntity.getStatus() == GlusterStatus.DOWN) {
                    return false;
                }
                if ((volumeEntity.getOptionValue("diagnostics.latency-measurement") != null) && (volumeEntity.getOptionValue("diagnostics.latency-measurement").equals("on"))) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    return false;
                }
            }
            return true;
        }
    }

    private boolean getRebalanceStatusAvailability(List<GlusterVolumeEntity> selectedVolumes) {
        if (selectedVolumes.size() == 1) {
            GlusterVolumeEntity selectedVolume = selectedVolumes.get(0);
            if (selectedVolume.getStatus() == GlusterStatus.UP && selectedVolume.getVolumeType().isDistributedType()
                    && selectedVolume.getBricks().size() > 1) {
                return true;
            }
        }
        return false;
    }

    private boolean getProfileStatisticsAvailability(List<GlusterVolumeEntity> selectedVolumes) {
        if(selectedVolumes.size() == 1) {
            GlusterVolumeEntity selectedVolume = selectedVolumes.get(0);
            if(selectedVolume.getStatus() == GlusterStatus.UP) {
                return true;
            }
        }
        return false;
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getNewVolumeCommand())) {
            newVolume();
        }
        else if (command.equals(getRemoveVolumeCommand())) {
            removeVolume();
        } else if(command.getName().equals("rebalanceNotStarted")) {//$NON-NLS-1$
            closeConfirmationWindow();
        }
        else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        } else if (command.getName().equals("onCreateVolume")) { //$NON-NLS-1$
            onCreateVolume();
        } else if (command.equals(getStartCommand())) {
            start();
        } else if (command.equals(getStopCommand())) {
            stop();
        } else if (command.equals(getStartRebalanceCommand())) {
            startRebalance();
        } else if (command.equals(getStopRebalanceCommand())) {
            stopRebalance();
        } else if (command.getName().equals("onStopRebalance")) { //$NON-NLS-1$
            onStopRebalance();
        } else if (command.equals(getStatusRebalanceCommand())) {
            showRebalanceStatus();
        } else if (command.getName().equals("CancelConfirmation")) { //$NON-NLS-1$
            setConfirmWindow(null);
        } else if (command.getName().equals("CancelRebalanceStatus")) {//$NON-NLS-1$
            cancelRebalanceStatus();
        }else if (command.equals(getOptimizeForVirtStoreCommand())) {
            optimizeForVirtStore();
        } else if (command.getName().equals("onStop")) {//$NON-NLS-1$
            onStop();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemoveVolume();
        } else if(command.getName().equals("stop_rebalance_from_status")) {//$NON-NLS-1$
            stopRebalance();
        } else if(command.equals(getStartVolumeProfilingCommand()) || command.getName().equals("startProfiling")) {//$NON-NLS-1$
            startVolumeProfiling();
        } else if(command.equals(getStopVolumeProfilingCommand()) || command.getName().equals("stopProfiling")) {//$NON-NLS-1$
            stopVolumeProfiling();
        } else if(command.equals(getShowVolumeProfileDetailsCommand()) || command.getName().equals("showProfileDetails")) {//$NON-NLS-1$
            showVolumeProfiling();
        }else if(command.getName().equalsIgnoreCase("closeProfileStats")) {//$NON-NLS-1$
            setWindow(null);
        } else if(command.getName().equalsIgnoreCase("CancelOptimizeForVirtStore")) {//$NON-NLS-1$
            setConfirmWindow(null);
        } else if (command.getName().equalsIgnoreCase("ConfirmOptimiseForVirtStore")) {//$NON-NLS-1$
            List<GlusterVolumeEntity> selectedVolumes = new ArrayList<GlusterVolumeEntity>();
            for(Object selectedVolume : getSelectedItems()) {
                selectedVolumes.add((GlusterVolumeEntity) selectedVolume);
            }
            optimizeVolumesForVirtStore(selectedVolumes);
        }
    }

    private void startVolumeProfiling() {
        if (getSelectedItems() == null) {
            return;
        }
        List<GlusterVolumeEntity> selectedVolumesList = Linq.<GlusterVolumeEntity> cast(getSelectedItems());
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (GlusterVolumeEntity currentSelectedVolume : selectedVolumesList) {
            GlusterVolumeParameters parameter = new GlusterVolumeParameters(currentSelectedVolume.getId());
            parameters.add(parameter);
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.StartGlusterVolumeProfile, parameters);
    }

    private void stopVolumeProfiling() {
        if (getSelectedItems() == null) {
            return;
        }
        List<GlusterVolumeEntity> selectedVolumesList = Linq.<GlusterVolumeEntity> cast(getSelectedItems());
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (GlusterVolumeEntity currentSelectedVolume : selectedVolumesList) {
            GlusterVolumeParameters parameter = new GlusterVolumeParameters(currentSelectedVolume.getId());
            parameters.add(parameter);
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.StopGlusterVolumeProfile, parameters);
    }

    private void closeConfirmationWindow() {
        if(getConfirmWindow() == null) {
            return;
        }
        getConfirmWindow().stopProgress();
        setConfirmWindow(null);
    }

    private void startRebalance() {
        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeRebalanceParameters(volume.getId(), false, false));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.StartRebalanceGlusterVolume, list);
    }

    private void stopRebalance() {
        if (getSelectedItem() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getSelectedItem();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmStopVolumeRebalanceTitle());
        model.setHelpTag(HelpTag.volume_rebalance_stop);
        model.setHashName("volume_rebalance_stop"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getMessages().confirmStopVolumeRebalance(
                volumeEntity.getName()));

        UICommand okCommand = new UICommand("onStopRebalance", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onStopRebalance() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        model.startProgress(null);

        final GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getSelectedItem();
        GlusterVolumeRebalanceParameters param = new GlusterVolumeRebalanceParameters(volumeEntity.getId(), false, false);

        Frontend.getInstance().runAction(VdcActionType.StopRebalanceGlusterVolume, param, new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                ConfirmationModel localModel = (ConfirmationModel) getConfirmWindow();
                localModel.stopProgress();
                setConfirmWindow(null);
                if (result.getReturnValue().getSucceeded()) {
                    showRebalanceStatus();
                }
            }
        });
    }

    private void showRebalanceStatus() {
        if (getSelectedItem() == null) {
            return;
        }
        final ConfirmationModel cModel = new ConfirmationModel();
        final GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getSelectedItem();
        setConfirmWindow(cModel);
        cModel.setTitle(ConstantsManager.getInstance().getConstants().rebalanceStatusTitle());
        cModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());//$NON-NLS-1$
        cModel.setHelpTag(HelpTag.volume_rebalance_status);
        cModel.setHashName("volume_rebalance_status"); //$NON-NLS-1$

        final UICommand rebalanceStatusOk = new UICommand("rebalanceNotStarted", VolumeListModel.this);//$NON-NLS-1$
        rebalanceStatusOk.setTitle(ConstantsManager.getInstance().getConstants().ok());
        rebalanceStatusOk.setIsCancel(true);
        cModel.getCommands().add(rebalanceStatusOk);

        AsyncDataProvider.getInstance().getGlusterRebalanceStatus(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                cModel.stopProgress();
                VdcQueryReturnValue vdcValue = (VdcQueryReturnValue) returnValue;
                GlusterVolumeTaskStatusEntity rebalanceStatusEntity =vdcValue.getReturnValue();
                if ((rebalanceStatusEntity == null) || !(vdcValue.getSucceeded())) {
                    cModel.setMessage(ConstantsManager.getInstance().getMessages().rebalanceStatusFailed(volumeEntity.getName()));
                } else {
                    setConfirmWindow(null);
                    if (getWindow() == null) {
                        VolumeRebalanceStatusModel rebalanceStatusModel =
                                new VolumeRebalanceStatusModel(volumeEntity);
                        rebalanceStatusModel.setTitle(ConstantsManager.getInstance()
                                .getConstants()
                                .volumeRebalanceStatusTitle());
                        setWindow(rebalanceStatusModel);
                        rebalanceStatusModel.setHelpTag(HelpTag.volume_rebalance_status);
                        rebalanceStatusModel.setHashName("volume_rebalance_status"); //$NON-NLS-1$
                        rebalanceStatusModel.getVolume().setEntity(volumeEntity.getName());
                        rebalanceStatusModel.getCluster().setEntity(volumeEntity.getVdsGroupName());

                        UICommand stopRebalanceFromStatus = new UICommand("stop_rebalance_from_status", VolumeListModel.this);//$NON-NLS-1$
                        stopRebalanceFromStatus.setTitle(ConstantsManager.getInstance().getConstants().stopRebalance());
                        rebalanceStatusModel.getCommands().add(stopRebalanceFromStatus);
                        rebalanceStatusModel.setStopReblanceFromStatus(stopRebalanceFromStatus);

                        UICommand cancelRebalance = new UICommand("CancelRebalanceStatus", VolumeListModel.this);//$NON-NLS-1$
                        cancelRebalance.setTitle(ConstantsManager.getInstance().getConstants().close());
                        cancelRebalance.setIsCancel(true);
                        rebalanceStatusModel.getCommands().add(cancelRebalance);

                        rebalanceStatusModel.showStatus(rebalanceStatusEntity);
                    }else {
                        VolumeRebalanceStatusModel statusModel = (VolumeRebalanceStatusModel) getWindow();
                        statusModel.getCommands().get(0).setIsExecutionAllowed(false);
                        statusModel.showStatus(rebalanceStatusEntity);
                    }

                }
            }
        }),
                volumeEntity.getClusterId(),
                volumeEntity.getId());
    }

    private void showVolumeProfiling() {
        if(getSelectedItem() == null || getWindow()!= null) {
            return;
        }
        GlusterVolumeEntity selectedVolume = (GlusterVolumeEntity)getSelectedItem();
        VolumeProfileStatisticsModel profileStatsModel = new VolumeProfileStatisticsModel(selectedVolume.getClusterId(), selectedVolume.getId(), selectedVolume.getName());

        setWindow(profileStatsModel);
        setHelpTag(HelpTag.volume_profile_statistics);
        setHashName("volume_profile_statistics"); //$NON-NLS-1$

        profileStatsModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());//$NON-NLS-1$

        UICommand closeProfilingStats = new UICommand("closeProfileStats", VolumeListModel.this);//$NON-NLS-1$
        closeProfilingStats.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeProfilingStats.setIsCancel(true);
        profileStatsModel.getCommands().add(closeProfilingStats);

        profileStatsModel.queryBackend(true);
        profileStatsModel.queryBackend(false);
    }

    private void cancelRebalanceStatus() {
        if (getWindow() == null)
        {
            return;
        }
        ((VolumeRebalanceStatusModel)getWindow()).cancelRefresh();
        cancel();
    }

    private void optimizeForVirtStore() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            return;
        }
        ArrayList<GlusterVolumeEntity> volumesForOptimiseForVirtStore = new ArrayList<GlusterVolumeEntity>();
        Boolean isDiscouragedVolumePresent = false;
        StringBuilder discouragedConfigVolumeNamesBuilder = new StringBuilder();
        discouragedConfigVolumeNamesBuilder.append(constants.optimiseForVirtStoreWarning());
        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            volumesForOptimiseForVirtStore.add(volume);
            if(volume.getReplicaCount() != 3) {
                discouragedConfigVolumeNamesBuilder.append(volume.getName() + "\n");//$NON-NLS-1$
                isDiscouragedVolumePresent = true;
            }
        }
        discouragedConfigVolumeNamesBuilder.append(constants.optimiseForVirtStoreContinueMessage());

        if(isDiscouragedVolumePresent) {
            ConfirmationModel cModel = new ConfirmationModel();

            cModel.setMessage(discouragedConfigVolumeNamesBuilder.toString());

            UICommand cancelOptimiseVirtStoreCommand = new UICommand("CancelOptimizeForVirtStore", this);//$NON-NLS-1$
            cancelOptimiseVirtStoreCommand.setTitle(constants.doNotOptimiseForVirtStore());
            cancelOptimiseVirtStoreCommand.setIsCancel(true);
            cModel.getCommands().add(cancelOptimiseVirtStoreCommand);

            UICommand confirmOptimiseForVirtStoreCommand = new UICommand("ConfirmOptimiseForVirtStore", this);//$NON-NLS-1$
            confirmOptimiseForVirtStoreCommand.setTitle(constants.continueOptimiseForVirtStore());
            confirmOptimiseForVirtStoreCommand.setIsDefault(true);
            cModel.getCommands().add(confirmOptimiseForVirtStoreCommand);
            setConfirmWindow(cModel);
        } else {
            optimizeVolumesForVirtStore(volumesForOptimiseForVirtStore);
        }
    }

    private void optimizeVolumesForVirtStore(final List<GlusterVolumeEntity> volumeList) {
        if(getConfirmWindow() != null) {
            setConfirmWindow(null);
        }
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, final Object result)
            {
                AsyncQuery aQueryInner = new AsyncQuery();
                aQueryInner.setModel(this);
                aQueryInner.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object modelInner, final Object resultInner)
                    {

                        AsyncQuery aQueryInner1 = new AsyncQuery();
                        aQueryInner1.setModel(this);
                        aQueryInner1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object modelInner1, Object resultInner1)
                            {
                                String optionGroupVirt = (String) result;
                                String optionOwnerUserVirt = (String) resultInner;
                                String optionOwnerGroupVirt = (String) resultInner1;

                                ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
                                for (GlusterVolumeEntity volume : volumeList)
                                {
                                    Guid volumeId = volume.getId();

                                    list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "group", optionGroupVirt)));//$NON-NLS-1$

                                    list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "storage.owner-uid", optionOwnerUserVirt)));//$NON-NLS-1$

                                    list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "storage.owner-gid", optionOwnerGroupVirt)));//$NON-NLS-1$

                                    final GlusterVolumeOptionEntity checkOption = getOption(volumeId, "network.ping-timeout", "10");//$NON-NLS-1$//$NON-NLS-2$
                                    List<PredicateFilter<GlusterVolumeOptionEntity>> predicaetFilters = Collections.singletonList(new PredicateFilter<GlusterVolumeOptionEntity>(new Predicate<GlusterVolumeOptionEntity>() {
                                        @Override
                                        public boolean evaluate(GlusterVolumeOptionEntity obj) {
                                            return obj.getKey().equalsIgnoreCase(checkOption.getKey());
                                        }
                                    }));
                                    if(!isOptionEnabledOnVolume(volume, predicaetFilters)) {
                                        list.add(new GlusterVolumeOptionParameters(checkOption));//$NON-NLS-1$
                                    }
                                }
                                Frontend.getInstance().runMultipleAction(VdcActionType.SetGlusterVolumeOption, list);
                            }
                        };

                        AsyncDataProvider.getInstance().getConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionOwnerGroupVirtValue,
                                                                                                 AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                                aQueryInner1);
                    }
                };
                AsyncDataProvider.getInstance().getConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionOwnerUserVirtValue,
                                                                                         AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                        aQueryInner);
            }
        };
        AsyncDataProvider.getInstance().getConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionGroupVirtValue,
                                                                                 AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                aQuery);
    }

    private boolean isOptionEnabledOnVolume(GlusterVolumeEntity volume, List<PredicateFilter<GlusterVolumeOptionEntity>> predicates) {
        List<GlusterVolumeOptionEntity> volumeOptionsEnabled = new ArrayList<GlusterVolumeOptionEntity>(volume.getOptions());
        List<GlusterVolumeOptionEntity> filteredOptions = volumeOptionsEnabled;
        for(PredicateFilter<GlusterVolumeOptionEntity> predicate  : predicates) {
             filteredOptions = ListUtils.filter(filteredOptions, predicate);
        }
        if(filteredOptions.size() > 0) {
            return true;
        }
        return false;
    }

    private GlusterVolumeOptionEntity getOption(Guid volumeId, String key, String value) {
        return new GlusterVolumeOptionEntity(volumeId, key, value);
    }

    private void stop() {
        if (getWindow() != null)
        {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmStopVolume());
        model.setHelpTag(HelpTag.volume_stop);
        model.setHashName("volume_stop"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().stopVolumeMessage());
        model.setNote(ConstantsManager.getInstance().getConstants().stopVolumeWarning());

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<String>();
        for (GlusterVolumeEntity item : Linq.<GlusterVolumeEntity> cast(getSelectedItems()))
        {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("onStop", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onStop()
    {
        if (getWindow() == null)
        {
            return;
        }
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.StopGlusterVolume, list,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }

                }, model);
    }

    private void start() {
        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.StartGlusterVolume, list);
    }


    private void onCreateVolume() {
        VolumeModel volumeModel = (VolumeModel) getWindow();

        if (!volumeModel.validate())
        {
            return;
        }

        Guid clusterId = volumeModel.getCluster().getSelectedItem().getId();
        final GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setClusterId(clusterId);
        volume.setName(volumeModel.getName().getEntity());
        GlusterVolumeType type = volumeModel.getTypeList().getSelectedItem();

        if (type.isStripedType()) {
            volume.setStripeCount(volumeModel.getStripeCountValue());
        }
        if (type.isReplicatedType()) {
            volume.setReplicaCount(volumeModel.getReplicaCountValue());
        }

        volume.setVolumeType(type);

        if (volumeModel.getTcpTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.TCP);
        }
        if (volumeModel.getRdmaTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.RDMA);
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<GlusterBrickEntity>();

        for (Object model : volumeModel.getBricks().getItems())
        {
            brickList.add((GlusterBrickEntity) ((EntityModel) model).getEntity());
        }

        volume.setBricks(brickList);

        if (volumeModel.getNfs_accecssProtocol().getEntity()) {
            volume.enableNFS();
        }
        else {
            volume.disableNFS();
        }

        if (volumeModel.getCifs_accecssProtocol().getEntity()) {
            volume.enableCifs();
        }
        else {
            volume.disableCifs();
        }

        volume.setAccessControlList(volumeModel.getAllowAccess().getEntity());

        volumeModel.startProgress(null);

        CreateGlusterVolumeParameters parameter =
                new CreateGlusterVolumeParameters(volume, volumeModel.isForceAddBricks());

        Frontend.getInstance().runAction(VdcActionType.CreateGlusterVolume, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                VolumeListModel localModel = (VolumeListModel) result.getState();
                localModel.postOnCreateVolume(result.getReturnValue(), volume);
            }
        }, this);
    }

    public void postOnCreateVolume(VdcReturnValueBase returnValue, GlusterVolumeEntity volume)
    {
        VolumeModel model = (VolumeModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            if (model.getOptimizeForVirtStore().getEntity()) {
                optimizeVolumesForVirtStore(Arrays.asList(volume));
            }
        }
    }

    @Override
    protected String getListName() {
        return "VolumeListModel"; //$NON-NLS-1$
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
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("volume"); //$NON-NLS-1$
    }
}
