package org.ovirt.engine.core.bll.gluster.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StepDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterTasksService {
    private static final Logger log = LoggerFactory.getLogger(GlusterTasksService.class);

    @Inject
    private StepDao stepDao;

    @Inject
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Inject
    private GlusterUtil glusterUtil;

    public Map<Guid, GlusterAsyncTask> getTaskListForCluster(Guid id) {
        VDS upServer = glusterUtil.getRandomUpServer(id);
        if (upServer == null) {
            log.info("No up server in cluster");
            return null;
        }
        VDSReturnValue returnValue =runVdsCommand(VDSCommandType.GlusterTasksList,
                new VdsIdVDSCommandParametersBase(upServer.getId()));
        if (returnValue.getSucceeded()) {
            List<GlusterAsyncTask> tasks = (List<GlusterAsyncTask>)returnValue.getReturnValue();
            Map<Guid, GlusterAsyncTask> tasksMap = new HashMap<>();
            for (GlusterAsyncTask task: tasks) {
                tasksMap.put(task.getTaskId(), task);
            }
            return tasksMap;
        } else {
            log.error("Error: {}", returnValue.getVdsError());
            throw new EngineException(EngineError.GlusterVolumeStatusAllFailedException, returnValue.getVdsError().getMessage());
        }
    }

    /**
     * Gets the list of stored tasks in database where the job is not ended
     */
    public List<Guid> getMonitoredTaskIDsInDB() {
        return stepDao.getExternalIdsForRunningSteps(ExternalSystemType.GLUSTER);
    }

    private VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase params) {
        return vdsBrokerFrontend.runVdsCommand(commandType, params);
    }

}
