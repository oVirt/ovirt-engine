package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
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
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;

public abstract class GlusterAsyncTaskStatusQueryBase<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {
    protected Guid clusterId;
    protected GlusterVolumeEntity volume;

    @Inject
    protected StepDao stepDao;

    @Inject
    protected GlusterServerDao glusterServerDao;

    @Inject
    protected VdsDao vdsDao;

    @Inject
    protected GlusterTaskUtils glusterTaskUtils;

    public GlusterAsyncTaskStatusQueryBase(P params, EngineContext engineContext) {
        super(params, engineContext);
    }

    @Override
    public void executeQueryCommand() {
        clusterId = getParameters().getClusterId();
        volume = glusterVolumeDao.getById(getParameters().getVolumeId());
        if (volume == null) {
            throw new RuntimeException(EngineMessage.GLUSTER_VOLUME_ID_INVALID.toString());
        }

        if (clusterId == null) {
            clusterId = volume.getClusterId();
        }

        getQueryReturnValue().setReturnValue(fetchTaskStatusDetails());
    }

    protected abstract GlusterVolumeTaskStatusEntity fetchTaskStatusDetails();

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

    private void updateHostIP(GlusterVolumeTaskStatusEntity taskStatus) {
        if (taskStatus != null) {
            for (GlusterVolumeTaskStatusForHost hostStatus : taskStatus.getHostwiseStatusDetails()) {
                GlusterServer glusterServer = glusterServerDao.getByGlusterServerUuid(hostStatus.getHostUuid());
                if (glusterServer != null) {
                    VDS host = vdsDao.get(glusterServer.getId());
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
            List<Step> stepsList = stepDao.getStepsByExternalId(asyncTask.getTaskId());
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

            List<Step> stepsList = stepDao.getStepsByExternalId(asyncTask.getTaskId());
            // if step has already ended, do not update status.
            if (stepsList != null && !stepsList.isEmpty() && stepsList.get(0).getEndTime() != null) {
                asyncTask.setStatus(status.getStatusSummary().getStatus());
                asyncTask.setMessage(glusterTaskUtils.getSummaryMessage(status.getStatusSummary()));
                glusterTaskUtils.updateSteps(clusterDao.get(clusterId), asyncTask, stepsList);

                // release the volume lock if the task is completed
                if (glusterTaskUtils.hasTaskCompleted(asyncTask)) {
                    glusterTaskUtils.releaseLock(volume.getId());
                }
            }

        }
    }
}
