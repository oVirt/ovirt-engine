package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.user.client.Timer;

public class VolumeRebalanceStatusModel extends Model {

    private EntityModel volume;

    private EntityModel cluster;

    private EntityModel startTime;

    private EntityModel statusTime;

    public EntityModel getStopTime() {
        return stopTime;
    }

    public void setStopTime(EntityModel stopTime) {
        this.stopTime = stopTime;
    }

    private EntityModel stopTime;

    private GlusterVolumeEntity entity;

    private ListModel rebalanceSessions;

    private boolean isStatusAvailable;

    private Timer refresh;

    private UICommand stopReblanceFromStatus;

    public VolumeRebalanceStatusModel(final GlusterVolumeEntity volumeEntity) {
        setVolume(new EntityModel());
        setCluster(new EntityModel());
        setStartTime(new EntityModel());
        getStartTime().setEntity(new Date());
        setStatusTime(new EntityModel());
        setStopTime(new EntityModel());
        getStopTime().setEntity(new Date());
        setRebalanceSessions(new ListModel());
        setEntity(volumeEntity);
        refresh = new Timer() {

            @Override
            public void run() {
                refreshDetails(volumeEntity);
            }

        };
        refresh.scheduleRepeating(10000);
    }

    public ListModel getRebalanceSessions() {
        return rebalanceSessions;
    }

    public void setRebalanceSessions(ListModel rebalanceSessions) {
        this.rebalanceSessions = rebalanceSessions;
    }

    public EntityModel getVolume() {
        return volume;
    }

    public void setVolume(EntityModel volume) {
        this.volume = volume;
    }

    public EntityModel getStartTime() {
        return startTime;
    }

    public void setStartTime(EntityModel startedTime) {
        this.startTime = startedTime;
    }

    public EntityModel getCluster() {
        return cluster;
    }

    public void setCluster(EntityModel cluster) {
        this.cluster = cluster;
    }

    public EntityModel getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(EntityModel statusTime) {
        this.statusTime = statusTime;
    }

    public void showStatus(GlusterVolumeTaskStatusEntity rebalanceStatusEntity) {
        List<GlusterVolumeTaskStatusForHost> rebalanceSessionsList =
                rebalanceStatusEntity.getHostwiseStatusDetails();
        List<EntityModel> sessionList = new ArrayList<EntityModel>();
        for (GlusterVolumeTaskStatusForHost hostDetail : rebalanceSessionsList) {
            EntityModel sessionModel = new EntityModel(hostDetail);
            sessionList.add(sessionModel);
        }
        getStartTime().setEntity(rebalanceStatusEntity.getStartTime());
        getStatusTime().setEntity(rebalanceStatusEntity.getStatusTime());
        getRebalanceSessions().setItems(sessionList);
        setStatusAvailable(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FINISHED);
        if(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FINISHED) {
            refresh.cancel();
        }else {
            if ((rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.ABORTED || rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FAILED)) {
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
        AsyncDataProvider.getGlusterRebalanceStatus(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue vdcValue = (VdcQueryReturnValue) returnValue;
                GlusterVolumeTaskStatusEntity rebalanceStatusEntity =vdcValue.getReturnValue();
                if (rebalanceStatusEntity != null) {
                    showStatus(rebalanceStatusEntity);
                }
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
