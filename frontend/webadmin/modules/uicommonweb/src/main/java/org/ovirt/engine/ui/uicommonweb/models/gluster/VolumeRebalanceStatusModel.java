package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.user.client.Timer;

public class VolumeRebalanceStatusModel extends Model {

    private EntityModel volume;

    private EntityModel cluster;

    private EntityModel startedTime;

    private EntityModel statusTime;

    private EntityModel status;

    private GlusterVolumeEntity entity;

    private ListModel rebalanceSessions;

    private boolean isStatusAvailable;

    private Timer refresh;

    public VolumeRebalanceStatusModel(final GlusterVolumeEntity volumeEntity) {
        setStatus(new EntityModel());
        setVolume(new EntityModel());
        setCluster(new EntityModel());
        setStartedTime(new EntityModel());
        setStatusTime(new EntityModel());
        setRebalanceSessions(new ListModel());
        setEntity(volumeEntity);
        refresh = new Timer() {

            @Override
            public void run() {
                refreshDetails(volumeEntity);
            }

        };
        refresh.scheduleRepeating(10000);
        this.entity = volumeEntity;
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

    public EntityModel getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(EntityModel startedTime) {
        this.startedTime = startedTime;
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
        getStartedTime().setEntity(rebalanceStatusEntity.getStartTime() == null ? ConstantsManager.getInstance()
                .getConstants()
                .notAvailableLabel()
                : rebalanceStatusEntity.getStartTime());
        getStatusTime().setEntity(rebalanceStatusEntity.getStatusTime() == null ? ConstantsManager.getInstance()
                .getConstants()
                .notAvailableLabel()
                : rebalanceStatusEntity.getStatusTime());
        getRebalanceSessions().setItems(sessionList);
        if(rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FINISHED) {
            setStatusAvailable(true);
            refresh.cancel();
        }else {
            setStatusAvailable(false);
            if ((rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.ABORTED || rebalanceStatusEntity.getStatusSummary().getStatus() == JobExecutionStatus.FAILED)) {
                refresh.cancel();
            }
        }
    }

    public void cancelRefresh() {
        if(refresh != null) {
            refresh.cancel();
        }
    }

    public void refreshDetails(GlusterVolumeEntity volumeEntity) {
        AsyncDataProvider.getGlusterRebalanceStatus(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                GlusterVolumeTaskStatusEntity rebalanceEntity = (GlusterVolumeTaskStatusEntity) returnValue;
                showStatus(rebalanceEntity);
            }
        }), volumeEntity.getClusterId(), volumeEntity.getId());
    }
    public GlusterVolumeEntity getEntity() {
        return entity;
    }

    public void setEntity(GlusterVolumeEntity entity) {
        this.entity = entity;
    }

    public EntityModel getStatus() {
        return status;
    }

    public void setStatus(EntityModel statusLabel) {
        this.status = statusLabel;
    }

    public boolean isStatusAvailable() {
        return isStatusAvailable;
    }

    public void setStatusAvailable(boolean isStatusAvailable) {
        this.isStatusAvailable = isStatusAvailable;
        if(isStatusAvailable == true) {
            onPropertyChanged(new PropertyChangedEventArgs("IS_STATUS_APPLICABLE"));//$NON-NLS-1$
        }
    }
}
