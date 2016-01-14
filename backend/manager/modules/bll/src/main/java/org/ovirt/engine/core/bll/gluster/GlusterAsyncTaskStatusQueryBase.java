package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;

public abstract class GlusterAsyncTaskStatusQueryBase<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {
    protected Guid clusterId;
    protected GlusterVolumeEntity volume;

    public GlusterAsyncTaskStatusQueryBase(P params) {
        super(params);
    }

    @Override
    public void executeQueryCommand() {
        clusterId = getParameters().getClusterId();
        volume = getGlusterVolumeDao().getById(getParameters().getVolumeId());
        if (volume == null) {
            throw new RuntimeException(EngineMessage.GLUSTER_VOLUME_ID_INVALID.toString());
        }

        if (clusterId == null) {
            clusterId = volume.getClusterId();
        }

        getQueryReturnValue().setReturnValue(fetchTaskStatusDetails());
    }

    protected abstract GlusterVolumeTaskStatusEntity fetchTaskStatusDetails();

    public StepDao getStepDao() {
        return getDbFacade().getStepDao();
    }

    public GlusterServerDao getGlusterServerDao() {
        return getDbFacade().getGlusterServerDao();
    }

    protected GlusterVolumeTaskStatusEntity updateStatusEntity(GlusterVolumeTaskStatusEntity status) {
        // Set the volume remove bricks start time
        setStartAndStopTime(status);
        // update the latest status
        updateLatestStatus(status);
        // Update the host details as required into entity
        return updateHostDetails(status);
    }

    private GlusterVolumeTaskStatusEntity updateHostDetails(GlusterVolumeTaskStatusEntity taskStatus) {
        updateHostIP(taskStatus);
        taskStatus.sort();
        return taskStatus;
    }

    public GlusterTaskUtils getGlusterTaskUtils() {
        return GlusterTaskUtils.getInstance();
    }

    @Override
    public ClusterDao getClusterDao() {
        return DbFacade.getInstance().getClusterDao();
    }

    private void updateHostIP(GlusterVolumeTaskStatusEntity taskStatus) {
        if (taskStatus != null) {
            for (GlusterVolumeTaskStatusForHost hostStatus : taskStatus.getHostwiseStatusDetails()) {
                GlusterServer glusterServer = getGlusterServerDao().getByGlusterServerUuid(hostStatus.getHostUuid());
                if (glusterServer != null) {
                    VDS host = getVdsDao().get(glusterServer.getId());
                    if (host != null) {
                        hostStatus.setHostName(host.getName());
                        hostStatus.setHostId(host.getId());
                    }
                }
            }
        }
    }

    private GlusterVolumeTaskStatusEntity setStartAndStopTime(GlusterVolumeTaskStatusEntity status) {
        if (status == null) {
            return null;
        }

        GlusterAsyncTask asyncTask = volume.getAsyncTask();
        if (asyncTask != null && asyncTask.getTaskId() != null) {
            List<Step> stepsList = getStepDao().getStepsByExternalId(asyncTask.getTaskId());
            if (stepsList != null && !stepsList.isEmpty()) {
                status.setStartTime(stepsList.get(0).getStartTime());
                status.setStopTime(stepsList.get(0).getEndTime());
            }
        }

        return status;
    }

    private void updateLatestStatus(GlusterVolumeTaskStatusEntity status) {
        GlusterAsyncTask asyncTask = volume.getAsyncTask();

        if (asyncTask != null && asyncTask.getTaskId() != null) {
            GlusterTaskParameters taskParameters = new GlusterTaskParameters();
            taskParameters.setVolumeName(volume.getName());
            asyncTask.setTaskParameters(taskParameters);

            List<Step> stepsList = getStepDao().getStepsByExternalId(asyncTask.getTaskId());
            // if step has already ended, do not update status.
            if (stepsList != null && !stepsList.isEmpty() && stepsList.get(0).getEndTime() != null) {
                asyncTask.setStatus(status.getStatusSummary().getStatus());
                asyncTask.setMessage(GlusterTaskUtils.getInstance().getSummaryMessage(status.getStatusSummary()));
                getGlusterTaskUtils().updateSteps(getClusterDao().get(clusterId), asyncTask, stepsList);

                // release the volume lock if the task is completed
                if (getGlusterTaskUtils().hasTaskCompleted(asyncTask)) {
                    getGlusterTaskUtils().releaseLock(volume.getId());
                }
            }

        }
    }
}
