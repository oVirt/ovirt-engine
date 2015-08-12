package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
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
        Guid job = ((StorageJobCommandParameters)commandEntity.getCommandParameters()).getStorageJobId();
        HostJobStatus jobStatus = null;
        if (job != null) {
            jobStatus = pollStorageJob(job);
            if (jobStatus == HostJobStatus.running) {
                log.info("Waiting on vdsm job: '{}' of '{}' (id: '{}') to complete",
                        job,
                        commandEntity.getCommandType(),
                        cmdId);
                return;
            }

            log.info("Command '{}' id: '{}' job '{}' execution was completed with VDSM job status '{}'",
                    commandEntity.getCommandType(), cmdId, job, jobStatus);
        } else {
            log.error("Step for command '{}' (id: '{}') to complete wasn't created, failing the operation",
                    commandEntity.getCommandType(),
                    cmdId);
        }

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

    //TODO: replace with actual polling logic
    private HostJobStatus pollStorageJob(Guid jobId) {
        return HostJobStatus.done;
    }
}
