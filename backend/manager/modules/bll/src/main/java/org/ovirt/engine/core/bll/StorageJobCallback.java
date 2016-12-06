package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.job.Step.MAX_PROGRESS;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
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
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageJobCallback implements CommandCallback {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(cmdId);
        StorageJobCommandParameters cmdParams = (StorageJobCommandParameters) commandEntity.getCommandParameters();
        Guid job = cmdParams.getStorageJobId();
        Guid vdsId = cmdParams.getVdsRunningOn();
        HostJobStatus jobStatus = null;
        VDS vds = getVdsDao().get(vdsId);
        boolean jobsReportedByHost = false;

        if (vds.getStatus() == VDSStatus.Up) {
            HostJobInfo jobInfo;
            try {
                jobInfo = pollStorageJob(job, vdsId);
            } catch (Exception e) {
                // We shouldn't get an error when polling the host job (as it access the local storage only).
                // If we got an error, it will usually be a network error - so the host will either move
                // to Non Responsive or the polling will succeed on the next attempt.
                log.warn("Command {} id: '{}': Failed to poll the job '{}' on host '{}' (id: '{}'), will retry soon",
                        commandEntity.getCommandType(), cmdId, job, vds.getName(), vdsId);
                return;
            }

            if (jobInfo != null) {
                handlePolledJobStatus((StorageJobCommand) getCommand(cmdId), jobInfo);
                jobStatus = jobInfo.getStatus();
                updateStepProgress(commandEntity.getStepId(), jobInfo.getProgress());
            }
            jobsReportedByHost = true;
        } else {
            log.warn("Command {} id: '{}': can't poll the job '{}' as host '{}' (id: '{}') isn't in status UP",
                    commandEntity.getCommandType(), cmdId, job, vds.getName(), vdsId);
        }

        // If we couldn't determine job status by polling the host, we can try to determine it using different methods.
        if (jobStatus == null) {
            jobStatus = handleUndeterminedJobStatus(getCommand(cmdId), jobsReportedByHost);
        }

        if (jobStatus == null) {
            log.info("Command {} id: '{}': couldn't get the status of job '{}' on host '{}' (id: '{}'), assuming it's " +
                    "still running",
                    commandEntity.getCommandType(), cmdId, job, vds.getName(), vdsId);
            return;
        }

        if (jobStatus.isAlive()) {
            log.info("Command {} id: '{}': waiting for job '{}' on host '{}' (id: '{}') to complete",
                    commandEntity.getCommandType(), cmdId, job, vds.getName(), vdsId);
            return;
        }

        log.info("Command {} id: '{}': job '{}' execution was completed with VDSM job status '{}'",
                commandEntity.getCommandType(), cmdId, job, jobStatus);
        CommandBase<?> command = getCommand(cmdId);


        if (command.shouldUpdateStepProgress() && jobStatus == HostJobStatus.done) {
            updateStepProgress(commandEntity.getStepId(), MAX_PROGRESS);
        }

        CommandExecutionStatus status = CommandCoordinatorUtil.getCommandExecutionStatus(cmdId);
        command.getParameters().setTaskGroupSuccess(status == CommandExecutionStatus.EXECUTED
                && jobStatus == HostJobStatus.done);
        command.setCommandStatus(command.getParameters().getTaskGroupSuccess() ? CommandStatus.SUCCEEDED
                : CommandStatus.FAILED);
        log.info("Command {} id: '{}': execution was completed, the command status is '{}'",
                command.getActionType(), command.getCommandId(), command.getCommandStatus());
    }

    protected CommandBase<?> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }

    private boolean isEntityPollingSupported(CommandBase<?> cmd) {
        return cmd instanceof EntityPollingCommand;
    }

    private HostJobInfo pollStorageJob(Guid jobId, Guid vdsId) {
        if (jobId == null) {
            return null;
        }

        GetHostJobsVDSCommandParameters p = new GetHostJobsVDSCommandParameters(vdsId, Collections
                .singletonList(jobId), HostJobType.storage);
        VDSReturnValue returnValue = Backend.getInstance().getResourceManager().runVdsCommand(VDSCommandType
                .GetHostJobs, p);
        return ((Map<Guid, HostJobInfo>) returnValue.getReturnValue()).get(jobId);
    }

    private HostJobStatus pollEntity(CommandBase<?> cmd) {
        try {
            return ((EntityPollingCommand) cmd).poll();
        } catch (Exception e) {
            log.error("Command {} id: '{}': failed to poll the command entity",
                    cmd.getActionType(),
                    cmd.getCommandId());
        }

        return null;
    }


    private void handlePolledJobStatus(StorageJobCommand<?> cmd, HostJobInfo jobInfo) {
        if (jobInfo.getStatus() != HostJobStatus.failed) {
            return;
        }

        // If a job failed on VDSM side, we may want to perform operations according to the job error or to consider the
        // job as successful on some cases.
        // each StorageJobCommand can override a method that'll handle the error and may return a different job status
        // if needed.
        if (jobInfo.getError() != null) {
            jobInfo.setStatus(cmd.handleJobError(jobInfo.getError().getCode()));
            if (jobInfo.getStatus() == HostJobStatus.done) {
                // if the error inspection led us to decide to the job actual status is done, we can set the progress
                // to null so the progress will be considered as 100 for the command step (if present), it's better than
                // setting to 100 as we don't know if progress is actually reported for the operation.
                jobInfo.setProgress(null);
            }
        }
    }

    private HostJobStatus handleUndeterminedJobStatus(CommandBase<?> cmd, boolean jobsReportedByHost) {
        // If the command supports entity polling, we can use it in order to determine the status.
        if (isEntityPollingSupported(cmd)) {
            log.info("Command {} id: '{}': attempting to determine the job status by polling the entity.",
                    cmd.getActionType(),
                    cmd.getCommandId());
            return pollEntity(cmd);
        }

        // if the job was cleared from the host job report we fail the operation so the command will end
        // (as the command doesn't support entity polling - so we don't have any way to poll it).
        if (jobsReportedByHost) {
            log.error("Command {} id: '{}': entity polling isn't supported and the job isn't reported by the host," +
                    "assuming it failed so that the command execution will end.",
                    cmd.getActionType(),
                    cmd.getCommandId());
            return HostJobStatus.failed;
        }

        // if the job status couldn't have been determined because of a different reason, we'll retry to poll it.
        log.error("Command {} id: '{}': entity polling isn't supported, will retry to poll the job soon",
                cmd.getActionType(),
                cmd.getCommandId());
        return null;
    }

    private void updateStepProgress(Guid stepId, Integer progress) {
        if (stepId != null) {
            getStepDao().updateStepProgress(stepId, progress);
        }
    }

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        endAction(getCommand(cmdId));
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<?> commandBase = getCommand(cmdId);
        // This should be removed as soon as infra bug will be fixed and failed execution will reach endWithFailure
        commandBase.getParameters().setTaskGroupSuccess(false);
        endAction(commandBase);
    }

    private void endAction(CommandBase<?> commandBase) {
        commandBase.getReturnValue().setSucceeded(false);
        commandBase.endAction();
    }

    private VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    private StepDao getStepDao() {
        return DbFacade.getInstance().getStepDao();
    }
}
