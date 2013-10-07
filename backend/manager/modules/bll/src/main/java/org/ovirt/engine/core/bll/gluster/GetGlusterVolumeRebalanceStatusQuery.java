package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;

public class GetGlusterVolumeRebalanceStatusQuery<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {

    private Guid clusterId;
    private GlusterVolumeEntity volume;
    private GlusterAsyncTask asyncTask;

    public GetGlusterVolumeRebalanceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        clusterId = getParameters().getClusterId();
        Guid volumeId = getParameters().getVolumeId();

        if (volumeId != null) {
            volume = getGlusterVolumeDao().getById(volumeId);
            if (volume == null) {
                throw new RuntimeException(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID.toString());
            }
            if (clusterId == null) {
                clusterId = volume.getClusterId();
            }
            asyncTask = volume.getAsyncTask();
            if (asyncTask == null) {
                // Set status as null and return as there is not rebalance task started
                getQueryReturnValue().setReturnValue(null);
                return;
            }

            getQueryReturnValue().setReturnValue(fetchTaskStatusDetails());
        }
    }

    private GlusterVolumeTaskStatusEntity fetchTaskStatusDetails() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeRebalanceStatus,
                        new GlusterVolumeVDSParameters(getUpServerId(clusterId), volume.getName()));

        // Set the volume re-balance start time
        GlusterVolumeTaskStatusEntity entity = (GlusterVolumeTaskStatusEntity) returnValue.getReturnValue();
        List<Step> stepsList = getStepDao().getStepsByExternalId(asyncTask.getTaskId());
        if (stepsList != null && !stepsList.isEmpty()) {
            entity.setStartTime(stepsList.get(0).getStartTime());
        }

        // Set the host ip in status details
        updateHostIP(entity);

        return entity;
    }

    private void updateHostIP(GlusterVolumeTaskStatusEntity taskStatus) {
        for (GlusterVolumeTaskStatusForHost hostStatus : taskStatus.getHostwiseStatusDetails()) {
            GlusterServer glusterServer = getGlusterServerDao().getByGlusterServerUuid(hostStatus.getHostUuid());
            if (glusterServer != null) {
                VDS host = getVdsDao().get(glusterServer.getId());
                if (host != null) {
                    hostStatus.setHostName(host.getName());
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
