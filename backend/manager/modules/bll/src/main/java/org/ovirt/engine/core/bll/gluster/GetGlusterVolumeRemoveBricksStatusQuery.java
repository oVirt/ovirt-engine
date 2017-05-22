package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksQueriesParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

public class GetGlusterVolumeRemoveBricksStatusQuery<P extends GlusterVolumeRemoveBricksQueriesParameters> extends GlusterAsyncTaskStatusQueryBase<P> {

    public GetGlusterVolumeRemoveBricksStatusQuery(P params, EngineContext engineContext) {
        super(params, engineContext);
    }

    @Override
    protected GlusterVolumeTaskStatusEntity fetchTaskStatusDetails() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeRemoveBricksStatus,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServerId(clusterId),
                                volume.getName(),
                                getParameters().getBricks(),
                                volume.getReplicaCount()));

        GlusterVolumeTaskStatusEntity entity = (GlusterVolumeTaskStatusEntity)returnValue.getReturnValue();

        // Update the latest status details
        GlusterAsyncTask asyncTask = volume.getAsyncTask();
        if (asyncTask != null) {
            // take a copy of the task status with engine for further use
            JobExecutionStatus jobStatusWithEngine = volume.getAsyncTask().getStatus();

            // update the status entity with required details
            updateStatusEntity(entity);

            // If the task status at engine side is aborted, set the same to current status
            // This is required because even in case of retention of the brick the gluster returns
            // the status as finished and to avoid enabling of retain and commit in ui the status should
            // be set to aborted. This is required only in case of remove bricks
            if (jobStatusWithEngine == JobExecutionStatus.ABORTED) {
                entity.getStatusSummary().setStatus(JobExecutionStatus.ABORTED);
            }
        }

        return entity;
    }
}
