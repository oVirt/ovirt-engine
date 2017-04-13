package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.user.client.Timer;

public class VolumeRebalanceStatusModel extends Model {

    private EntityModel<String> volume;

    private EntityModel<String> cluster;

    private EntityModel<Date> startTime;

    private EntityModel<Date> statusTime;

    public EntityModel<Date> getStopTime() {
        return stopTime;
    }

    public void setStopTime(EntityModel<Date> stopTime) {
        this.stopTime = stopTime;
    }

    private EntityModel<Date> stopTime;

    private GlusterVolumeEntity entity;

    private ListModel<EntityModel<GlusterVolumeTaskStatusForHost>> rebalanceSessions;

    private boolean isStatusAvailable;

    private Timer refresh;

    private UICommand stopReblanceFromStatus;

    public VolumeRebalanceStatusModel(final GlusterVolumeEntity volumeEntity) {
        setVolume(new EntityModel<String>());
        setCluster(new EntityModel<String>());
        setStartTime(new EntityModel<Date>());
        setStatusTime(new EntityModel<Date>());
        setStopTime(new EntityModel<Date>());
        setRebalanceSessions(new ListModel<EntityModel<GlusterVolumeTaskStatusForHost>>());
        setEntity(volumeEntity);
        refresh = new Timer() {

            @Override
            public void run() {
                refreshDetails(volumeEntity);
            }

        };
        refresh.scheduleRepeating(10000);
    }

    public ListModel<EntityModel<GlusterVolumeTaskStatusForHost>> getRebalanceSessions() {
        return rebalanceSessions;
    }

    public void setRebalanceSessions(ListModel<EntityModel<GlusterVolumeTaskStatusForHost>> rebalanceSessions) {
        this.rebalanceSessions = rebalanceSessions;
    }

    public EntityModel<String> getVolume() {
        return volume;
    }

    public void setVolume(EntityModel<String> volume) {
        this.volume = volume;
    }

    public EntityModel<Date> getStartTime() {
        return startTime;
    }

    public void setStartTime(EntityModel<Date> startedTime) {
        this.startTime = startedTime;
    }

    public EntityModel<String> getCluster() {
        return cluster;
    }

    public void setCluster(EntityModel<String> cluster) {
        this.cluster = cluster;
    }

    public EntityModel<Date> getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(EntityModel<Date> statusTime) {
        this.statusTime = statusTime;
    }

    public void showStatus(GlusterVolumeTaskStatusEntity rebalanceStatusEntity) {
        List<GlusterVolumeTaskStatusForHost> rebalanceSessionsList =
                rebalanceStatusEntity.getHostwiseStatusDetails();
        List<EntityModel<GlusterVolumeTaskStatusForHost>> sessionList = new ArrayList<>();
        for (GlusterVolumeTaskStatusForHost hostDetail : rebalanceSessionsList) {
            EntityModel<GlusterVolumeTaskStatusForHost> sessionModel = new EntityModel<>(hostDetail);
            sessionList.add(sessionModel);
        }
        getStartTime().setEntity(rebalanceStatusEntity.getStartTime());
        getStatusTime().setEntity(rebalanceStatusEntity.getStatusTime());
        getRebalanceSessions().setItems(sessionList);
        setStatusAvailable(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FINISHED);
        if(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FINISHED) {
            refresh.cancel();
        }else {
            if (rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.ABORTED || rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FAILED) {
                refresh.cancel();
                if(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.ABORTED) {
                    getStopTime().setEntity(rebalanceStatusEntity.getStopTime());
                }
            }
        }
        setStopTimeVisible(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.ABORTED);
        if (GlusterTaskType.REBALANCE == getEntity().getAsyncTask().getType()) {
            getStopReblanceFromStatus().setIsExecutionAllowed(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.STARTED);
        }
    }

    public void cancelRefresh() {
        if (refresh != null) {
            refresh.cancel();
        }
    }

    public void refreshDetails(GlusterVolumeEntity volumeEntity) {
        AsyncDataProvider.getInstance().getGlusterRebalanceStatus(new AsyncQuery<>(returnValue -> {
            GlusterVolumeTaskStatusEntity rebalanceStatusEntity = returnValue.getReturnValue();
            if (rebalanceStatusEntity != null) {
                showStatus(rebalanceStatusEntity);
            }
        }), volumeEntity.getClusterId(), volumeEntity.getId());
    }

    public GlusterVolumeEntity getEntity() {
        return entity;
    }

    public void setEntity(GlusterVolumeEntity entity) {
        this.entity = entity;
    }

    public boolean isStatusAvailable() {
        return isStatusAvailable;
    }

    public void setStatusAvailable(boolean isStatusAvailable) {
        this.isStatusAvailable = isStatusAvailable;
        onPropertyChanged(new PropertyChangedEventArgs("STATUS_UPDATED"));//$NON-NLS-1$
    }

    private boolean stopTimeVisible;

    public UICommand getStopReblanceFromStatus() {
        return stopReblanceFromStatus;
    }

    public void setStopReblanceFromStatus(UICommand stopReblanceFromStatus) {
        this.stopReblanceFromStatus = stopReblanceFromStatus;
    }

    public boolean isStopTimeVisible() {
        return stopTimeVisible;
    }

    public void setStopTimeVisible(boolean stopTimeVisible) {
        this.stopTimeVisible = stopTimeVisible;
        onPropertyChanged(new PropertyChangedEventArgs("STOP_TIME_UPDATED"));//$NON-NLS-1$
    }
}
