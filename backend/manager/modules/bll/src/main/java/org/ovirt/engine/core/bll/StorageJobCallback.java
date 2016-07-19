package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;
import org.ovirt.engine.core.common.vdscommands.GetHostJobsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageJobCallback extends CommandCallback {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(cmdId);
        StorageJobCommandParameters cmdParams = (StorageJobCommandParameters) commandEntity.getCommandParameters();
        Guid job = cmdParams.getStorageJobId();
        HostJobStatus jobStatus;
        try {
            jobStatus = pollStorageJob(job, cmdParams.getVdsRunningOn());
        } catch (Exception e) {
            log.error("Failed to poll job '{}' for command '{}' (id: '{}'), will retry soon",
                    job,
                    commandEntity.getCommandType(),
                    cmdId);
            return;
        }

        if (jobStatus == null) {
            log.error("Job for command '{}' (id: '{}')  wasn't created, failing the operation",
                    commandEntity.getCommandType(),
                    cmdId);
        } else if (jobStatus.isAlive()) {
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

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }
}
