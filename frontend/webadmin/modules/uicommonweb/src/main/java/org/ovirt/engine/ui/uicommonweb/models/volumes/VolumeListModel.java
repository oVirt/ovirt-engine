package org.ovirt.engine.ui.uicommonweb.models.volumes;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterVolumeSnapshotConfigParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils.VolumeStatus;
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
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterClusterSnapshotConfigModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepActionConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepCreateModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotConfigModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel.EndDateOptions;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeProfileStatisticsModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeRebalanceStatusModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeSnapshotOptionModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;

public class VolumeListModel extends ListWithSimpleDetailsModel<Void, GlusterVolumeEntity> {

    public static final Integer REPLICATE_COUNT_DEFAULT = 3;
    public static final Integer STRIPE_COUNT_DEFAULT = 4;

    private UICommand newVolumeCommand;

    public UICommand getNewVolumeCommand() {
        return newVolumeCommand;
    }

    private void setNewVolumeCommand(UICommand value) {
        newVolumeCommand = value;
    }

    private UICommand removeVolumeCommand;

    public UICommand getRemoveVolumeCommand() {
        return removeVolumeCommand;
    }

    private void setRemoveVolumeCommand(UICommand value) {
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
    private UICommand configureClusterSnapshotOptionsCommand;
    private UICommand configureVolumeSnapshotOptionsCommand;
    private UICommand createSnapshotCommand;
    private UICommand editSnapshotScheduleCommand;
    private UICommand newGeoRepSessionCommand;

    private String glusterMetaVolumeName;

    public UICommand getNewGeoRepSessionCommand() {
        return newGeoRepSessionCommand;
    }

    public void setNewGeoRepSessionCommand(UICommand newGeoRepSessionCommand) {
        this.newGeoRepSessionCommand = newGeoRepSessionCommand;
    }

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

    public UICommand getConfigureClusterSnapshotOptionsCommand() {
        return this.configureClusterSnapshotOptionsCommand;
    }

    public void setConfigureClusterSnapshotOptionsCommand(UICommand command) {
        this.configureClusterSnapshotOptionsCommand = command;
    }

    public UICommand getConfigureVolumeSnapshotOptionsCommand() {
        return this.configureVolumeSnapshotOptionsCommand;
    }

    public void setConfigureVolumeSnapshotOptionsCommand(UICommand command) {
        this.configureVolumeSnapshotOptionsCommand = command;
    }

    public UICommand getCreateSnapshotCommand() {
        return this.createSnapshotCommand;
    }

    public void setCreateSnapshotCommand(UICommand command) {
        this.createSnapshotCommand = command;
    }

    public UICommand getEditSnapshotScheduleCommand() {
        return this.editSnapshotScheduleCommand;
    }

    public void setEditSnapshotScheduleCommand(UICommand command) {
        this.editSnapshotScheduleCommand = command;
    }

    private final VolumeBrickListModel brickListModel;

    public VolumeBrickListModel getBrickListModel() {
        return brickListModel;
    }

    private final VolumeGeoRepListModel geoRepListModel;

    public VolumeGeoRepListModel getGeoRepListModel() {
        return geoRepListModel;
    }

    private final GlusterVolumeSnapshotListModel snapshotListModel;

    public GlusterVolumeSnapshotListModel getSnapshotListModel() {
        return snapshotListModel;
    }

    private final VolumeGeneralModel generalModel;

    public VolumeGeneralModel getGeneralModel() {
        return generalModel;
    }

    private final VolumeParameterListModel parameterListModel;

    public VolumeParameterListModel getParameterListModel() {
        return parameterListModel;
    }

    private final PermissionListModel<GlusterVolumeEntity> permissionListModel;

    public PermissionListModel<GlusterVolumeEntity> getPermissionListModel() {
        return permissionListModel;
    }

    private final VolumeEventListModel eventListModel;

    public VolumeEventListModel getEventListModel() {
        return eventListModel;
    }

    @Inject
    public VolumeListModel(final VolumeBrickListModel volumeBrickListModel,
            final VolumeGeneralModel volumeGeneralModel,
            final VolumeParameterListModel volumeParameterListModel,
            final PermissionListModel<GlusterVolumeEntity> permissionListModel,
            final VolumeEventListModel volumeEventListModel,
            final VolumeGeoRepListModel geoRepListModel,
            final GlusterVolumeSnapshotListModel snapshotListModel) {
        this.brickListModel = volumeBrickListModel;
        this.geoRepListModel = geoRepListModel;
        this.snapshotListModel = snapshotListModel;
        this.generalModel = volumeGeneralModel;
        this.parameterListModel = volumeParameterListModel;
        this.permissionListModel = permissionListModel;
        this.eventListModel = volumeEventListModel;
        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().volumesTitle());
        setApplicationPlace(WebAdminApplicationPlaces.volumeMainPlace);

        setDefaultSearchString(SearchStringMapping.VOLUMES_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
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
        setConfigureClusterSnapshotOptionsCommand(new UICommand("configureClusterSnapshotOptions", this)); //$NON-NLS-1$
        setConfigureVolumeSnapshotOptionsCommand(new UICommand("configureVolumeSnapshotOptions", this)); //$NON-NLS-1$
        setCreateSnapshotCommand(new UICommand("createSnapshot", this)); //$NON-NLS-1$
        setEditSnapshotScheduleCommand(new UICommand("editSnapshotSchedule", this)); //$NON-NLS-1$
        setNewGeoRepSessionCommand(new UICommand("createGeoRepSession", this));//$NON-NLS-1$

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

        // Get the meta volume name
        AsyncDataProvider.getInstance()
                .getConfigFromCache(new GetConfigurationValueParameters(ConfigValues.GlusterMetaVolumeName,
                        AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                        new AsyncQuery<String>(returnValue -> glusterMetaVolumeName = returnValue));
    }

    private void setDetailList() {
        List<HasEntity<GlusterVolumeEntity>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(parameterListModel);
        list.add(getBrickListModel());
        list.add(getGeoRepListModel());
        list.add(permissionListModel);
        list.add(eventListModel);
        list.add(getSnapshotListModel());
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
        AsyncDataProvider.getInstance().getDataCenterByClusterServiceList(new AsyncQuery<>(dataCenters -> {
            VolumeModel innerVolumeModel = (VolumeModel) getWindow();
            final UIConstants constants = ConstantsManager.getInstance().getConstants();
            innerVolumeModel.getDataCenter().setItems(dataCenters);
            innerVolumeModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));

            UICommand command = UICommand.createDefaultOkUiCommand("onCreateVolume", VolumeListModel.this); //$NON-NLS-1$
            innerVolumeModel.getCommands().add(command);
            innerVolumeModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", VolumeListModel.this)); //$NON-NLS-1$
        }), false, true);

    }

