package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksQueriesParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;

public class GetGlusterVolumeRemoveBricksStatusQuery<P extends GlusterVolumeRemoveBricksQueriesParameters> extends GlusterQueriesCommandBase<P> {

    private Guid clusterId;
    private GlusterVolumeEntity volume;

    public GetGlusterVolumeRemoveBricksStatusQuery(P params) {
        super(params);
    }

    @Override
    public void executeQueryCommand() {
        clusterId = getParameters().getClusterId();
        Guid volumeId = getParameters().getVolumeId();
        if (volumeId != null) {
            volume = getGlusterVolumeDao().getById(volumeId);
            if (volume == null) {
                throw new RuntimeException(VdcBllMessages.GLUSTER_VOLUME_ID_INVALID.toString());
            }
        }

        if (clusterId == null) {
            clusterId = volume.getClusterId();
        }

        getQueryReturnValue().setReturnValue(fetchRemobeBricksStatusDetails());
    }

    private GlusterVolumeTaskStatusEntity fetchRemobeBricksStatusDetails() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeRemoveBricksStatus,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServerId(clusterId),
                                volume.getName(),
                                getParameters().getBricks(),
                                volume.getReplicaCount()));

        // Set the volume re-balance start time
        GlusterVolumeTaskStatusEntity entity = (GlusterVolumeTaskStatusEntity) returnValue.getReturnValue();
        GlusterAsyncTask asyncTask = volume.getAsyncTask();
        if (asyncTask != null && asyncTask.getTaskId() != null) {
            List<Step> stepsList = getStepDao().getStepsByExternalId(asyncTask.getTaskId());
            if (stepsList != null && !stepsList.isEmpty()) {
                entity.setStartTime(stepsList.get(0).getStartTime());
            }
        }

        // Set the host ip in status details
        updateHostIP(entity);

        return entity.sort();
    }

    private void updateHostIP(GlusterVolumeTaskStatusEntity taskStatus) {
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

    public StepDao getStepDao() {
        return getDbFacade().getStepDao();
    }

    public GlusterServerDao getGlusterServerDao() {
        return getDbFacade().getGlusterServerDao();
    }
}
