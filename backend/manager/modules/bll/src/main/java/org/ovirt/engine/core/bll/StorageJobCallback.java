package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.vdscommands.GetHostJobsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageJobCallback extends CommandCallback {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(cmdId);
        StorageJobCommandParameters cmdParams = (StorageJobCommandParameters) commandEntity.getCommandParameters();
        Guid job = cmdParams.getStorageJobId();
        Guid vdsId = cmdParams.getVdsRunningOn();
        HostJobStatus jobStatus = null;
        VDS vds = getVdsDao().get(vdsId);
        if (vds.getStatus() == VDSStatus.Up) {
            try {
                jobStatus = pollStorageJob(job, vdsId);
            } catch (Exception e) {
                // We shouldn't get an error when polling the host job (as it access the local storage only).
                // If we got an error, it will usually be a network error - so the host will either move
                // to Non Responsive or the polling will succeed on the next attempt.
                log.warn("Command {} id: '{}': Failed to poll the job '{}' on host '{}' (id: '{}'), will retry soon",
                        commandEntity.getCommandType(), cmdId, job, vds.getName(), vdsId);
                return;
            }
        } else {
            log.warn("Can't poll the status of job '{}' as Host '{}' (id: '{}') isn't in status UP",
                     job, vds.getName(), vds.getId());
        }

        // We need to poll the entity if the host isn't up or if the job wasn't return from the job polling on vdsm
        // (it might have already been deleted from the vdsm job caching)
        if (jobStatus == null) {
            jobStatus = pollEntityIfSupported(getCommand(cmdId));
        }

        if (jobStatus == null) {
            log.info("Couldn't get status of job '{}' running on Host '{}' of '{}' (id: '{}'), Assuming it's still " +
                    "running", job, vdsId, commandEntity.getCommandType(), cmdId);
            return;
        }

        if (jobStatus.isAlive()) {
            log.info("Waiting on vdsm job: '{}' of '{}' (id: '{}') to complete",
                    job,
                    commandEntity.getCommandType(),
                    cmdId);
            return;
        }

        log.info("Command '{}' id: '{}' job '{}' execution was completed with VDSM job status '{}'",
                commandEntity.getCommandType(), cmdId, job, jobStatus);
        CommandBase<?> command = getCommand(cmdId);
        CommandExecutionStatus status = CommandCoordinatorUtil.getCommandExecutionStatus(cmdId);
        command.getParameters().setTaskGroupSuccess(status == CommandExecutionStatus.EXECUTED
                && jobStatus == HostJobStatus.done);
        command.setCommandStatus(command.getParameters().getTaskGroupSuccess() ? CommandStatus.SUCCEEDED
                : CommandStatus.FAILED);
        log.info("Command '{}' (id: '{}') execution was completed, the command status is '{}'",
                command.getActionType(), command.getCommandId(), command.getCommandStatus());
    }

    protected CommandBase<?> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }

    private HostJobStatus pollStorageJob(Guid jobId, Guid vdsId) {
        if (jobId == null) {
            return null;
        }

        GetHostJobsVDSCommandParameters p = new GetHostJobsVDSCommandParameters(vdsId, Collections
                .singletonList(jobId), HostJobType.storage);
        VDSReturnValue returnValue = Backend.getInstance().getResourceManager().runVdsCommand(VDSCommandType
                .GetHostJobs, p);
        HostJobInfo jobInfo = ((Map<Guid, HostJobInfo>) returnValue.getReturnValue()).get(jobId);
        return jobInfo != null ? jobInfo.getStatus() : null;
    }

    private HostJobStatus pollEntityIfSupported(CommandBase<?> cmd) {
        if (!(cmd instanceof EntityPollingCommand)) {
            log.error("Entity polling isn't supported for command '{}' (id: '{}'), will retry to poll the job soon",
                    cmd.getActionType(),
                    cmd.getCommandId());
            return null;
        }

        try {
            return ((EntityPollingCommand) cmd).poll();
        } catch (Exception e) {
            log.error("Failed to poll entity for command '{}' (id: '{}'), will retry soon",
                    cmd.getActionType(),
                    cmd.getCommandId());
        }

        return null;
    }

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }

    private VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }
}