    private boolean isMetaVolumeInList(List<GlusterVolumeEntity> volumes) {
        for (GlusterVolumeEntity volume : volumes) {
            if (volume.getName().equals(glusterMetaVolumeName)) {
                return true;
            }
        }
        return false;
    }

    private void removeVolume() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeVolumesTitle());
        model.setHelpTag(HelpTag.remove_volume);
        model.setHashName("remove_volume"); //$NON-NLS-1$
        if (isMetaVolumeInList(getSelectedItems())) {
            model.setNote(ConstantsManager.getInstance().getConstants().removeMetaVolumeWarning());
        } else {
            model.setNote(ConstantsManager.getInstance().getConstants().removeVolumesWarning());
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        for (GlusterVolumeEntity item : getSelectedItems()) {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemoveVolume() {
        if (getWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.DeleteGlusterVolume, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.GlusterVolume, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
        GlusterVolumeEntity selectedVolume = provideDetailModelEntity(getSelectedItem());
        getBrickListModel().setVolumeEntity(selectedVolume);
        getGeoRepListModel().setEntity(selectedVolume);
        getSnapshotListModel().setEntity(selectedVolume);
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
        boolean allowConfigureClusterSnapshotOptions = true;
        boolean allowConfigureVolumeSnapshotOptions = false;
        boolean allowCreateSnapshot = false;
        boolean allowEditSnapshotSchedule = false;
        boolean allowCreateGeoRepSession = false;

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowStart = false;
            allowStop = false;
            allowRemove = false;
            allowStartRebalance = false;
            allowStopRebalance = false;
            allowStatusRebalance = false;
            allowOptimize = false;
        } else {
            List<GlusterVolumeEntity> list = getSelectedItems();
            allowStartProfiling = isStartProfileAvailable(list);
            allowStopProfiling = isStopProfileAvailable(list);
            for (GlusterVolumeEntity volume : list) {
                if (volume.getStatus() == GlusterStatus.UP) {
                    VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
                    allowStart = status == VolumeStatus.ALL_BRICKS_DOWN || status == VolumeStatus.SOME_BRICKS_DOWN;
                    allowRemove = false;
                    if (!volume.getVolumeType().isDistributedType()) {
                        allowStartRebalance = false;
                    }
                } else if (volume.getStatus() == GlusterStatus.DOWN) {
                    allowStop = false;
                    allowStartRebalance = false;
                }

                GlusterAsyncTask asyncTask = volume.getAsyncTask();
                if (asyncTask != null) {
                    allowStartRebalance =
                            allowStartRebalance && (
                                    asyncTask.getStatus() == null ? asyncTask.getJobStatus() != JobExecutionStatus.STARTED
                                    : asyncTask.getStatus() != JobExecutionStatus.STARTED);
                }
            }

            if (list.size() == 1) {
                GlusterVolumeEntity volumeEntity = list.get(0);
                GlusterAsyncTask asyncTask = volumeEntity.getAsyncTask();
                allowStopRebalance =
                        volumeEntity.getStatus() == GlusterStatus.UP && asyncTask != null
                                && asyncTask.getType() == GlusterTaskType.REBALANCE
                                && asyncTask.getStatus() == JobExecutionStatus.STARTED;
                allowConfigureVolumeSnapshotOptions = volumeEntity.getStatus() == GlusterStatus.UP;
                allowCreateGeoRepSession = volumeEntity.getStatus() == GlusterStatus.UP;
                allowCreateSnapshot = isCreateSnapshotAvailable(volumeEntity);
            } else {
                allowStopRebalance = false;
            }
            allowStatusRebalance = getRebalanceStatusAvailability(getSelectedItems());
            allowProfileStatisticsDetails = getProfileStatisticsAvailability(list);
            allowEditSnapshotSchedule = isEditSnapshotScheduleAvailable(list);
        }

        getStartCommand().setIsExecutionAllowed(allowStart);
        getStopCommand().setIsExecutionAllowed(allowStop);
        getRemoveVolumeCommand().setIsExecutionAllowed(allowRemove);
        getStartRebalanceCommand().setIsExecutionAllowed(allowStartRebalance);
        getStopRebalanceCommand().setIsExecutionAllowed(allowStopRebalance);
        getStatusRebalanceCommand().setIsExecutionAllowed(allowStatusRebalance);
        getOptimizeForVirtStoreCommand().setIsExecutionAllowed(allowOptimize);
        getConfigureClusterSnapshotOptionsCommand().setIsExecutionAllowed(allowConfigureClusterSnapshotOptions);
        getConfigureVolumeSnapshotOptionsCommand().setIsExecutionAllowed(allowConfigureVolumeSnapshotOptions);
        getCreateSnapshotCommand().setIsExecutionAllowed(allowCreateSnapshot);
        getEditSnapshotScheduleCommand().setIsExecutionAllowed(allowEditSnapshotSchedule);

        getNewVolumeCommand().setIsAvailable(true);
        getRemoveVolumeCommand().setIsAvailable(true);
        getStartVolumeProfilingCommand().setIsExecutionAllowed(allowStartProfiling);
        getStopVolumeProfilingCommand().setIsExecutionAllowed(allowStopProfiling);
        getShowVolumeProfileDetailsCommand().setIsExecutionAllowed(allowProfileStatisticsDetails);
        getNewGeoRepSessionCommand().setIsExecutionAllowed(allowCreateGeoRepSession);
    }

    private boolean isCreateSnapshotAvailable(GlusterVolumeEntity volume) {
        if (volume.getStatus() == GlusterStatus.UP) {
            List<GlusterBrickEntity> bricks = volume.getBricks();
            for (GlusterBrickEntity brick : bricks) {
                if (brick.getStatus() != GlusterStatus.UP) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isEditSnapshotScheduleAvailable(List<GlusterVolumeEntity> list) {
        return (list.size() == 1) && (list.get(0).getStatus() == GlusterStatus.UP) && list.get(0)
                .getSnapshotScheduled();
    }

    private boolean isStopProfileAvailable(List<GlusterVolumeEntity> list) {
        if (getSelectedItems().size() == 0) {
            return false;
        } else {
            for (GlusterVolumeEntity volumeEntity : list) {
                if (volumeEntity.getStatus() == GlusterStatus.DOWN) {
                    return false;
                }
                if ((volumeEntity.getOptionValue("diagnostics.latency-measurement") == null)|| !volumeEntity.getOptionValue("diagnostics.latency-measurement").equals("on")) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
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
                if ((volumeEntity.getOptionValue("diagnostics.latency-measurement") != null) && volumeEntity.getOptionValue("diagnostics.latency-measurement").equals("on")) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
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
        } else if (command.equals(getRemoveVolumeCommand())) {
            removeVolume();
        } else if(command.getName().equals("closeConfirmationWindow")) {//$NON-NLS-1$
            closeConfirmationWindow();
        } else if (command.getName().equalsIgnoreCase("Cancel")) { //$NON-NLS-1$
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
        } else if (command.equals(getNewGeoRepSessionCommand())) {
            createNewGeoRepSession();
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
            List<GlusterVolumeEntity> selectedVolumes = new ArrayList<>();
            for(Object selectedVolume : getSelectedItems()) {
                selectedVolumes.add((GlusterVolumeEntity) selectedVolume);
            }
            optimizeVolumesForVirtStore(selectedVolumes);
        } else if (command.equals(getConfigureClusterSnapshotOptionsCommand())) {
            configureClusterSnapshotOptions();
        } else if (command.getName().equalsIgnoreCase("confirmConfigureClusterSnapshotOptions")) {//$NON-NLS-1$
            confirmConfigureClusterSnapshotOptions();
        } else if (command.getName().equalsIgnoreCase("onConfigureClusterSnapshotOptions")) {//$NON-NLS-1$
            onConfigureClusterSnapshotOptions();
        } else if (command.equals(getConfigureVolumeSnapshotOptionsCommand())) {
            configureVolumeSnapshotOptions();
        } else if (command.getName().equalsIgnoreCase("confirmConfigureVolumeSnapshotOptions")) {//$NON-NLS-1$
            confirmConfigureVolumeSnapshotOptions();
        } else if (command.getName().equalsIgnoreCase("onConfigureVolumeSnapshotOptions")) {//$NON-NLS-1$
            onConfigureVolumeSnapshotOptions();
        } else if (command.equals(getCreateSnapshotCommand())) {
            setWindow(null);
            createVolumeSnapshot();
        } else if (command.getName().equalsIgnoreCase("onCreateSnapshot")) {//$NON-NLS-1$
            onCreateSnapshot();
        } else if (command.equals(getEditSnapshotScheduleCommand())) {
            editSnapshotSchedule();
        } else if (command.getName().equalsIgnoreCase("onEditSnapshotSchedule")) {//$NON-NLS-1$
            onEditSnapshotSchedule();
        } else if (command.getName().equalsIgnoreCase("onEditSnapshotScheduleInternal")) {//$NON-NLS-1$
            onEditSnapshotScheduleInternal();
        } else if (command.getName().equals("startVolumeWithForceOption")) {//$NON-NLS-1$
            prepareForStartVolume(false);
        } else if (command.getName().equalsIgnoreCase("onCreateSession")) {//$NON-NLS-1$
            onCreateSession();
        } else if (command.getName().equalsIgnoreCase("closeWindow")) {//$NON-NLS-1$
            setWindow(null);
        }
    }

    public void createNewGeoRepSession() {
        if (getWindow() != null) {
            return;
        }
        GlusterVolumeEntity volumeEntity = getSelectedItem();
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        final GlusterVolumeGeoRepCreateModel geoRepCreateModel =
                new GlusterVolumeGeoRepCreateModel(volumeEntity);
        setWindow(geoRepCreateModel);
        geoRepCreateModel.getSlaveUserName().setEntity(constants.rootUser());
        geoRepCreateModel.getShowEligibleVolumes().setEntity(true);

        UICommand ok = new UICommand("onCreateSession", this);//$NON-NLS-1$
        ok.setTitle(constants.ok());
        ok.setIsDefault(true);
        geoRepCreateModel.getCommands().add(ok);

        UICommand close = new UICommand("closeWindow", this);//$NON-NLS-1$
        close.setTitle(constants.cancel());
        close.setIsCancel(true);
        geoRepCreateModel.getCommands().add(close);
    }

    private void onCreateSession() {
        final GlusterVolumeGeoRepCreateModel createModel = (GlusterVolumeGeoRepCreateModel) getWindow();
        if (!createModel.validate()) {
            return;
        }
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final UIMessages messages = ConstantsManager.getInstance().getMessages();
        createModel.startProgress();
        final Guid masterVolumeId = getSelectedItem().getId();
        final String remoteVolumeName = createModel.getSlaveVolumes().getSelectedItem().getName();
        final String remoteHostName = createModel.getSlaveHosts().getSelectedItem().getFirst();
        String remoteUserName = createModel.getSlaveUserName().getEntity();
        String remoteUserGroup = createModel.getSlaveUserGroupName().getEntity();
        final Guid remoteHostId = createModel.getSlaveHosts().getSelectedItem().getSecond();
        Frontend.getInstance()
                .runAction(ActionType.CreateGlusterVolumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                                remoteVolumeName,
                                remoteHostId,
                                remoteUserName,
                                remoteUserGroup,
                                !createModel.getShowEligibleVolumes().getEntity()),
                        result -> {
                            createModel.stopProgress();
                            if (result.getReturnValue().getSucceeded()) {
                                setWindow(null);
                                if (createModel.getStartSession().getEntity()) {
                                    initializeGeoRepActionConfirmation(constants.geoReplicationStartTitle(),
                                            HelpTag.volume_geo_rep_start_confirmation,
                                            "volume_geo_rep_start_confirmation", //$NON-NLS-1$
                                            constants.geoRepForceHelp(),
                                            messages.geoRepForceTitle(constants.startGeoRep()),
                                            "onStartGeoRepSession", //$NON-NLS-1$
                                            getSelectedItem().getName(),
                                            remoteVolumeName,
                                            remoteHostName,
                                            null);
                                    final GlusterVolumeGeoRepActionConfirmationModel cModel =
                                            (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
                                    cModel.startProgress();
                                    Frontend.getInstance()
                                            .runAction(ActionType.StartGlusterVolumeGeoRep,
                                                    new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                                                            remoteVolumeName,
                                                            remoteHostId),
                                                    result1 -> {
                                                        cModel.stopProgress();
                                                        if (!result1.getReturnValue().getSucceeded()) {
                                                            cModel.setMessage(
                                                                    result1.getReturnValue().getFault().getMessage());
                                                        } else {
                                                            setWindow(null);
                                                        }
                                                    },
                                                    this,
                                                    false);
                                }
                            }
                        },
                        this,
                        true);
    }

    public void initializeGeoRepActionConfirmation(String title,
            HelpTag helpTag,
            String hashName,
            String forceHelp,
            String forceLabelText,
            String commandName,
            String masterVolumeName,
            String slaveVolumeName,
            String slaveHostName,
            String message) {
        GlusterVolumeGeoRepActionConfirmationModel cModel;
        if (getWindow() != null) {
            if (getWindow() instanceof GlusterVolumeGeoRepActionConfirmationModel) {
                cModel = (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
            } else {
                return;
            }
        } else {
            cModel = new GlusterVolumeGeoRepActionConfirmationModel();
            cModel.setTitle(title);
        }
        cModel.setHelpTag(helpTag);
        cModel.setHashName(hashName);

        setWindow(cModel);

        GlusterVolumeGeoRepActionConfirmationModel
                .callInitWindow(cModel, masterVolumeName, slaveVolumeName, slaveHostName);

        cModel.setActionConfirmationMessage(message);
        cModel.setForceHelp(forceHelp);
        cModel.setForceLabel(forceLabelText);

        List<UICommand> geoRepActionCommands = Arrays.asList(UICommand.createDefaultOkUiCommand(commandName, this),
                UICommand.createCancelUiCommand("closeWindow", this));//$NON-NLS-1$

        if (cModel.getCommands().size() > 0) {
            cModel.setCommands(geoRepActionCommands);
        } else {
            cModel.getCommands().addAll(geoRepActionCommands);
        }
    }

    public void createVolumeSnapshot() {
        if (getWindow() != null) {
            return;
        }
        GlusterVolumeEntity volumeEntity = getSelectedItem();
        GlusterVolumeSnapshotModel snapshotModel = GlusterVolumeSnapshotModel.createVolumeSnapshotModel(this, volumeEntity);
        setWindow(snapshotModel);

        snapshotModel.startProgress();

        snapshotModel.getClusterName().setEntity(volumeEntity.getClusterName());
        snapshotModel.getVolumeName().setEntity(volumeEntity.getName());
        UICommand okCommand = UICommand.createDefaultOkUiCommand("onCreateSnapshot", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(cancelCommand);
        AsyncDataProvider.getInstance().getIsGlusterVolumeSnapshotCliScheduleEnabled(new AsyncQuery<>(
                isCliScheduleEnabled -> {
                    snapshotModel.getDisableCliSchedule().setEntity(isCliScheduleEnabled);
                    snapshotModel.stopProgress();
                }), volumeEntity.getClusterId());

   }

    private void onCreateSnapshot() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (!snapshotModel.validate(false)) {
            return;
        }

        if (!snapshotModel.isScheduleTabVisible()
                || snapshotModel.getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
            createNewSnapshot(snapshotModel);
        } else {
            scheduleSnapshot(snapshotModel, false);
        }
    }

    private Time getExecutionTime(GlusterVolumeSnapshotModel model) {
        int hours = model.getExecutionTime().getEntity().getHours();
        int minutes = model.getExecutionTime().getEntity().getMinutes();

        return new Time(hours, minutes, 0);
    }

    private void scheduleSnapshot(final GlusterVolumeSnapshotModel snapshotModel, boolean reschedule) {
        GlusterVolumeEntity volumeEntity = getSelectedItem();

        final GlusterVolumeSnapshotSchedule schedule = new GlusterVolumeSnapshotSchedule();
        schedule.setSnapshotNamePrefix(snapshotModel.getSnapshotName().getEntity());
        schedule.setSnapshotDescription(snapshotModel.getDescription().getEntity());
        schedule.setClusterId(volumeEntity.getClusterId());
        schedule.setVolumeId(volumeEntity.getId());
        switch (snapshotModel.getRecurrence().getSelectedItem()) {
        case INTERVAL:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.INTERVAL);
            schedule.setInterval(Integer.valueOf(snapshotModel.getInterval().getSelectedItem()));
            break;
        case HOURLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.HOURLY);
            break;
        case DAILY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.DAILY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            break;
        case WEEKLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.WEEKLY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            StringBuilder sb = new StringBuilder();
            for (DayOfWeek day : snapshotModel.getDaysOfTheWeek().getSelectedItem()) {
                sb.append(day.name().substring(0, 3));
                sb.append(',');//$NON-NLS-1$
            }
            schedule.setDays(sb.toString());
            break;
        case MONTHLY:
            schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
            schedule.setExecutionTime(getExecutionTime(snapshotModel));
            schedule.setDays(snapshotModel.getDaysOfMonth().getSelectedItem());
            break;
        }

        Date startAt = snapshotModel.getStartAt().getEntity();
        schedule.setStartDate(startAt);
        schedule.setTimeZone(snapshotModel.getTimeZones().getSelectedItem().getKey());

        if (snapshotModel.getEndByOptions().getSelectedItem() == EndDateOptions.NoEndDate) {
            schedule.setEndByDate(null);
        } else {
            schedule.setEndByDate(snapshotModel.getEndDate().getEntity());
        }

        ScheduleGlusterVolumeSnapshotParameters params =
                new ScheduleGlusterVolumeSnapshotParameters(schedule, snapshotModel.getDisableCliSchedule().getEntity());
        snapshotModel.startProgress();

        ActionType actionType = null;
        if (reschedule) {
            actionType = ActionType.RescheduleGlusterVolumeSnapshot;
        } else {
            actionType = ActionType.ScheduleGlusterVolumeSnapshot;
        }

        Frontend.getInstance().runAction(actionType,
                params,
                result -> {
                    VolumeListModel localModel =
                            (VolumeListModel) result.getState();
                    snapshotModel.stopProgress();
                    localModel.postSnapshotAction(result.getReturnValue());
                },
                this, snapshotModel.getDisableCliSchedule().getEntity());
    }

    private void createNewSnapshot(final GlusterVolumeSnapshotModel snapshotModel) {
        GlusterVolumeEntity volumeEntity = getSelectedItem();

        final GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setClusterId(volumeEntity.getClusterId());
        snapshot.setSnapshotName(snapshotModel.getSnapshotName().getEntity());
        snapshot.setVolumeId(volumeEntity.getId());
        snapshot.setDescription(snapshotModel.getDescription().getEntity());

        CreateGlusterVolumeSnapshotParameters parameter =
                new CreateGlusterVolumeSnapshotParameters(snapshot, false);

        snapshotModel.startProgress();
        Frontend.getInstance().runAction(ActionType.CreateGlusterVolumeSnapshot,
                parameter,
                result -> {
                          VolumeListModel localModel =
                            (VolumeListModel) result.getState();
                    snapshotModel.stopProgress();
                    localModel.postSnapshotAction(result.getReturnValue());
                },
                this);
    }

    public void postSnapshotAction(ActionReturnValue returnValue) {
        if (returnValue != null && returnValue.getSucceeded()) {
            setWindow(null);
        }
    }

    public void editSnapshotSchedule() {
        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        final GlusterVolumeSnapshotModel snapshotModel =
                new GlusterVolumeSnapshotModel(true, true);
        snapshotModel.setHelpTag(HelpTag.edit_volume_snapshot_schedule);
        snapshotModel.setHashName("edit_volume_snapshot_schedule"); //$NON-NLS-1$
        snapshotModel.setTitle(constants.editVolumeSnapshotScheduleTitle());
        setWindow(snapshotModel);

        snapshotModel.startProgress();

        AsyncDataProvider.getInstance().getVolumeSnapshotSchedule(new AsyncQuery<>(new AsyncCallback<GlusterVolumeSnapshotSchedule>() {

            @Override
            public void onSuccess(final GlusterVolumeSnapshotSchedule schedule) {
                if (schedule == null) {
                    snapshotModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .unableToFetchVolumeSnapshotSchedule());
                    return;
                }
                snapshotModel.getSnapshotName().setEntity(schedule.getSnapshotNamePrefix());
                snapshotModel.getDescription().setEntity(schedule.getSnapshotDescription());
                snapshotModel.getRecurrence().setSelectedItem(schedule.getRecurrence());
                if (schedule.getEndByDate() == null) {
                    snapshotModel.getEndByOptions().setSelectedItem(EndDateOptions.NoEndDate);
                } else {
                    snapshotModel.getEndByOptions().setSelectedItem(EndDateOptions.HasEndDate);
                    snapshotModel.getEndDate().setEntity(schedule.getEndByDate());
                }

                if (schedule.getRecurrence() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
                    Map<String, String> timeZones = AsyncDataProvider.getInstance().getTimezones(TimeZoneType.GENERAL_TIMEZONE);
                    snapshotModel.getTimeZones().setSelectedItem(Linq.firstOrNull(timeZones.entrySet(),
                            item -> item.getKey().startsWith(schedule.getTimeZone())));
                }
                switch (schedule.getRecurrence()) {
                case INTERVAL:
                    snapshotModel.getInterval().setSelectedItem(String.valueOf(schedule.getInterval()));
                    break;
                case HOURLY:
                    break;
                case DAILY:
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                case WEEKLY:
                    List<DayOfWeek> daysList = new ArrayList<>();
                    for (String day : schedule.getDays().split(",")) {//$NON-NLS-1$
                        daysList.add(getDayOfWeek(day));
                    }
                    snapshotModel.getDaysOfTheWeek().setSelectedItem(daysList);
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                case MONTHLY:
                    snapshotModel.getDaysOfMonth().setSelectedItem(schedule.getDays());
                    snapshotModel.getExecutionTime().setEntity(getExecutionTimeValue(schedule));
                    break;
                }

                snapshotModel.getStartAt().setEntity(schedule.getStartDate());
                snapshotModel.stopProgress();
            }

            private DayOfWeek getDayOfWeek(String day) {
                switch (day) {
                case "Sun"://$NON-NLS-1$
                    return DayOfWeek.Sunday;
                case "Mon"://$NON-NLS-1$
                    return DayOfWeek.Monday;
                case "Tue"://$NON-NLS-1$
                    return DayOfWeek.Tuesday;
                case "Wed"://$NON-NLS-1$
                    return DayOfWeek.Wednesday;
                case "Thu"://$NON-NLS-1$
                    return DayOfWeek.Thursday;
                case "Fri"://$NON-NLS-1$
                    return DayOfWeek.Friday;
                case "Sat"://$NON-NLS-1$
                    return DayOfWeek.Saturday;
                default:
                    return null;
                }
            }

            private Date getExecutionTimeValue(GlusterVolumeSnapshotSchedule schedule) {
                Date dt = new Date();
                dt.setHours(schedule.getExecutionTime().getHours());
                dt.setMinutes(schedule.getExecutionTime().getMinutes());

                return dt;
            }
        }),
                getSelectedItem().getId());

        snapshotModel.getClusterName().setEntity(getSelectedItem().getClusterName());
        snapshotModel.getVolumeName().setEntity(getSelectedItem().getName());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onEditSnapshotSchedule", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        snapshotModel.getCommands().add(cancelCommand);
    }

    private void confirmDeleteVolumeSnapshotSchedule() {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .removeGlusterVolumeSnapshotScheduleConfirmationTitle());
        model.setHelpTag(HelpTag.remove_volume_snapshot_schedule_confirmation);
        model.setHashName("remove_volume_snapshot_schedule_confirmation"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().youAreAboutToRemoveSnapshotScheduleMsg());

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onEditSnapshotScheduleInternal", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("cancelConfirmation", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    public void onEditSnapshotSchedule() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (snapshotModel.getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
            confirmDeleteVolumeSnapshotSchedule();
        } else {
            onEditSnapshotScheduleInternal();
        }
    }

    private void onEditSnapshotScheduleInternal() {
        final GlusterVolumeSnapshotModel snapshotModel = (GlusterVolumeSnapshotModel) getWindow();

        if (!snapshotModel.validate(false)) {
            return;
        }

        setConfirmWindow(null);

        scheduleSnapshot(snapshotModel, true);
    }

    private void startVolumeProfiling() {
        if (getSelectedItems() == null) {
            return;
        }
        List<GlusterVolumeEntity> selectedVolumesList = getSelectedItems();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (GlusterVolumeEntity currentSelectedVolume : selectedVolumesList) {
            GlusterVolumeParameters parameter = new GlusterVolumeParameters(currentSelectedVolume.getId());
            parameters.add(parameter);
        }
        Frontend.getInstance().runMultipleAction(ActionType.StartGlusterVolumeProfile, parameters);
    }

    private void stopVolumeProfiling() {
        if (getSelectedItems() == null) {
            return;
        }
        List<GlusterVolumeEntity> selectedVolumesList = getSelectedItems();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (GlusterVolumeEntity currentSelectedVolume : selectedVolumesList) {
            GlusterVolumeParameters parameter = new GlusterVolumeParameters(currentSelectedVolume.getId());
            parameters.add(parameter);
        }
        Frontend.getInstance().runMultipleAction(ActionType.StopGlusterVolumeProfile, parameters);
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

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeRebalanceParameters(volume.getId(), false, false));
        }
        Frontend.getInstance().runMultipleAction(ActionType.StartRebalanceGlusterVolume, list, null, true, true);
    }

    private void stopRebalance() {
        if (getSelectedItem() == null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        GlusterVolumeEntity volumeEntity = getSelectedItem();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmStopVolumeRebalanceTitle());
        model.setHelpTag(HelpTag.volume_rebalance_stop);
        model.setHashName("volume_rebalance_stop"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getMessages().confirmStopVolumeRebalance(
                volumeEntity.getName()));

        UICommand okCommand = UICommand.createDefaultOkUiCommand("onStopRebalance", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
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

        model.startProgress();

        final GlusterVolumeEntity volumeEntity = getSelectedItem();
        GlusterVolumeRebalanceParameters param = new GlusterVolumeRebalanceParameters(volumeEntity.getId(), false, false);

        Frontend.getInstance().runAction(ActionType.StopRebalanceGlusterVolume, param, result -> {
            ConfirmationModel localModel = (ConfirmationModel) getConfirmWindow();
            localModel.stopProgress();
            setConfirmWindow(null);
            if (result.getReturnValue().getSucceeded()) {
                showRebalanceStatus();
            }
        });
    }

    private void showRebalanceStatus() {
        if (getSelectedItem() == null) {
            return;
        }
        final ConfirmationModel cModel = new ConfirmationModel();
        final GlusterVolumeEntity volumeEntity = getSelectedItem();
        setConfirmWindow(cModel);
        cModel.setTitle(ConstantsManager.getInstance().getConstants().rebalanceStatusTitle());
        cModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());//$NON-NLS-1$
        cModel.setHelpTag(HelpTag.volume_rebalance_status);
        cModel.setHashName("volume_rebalance_status"); //$NON-NLS-1$

        final UICommand rebalanceStatusOk = new UICommand("closeConfirmationWindow", VolumeListModel.this);//$NON-NLS-1$
        rebalanceStatusOk.setTitle(ConstantsManager.getInstance().getConstants().ok());
        rebalanceStatusOk.setIsCancel(true);
        cModel.getCommands().add(rebalanceStatusOk);

        AsyncDataProvider.getInstance().getGlusterRebalanceStatus(new AsyncQuery<>(returnValue -> {
            cModel.stopProgress();
            GlusterVolumeTaskStatusEntity rebalanceStatusEntity = returnValue.getReturnValue();
            if ((rebalanceStatusEntity == null) || !returnValue.getSucceeded()) {
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
                    rebalanceStatusModel.getCluster().setEntity(volumeEntity.getClusterName());

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
        }),
                volumeEntity.getClusterId(),
                volumeEntity.getId());
    }

    private void showVolumeProfiling() {
        if(getSelectedItem() == null || getWindow()!= null) {
            return;
        }
        GlusterVolumeEntity selectedVolume = getSelectedItem();
        VolumeProfileStatisticsModel profileStatsModel = new VolumeProfileStatisticsModel(selectedVolume.getClusterId(), selectedVolume.getId(), selectedVolume.getName());

        setWindow(profileStatsModel);
        setHelpTag(HelpTag.volume_profile_statistics);
        setHashName("volume_profile_statistics"); //$NON-NLS-1$

        profileStatsModel.startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());//$NON-NLS-1$

        UICommand closeProfilingStats = new UICommand("closeProfileStats", VolumeListModel.this);//$NON-NLS-1$
        closeProfilingStats.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeProfilingStats.setIsCancel(true);
        profileStatsModel.getCommands().add(closeProfilingStats);

        if (selectedVolume.isNfsEnabled()) {
            profileStatsModel.queryBackend(true);
        }
        profileStatsModel.queryBackend(false);
    }

    private void cancelRebalanceStatus() {
        if (getWindow() == null) {
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
        ArrayList<GlusterVolumeEntity> volumesForOptimiseForVirtStore = new ArrayList<>();
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
            cModel.setTitle(ConstantsManager.getInstance().getConstants().optimiseForVirtStoreTitle());
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
        AsyncDataProvider.getInstance().getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.GlusterVolumeOptionGroupVirtValue, AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                new AsyncQuery<String>(optionGroupVirt -> AsyncDataProvider.getInstance().getConfigFromCache(new GetConfigurationValueParameters(ConfigValues.GlusterVolumeOptionOwnerUserVirtValue,
                                AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                        new AsyncQuery<String>(optionOwnerUserVirt -> AsyncDataProvider.getInstance().getConfigFromCache(new GetConfigurationValueParameters(ConfigValues.GlusterVolumeOptionOwnerGroupVirtValue,
                                        AsyncDataProvider.getInstance().getDefaultConfigurationVersion()),
                                new AsyncQuery<String>(optionOwnerGroupVirt -> {

                                    ArrayList<ActionParametersBase> list = new ArrayList<>();
                                    for (GlusterVolumeEntity volume : volumeList) {
                                        Guid volumeId = volume.getId();
                                        if (volume.getVolumeType().isReplicatedType()) {
                                            list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "group", optionGroupVirt)));//$NON-NLS-1$
                                        } else if (volume.getVolumeType().equals(GlusterVolumeType.DISTRIBUTE)){
                                            list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "group", "distributed-virt")));//$NON-NLS-1$ $NON-NLS-2$
                                        }

                                        list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "storage.owner-uid", optionOwnerUserVirt)));//$NON-NLS-1$

                                        list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "storage.owner-gid", optionOwnerGroupVirt)));//$NON-NLS-1$

                                        list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "performance.strict-o-direct", "on")));//$NON-NLS-1$ $NON-NLS-2$

                                        list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "network.remote-dio", "off")));//$NON-NLS-1$ $NON-NLS-2$

                                        list.add(new GlusterVolumeOptionParameters(getOption(volumeId, "cluster.granular-entry-heal", "enable")));//$NON-NLS-1$ $NON-NLS-2$

                                        final GlusterVolumeOptionEntity checkOption = getOption(volumeId, "network.ping-timeout", "30");//$NON-NLS-1$//$NON-NLS-2$
                                        if (volume.getOptions().stream().noneMatch
                                                (obj -> obj.getKey().equalsIgnoreCase(checkOption.getKey()))) {
                                            list.add(new GlusterVolumeOptionParameters(checkOption));//$NON-NLS-1$
                                        }
                                    }
                                    Frontend.getInstance().runMultipleAction(ActionType.SetGlusterVolumeOption, list);
                                }))))));
    }

    private GlusterVolumeOptionEntity getOption(Guid volumeId, String key, String value) {
        return new GlusterVolumeOptionEntity(volumeId, key, value);
    }

    private void stop() {
        if (getWindow() != null) {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmStopVolume());
        model.setHelpTag(HelpTag.volume_stop);
        model.setHashName("volume_stop"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().stopVolumeMessage());
        if (isMetaVolumeInList(getSelectedItems())) {
            model.setNote(ConstantsManager.getInstance().getConstants().stopMetaVolumeWarning());
        } else {
            model.setNote(ConstantsManager.getInstance().getConstants().stopVolumeWarning());
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        for (GlusterVolumeEntity item : getSelectedItems()) {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("onStop", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onStop() {
        if (getWindow() == null) {
            return;
        }
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.StopGlusterVolume, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    private void start() {
        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> volumesForForceStartWarning = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
            if (status == VolumeStatus.ALL_BRICKS_DOWN || status == VolumeStatus.SOME_BRICKS_DOWN) {
                volumesForForceStartWarning.add(volume.getName());
            }
        }

        prepareForStartVolume(true);

        if (!volumesForForceStartWarning.isEmpty()) {
            final ConfirmationModel cModel = new ConfirmationModel();
            cModel.setHelpTag(HelpTag.volume_start);
            cModel.setHashName("volume_start");//$NON-NLS-1$

            setConfirmWindow(cModel);

            cModel.setMessage(ConstantsManager.getInstance().getConstants().startForceVolumeMessage());
            cModel.setTitle(ConstantsManager.getInstance().getConstants().confirmStartVolume());
            cModel.setForceLabel(ConstantsManager.getInstance().getConstants().startForceLabel());
            cModel.setItems(volumesForForceStartWarning);
            cModel.getForce().setIsAvailable(true);
            cModel.getForce().setEntity(true);

            cModel.getForce().getEntityChangedEvent().addListener((ev, sender, args) -> {
                if(cModel.getCommands() != null && cModel.getCommands().get(0) !=null) {
                    cModel.getCommands().get(0).setIsExecutionAllowed(cModel.getForce().getEntity());
                }
            });

            cModel.getCommands().add(UICommand.createDefaultOkUiCommand("startVolumeWithForceOption", this));//$NON-NLS-1$
            cModel.getCommands().add(UICommand.createCancelUiCommand("closeConfirmationWindow", this));//$NON-NLS-1$
        }
    }

    private void onStartVolume(ArrayList<ActionParametersBase> parameters) {
        Frontend.getInstance().runMultipleAction(ActionType.StartGlusterVolume, parameters, null, true, true);
    }

    private void prepareForStartVolume(boolean noForceStart) {
        boolean force = false;
        ConfirmationModel cModel;
        if (getConfirmWindow() != null) {
            cModel = (ConfirmationModel) getConfirmWindow();
            closeConfirmationWindow();
            force = cModel.getForce().getEntity();
        }
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
            if (!noForceStart && (status == VolumeStatus.ALL_BRICKS_DOWN || status == VolumeStatus.SOME_BRICKS_DOWN)) {
                list.add(new GlusterVolumeActionParameters(volume.getId(), force));
            } else if (noForceStart && status == VolumeStatus.DOWN) {
                list.add(new GlusterVolumeActionParameters(volume.getId(), false));
            }
        }
        onStartVolume(list);
    }

    private void onCreateVolume() {
        VolumeModel volumeModel = (VolumeModel) getWindow();

        if (!volumeModel.validate()) {
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
            volume.setIsArbiter(volumeModel.getArbiterVolume().getEntity());
        }

        volume.setVolumeType(type);
        if (volumeModel.getTcpTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.TCP);
        }
        if (volumeModel.getRdmaTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.RDMA);
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<>();

        for (Object model : volumeModel.getBricks().getItems()) {
            brickList.add((GlusterBrickEntity) ((EntityModel) model).getEntity());
        }

        volume.setBricks(brickList);

        if (volumeModel.getNfs_accecssProtocol().getEntity()) {
            volume.enableNFS();
        } else {
            volume.disableNFS();
        }

        if (volumeModel.getCifs_accecssProtocol().getEntity()) {
            volume.enableCifs();
        } else {
            volume.disableCifs();
        }

        volume.setAccessControlList(volumeModel.getAllowAccess().getEntity());

        volumeModel.startProgress();

        CreateGlusterVolumeParameters parameter =
                new CreateGlusterVolumeParameters(volume, volumeModel.isForceAddBricks());

        Frontend.getInstance().runAction(ActionType.CreateGlusterVolume, parameter, result -> {
            VolumeListModel localModel = (VolumeListModel) result.getState();
            localModel.postOnCreateVolume(result.getReturnValue(), volume);
        }, this);
    }

    public void postOnCreateVolume(ActionReturnValue returnValue, GlusterVolumeEntity volume) {
        VolumeModel model = (VolumeModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            if (model.getOptimizeForVirtStore().getEntity()) {
                volume.setId((Guid) returnValue.getActionReturnValue());
                optimizeVolumesForVirtStore(Arrays.asList(volume));
            }
        }
    }

    /**
     * This action is handled here in VolumeLisModel only, because there is a use case where no volume would be selected
     * for setting the configuration. And in this scenario the GlusrerVolumeSnapshotListModel would not be initialized.
     */
    public void configureClusterSnapshotOptions() {
        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        final GlusterClusterSnapshotConfigModel clusterSnapshotConfigModel = new GlusterClusterSnapshotConfigModel();
        clusterSnapshotConfigModel.setHelpTag(HelpTag.configure_volume_snapshot);
        clusterSnapshotConfigModel.setHashName("configure_volume_snapshot"); //$NON-NLS-1$
        clusterSnapshotConfigModel.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .configureClusterSnapshotOptionsTitle());
        setWindow(clusterSnapshotConfigModel);

        AsyncDataProvider.getInstance().getClustersHavingHosts(new AsyncQuery<>(returnValue -> {
            if (getSelectedItems() != null) {
                GlusterVolumeEntity volumeEntity = getSelectedItems().get(0);
                if (volumeEntity != null) {
                    AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(cluster -> clusterSnapshotConfigModel.getClusters().setItems(returnValue, cluster)), volumeEntity.getClusterId());
                }
            } else {
                clusterSnapshotConfigModel.getClusters().setItems(returnValue);
            }
        }));

        clusterSnapshotConfigModel.getClusterConfigOptions().setTitle(ConstantsManager.getInstance()
                .getConstants()
                .configureClusterSnapshotOptionsTitle());

        UICommand updateCommand = new UICommand("confirmConfigureClusterSnapshotOptions", this); //$NON-NLS-1$
        updateCommand.setTitle(constants.snapshotConfigUpdateButtonLabel());
        updateCommand.setIsDefault(true);
        clusterSnapshotConfigModel.getCommands().add(updateCommand);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(constants.cancel());
        cancelCommand.setIsCancel(true);
        clusterSnapshotConfigModel.getCommands().add(cancelCommand);
    }

    public void confirmConfigureClusterSnapshotOptions() {
        boolean cfgChanged = false;
        GlusterClusterSnapshotConfigModel snapshotConfigModel = (GlusterClusterSnapshotConfigModel) getWindow();

        if (!snapshotConfigModel.validate()) {
            return;
        }

        for (EntityModel<GlusterVolumeSnapshotConfig> clusterCfg : snapshotConfigModel.getClusterConfigOptions()
                .getItems()) {
            if (!clusterCfg.getEntity().getParamValue().equals(snapshotConfigModel.getExistingClusterConfigValue(clusterCfg.getEntity()
                    .getParamName()))) {
                cfgChanged = true;
                break;
            }
        }

        if (cfgChanged) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .updateSnapshotConfigurationConfirmationTitle());
            confirmModel.setHelpTag(HelpTag.configure_volume_snapshot_confirmation);
            confirmModel.setHashName("configure_volume_snapshot_confirmation"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeSnapshotConfigurationMsg());

            UICommand tempVar = new UICommand("onConfigureClusterSnapshotOptions", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onConfigureClusterSnapshotOptions();
        }
    }

    public void onConfigureClusterSnapshotOptions() {
        GlusterClusterSnapshotConfigModel clusterSnapshotConfigModel = (GlusterClusterSnapshotConfigModel) getWindow();

        Guid clusterId = clusterSnapshotConfigModel.getClusters().getSelectedItem().getId();
        List<GlusterVolumeSnapshotConfig> vdsParams = new ArrayList<>();
        for (EntityModel<GlusterVolumeSnapshotConfig> clusterCfg : clusterSnapshotConfigModel.getClusterConfigOptions()
                .getItems()) {
            vdsParams.add(new GlusterVolumeSnapshotConfig(clusterId,
                    null,
                    clusterCfg.getEntity().getParamName(),
                    clusterCfg.getEntity().getParamValue()));
        }

        Frontend.getInstance().runAction(ActionType.UpdateGlusterVolumeSnapshotConfig,
                new UpdateGlusterVolumeSnapshotConfigParameters(clusterId, null, vdsParams),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        cancel();
                    }
                    if (getConfirmWindow() != null) {
                        setConfirmWindow(null);
                    }
                },
                this);
    }

    public void configureVolumeSnapshotOptions() {
        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        GlusterVolumeEntity volumeEntity = getSelectedItems().get(0);
        final GlusterVolumeSnapshotConfigModel volumeSnapshotConfigModel =
                new GlusterVolumeSnapshotConfigModel(volumeEntity);
        volumeSnapshotConfigModel.setHelpTag(HelpTag.configure_volume_snapshot);
        volumeSnapshotConfigModel.setHashName("configure_volume_snapshot"); //$NON-NLS-1$
        volumeSnapshotConfigModel.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .configureVolumeSnapshotOptionsTitle());
        setWindow(volumeSnapshotConfigModel);

        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(cluster -> volumeSnapshotConfigModel.getClusterName().setEntity(cluster.getName())), volumeEntity.getClusterId());
        volumeSnapshotConfigModel.getVolumeName().setEntity(volumeEntity.getName());

        UICommand updateCommand = new UICommand("confirmConfigureVolumeSnapshotOptions", this); //$NON-NLS-1$
        updateCommand.setTitle(constants.snapshotConfigUpdateButtonLabel());
        updateCommand.setIsDefault(true);
        volumeSnapshotConfigModel.getCommands().add(updateCommand);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(constants.cancel());
        cancelCommand.setIsCancel(true);
        volumeSnapshotConfigModel.getCommands().add(cancelCommand);
    }

    public void confirmConfigureVolumeSnapshotOptions() {
        boolean cfgChanged = false;
        GlusterVolumeSnapshotConfigModel snapshotConfigModel = (GlusterVolumeSnapshotConfigModel) getWindow();

        if (!snapshotConfigModel.validate()) {
            return;
        }

        for (EntityModel<VolumeSnapshotOptionModel> volumeCfg : snapshotConfigModel.getConfigOptions().getItems()) {
            if (!volumeCfg.getEntity().getOptionValue().equals(snapshotConfigModel.getExistingVolumeConfigValue(volumeCfg.getEntity()
                    .getOptionName()))) {
                cfgChanged = true;
                break;
            }
        }

        if (cfgChanged) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .updateSnapshotConfigurationConfirmationTitle());
            confirmModel.setHelpTag(HelpTag.configure_volume_snapshot_confirmation);
            confirmModel.setHashName("configure_volume_snapshot_confirmation"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeSnapshotConfigurationMsg());

            UICommand tempVar = new UICommand("onConfigureVolumeSnapshotOptions", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            getConfirmWindow().getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            getConfirmWindow().getCommands().add(tempVar2);
        } else {
            onConfigureVolumeSnapshotOptions();
        }
    }

    public void onConfigureVolumeSnapshotOptions() {
        GlusterVolumeSnapshotConfigModel volumeSnapshotConfigModel = (GlusterVolumeSnapshotConfigModel) getWindow();

        GlusterVolumeEntity volumeEntity = volumeSnapshotConfigModel.getSelectedVolumeEntity();
        List<GlusterVolumeSnapshotConfig> vdsParams = new ArrayList<>();
        for (EntityModel<VolumeSnapshotOptionModel> volumeCfg : volumeSnapshotConfigModel.getConfigOptions()
                .getItems()) {
            vdsParams.add(new GlusterVolumeSnapshotConfig(volumeEntity.getClusterId(),
                    volumeEntity.getId(),
                    volumeCfg.getEntity().getOptionName(),
                    volumeCfg.getEntity().getOptionValue()));
        }

        Frontend.getInstance().runAction(ActionType.UpdateGlusterVolumeSnapshotConfig,
                new UpdateGlusterVolumeSnapshotConfigParameters(volumeEntity.getClusterId(),
                        volumeEntity.getId(),
                        vdsParams),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        cancel();
                    }
                    if (getConfirmWindow() != null) {
                        setConfirmWindow(null);
                    }
                },
                this);
    }

    @Override
    protected String getListName() {
        return "VolumeListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("volume"); //$NON-NLS-1$
    }
}
