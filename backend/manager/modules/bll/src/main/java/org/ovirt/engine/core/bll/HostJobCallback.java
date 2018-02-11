package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.job.Step.MAX_PROGRESS;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.HostJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.GetHostJobsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HostJobCallback extends ChildCommandsCallbackBase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private VDSBrokerFrontend resourceManager;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StepDao stepDao;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
                                               boolean anyFailed,
                                               List<Guid> childCmdIds,
                                               CommandExecutionStatus status,
                                               int completedChildren) {
        Guid cmdId = command.getCommandId();
        CommandEntity commandEntity = commandCoordinatorUtil.getCommandEntity(cmdId);
        ActionParametersBase cmdParams = commandEntity.getCommandParameters();
        Guid job = ((HostJobCommandParameters) cmdParams).getHostJobId();
        HostJobStatus jobStatus = null;
        Guid vdsId = cmdParams.getVdsRunningOn();
        VDS vds = vdsDao.get(vdsId);
        if (vds != null) {
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
                    handlePolledJobInfo(getCommand(cmdId), jobInfo);
                    jobStatus = jobInfo.getStatus();
                    updateStepProgress(commandEntity.getCommandContext().getStepId(), jobInfo.getProgress());
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

            if (command.shouldUpdateStepProgress() && jobStatus == HostJobStatus.done) {
                updateStepProgress(commandEntity.getCommandContext().getStepId(), MAX_PROGRESS);
            }
        } else {
            jobStatus = HostJobStatus.failed;
            log.info("Command {} id: '{}': job '{}' wasn't executed on any host, considering the job status as failed",
                    commandEntity.getCommandType(), cmdId, job);
        }

        command.getParameters().setTaskGroupSuccess(status == CommandExecutionStatus.EXECUTED
                && jobStatus == HostJobStatus.done);
        command.setCommandStatus(command.getParameters().getTaskGroupSuccess() ? CommandStatus.SUCCEEDED
                : CommandStatus.FAILED);
        log.info("Command {} id: '{}': execution was completed, the command status is '{}'",
                command.getActionType(), command.getCommandId(), command.getCommandStatus());
    }

    protected CommandBase<? extends HostJobCommandParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }

    private boolean isEntityPollingSupported(CommandBase<?> cmd) {
        return cmd instanceof EntityPollingCommand;
    }

    private HostJobInfo pollStorageJob(Guid jobId, Guid vdsId) {
        if (jobId == null) {
            return null;
        }

        GetHostJobsVDSCommandParameters p = new GetHostJobsVDSCommandParameters(vdsId, Collections
                .singletonList(jobId), getHostJobType());
        VDSReturnValue returnValue = resourceManager.runVdsCommand(VDSCommandType.GetHostJobs, p);
        return ((Map<Guid, HostJobInfo>) returnValue.getReturnValue()).get(jobId);
    }

    protected abstract HostJobType getHostJobType();

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

    private void handlePolledJobInfo(CommandBase<? extends HostJobCommandParameters> cmd, HostJobInfo jobInfo) {
        if (jobInfo.getStatus() != HostJobStatus.failed) {
            return;
        }

        // If a job failed on VDSM side, we may want to perform operations according to the job error or to consider the
        // job as successful on some cases.
        // each StorageJobCommand can override a method that'll handle the error and may return a different job status
        // if needed.
        if (jobInfo.getError() != null) {
            jobInfo.setStatus(((HostJobCommand) cmd).handleJobError(jobInfo.getError().getCode()));
            if (jobInfo.getStatus() == HostJobStatus.done) {
                // if the error inspection led us to decide to the job actual status is done, we can set the progress
                // to null so the progress will be considered as 100 for the command step (if present), it's better than
                // setting to 100 as we don't know if progress is actually reported for the operation.
                jobInfo.setProgress(null);
            }
        }
    }

    private HostJobStatus handleUndeterminedJobStatus(CommandBase<? extends HostJobCommandParameters> cmd,
                                                      boolean jobsReportedByHost) {
        // If the command supports entity polling, we can use it in order to determine the status.
        if (isEntityPollingSupported(cmd)) {
            log.info("Command {} id: '{}': attempting to determine the job status by polling the entity.",
                    cmd.getActionType(),
                    cmd.getCommandId());

            HostJobStatus jobStatus = pollEntity(cmd);
            if (jobStatus != null) {
                return jobStatus;
            }

            // If the job status couldn't been detected using entity polling and the command supports job fencing, we
            // can attempt to fence the job - which means that the host will fail to execute it if it attempts to.
            // Note that we may attempt to perform the fencing even if the job failed in case we couldn't determine
            // the job status, that'll confirm the job failure.
            //
            // Fencing the operation will usually be performed by executing an asynchronous fencing command on the
            // entity the job is supposed to be performed on.
            // If a fencing command was executed, the callback will wait for it to end and then will try to poll the
            // entity again (it'll be detected as a running child command). On synchronous fencing/no fencing we
            // will attempt to poll the entity again.
            ((EntityPollingCommand) cmd).attemptToFenceJob();

            return null;
        }

        if (((HostJobCommand) cmd).failJobWithUndeterminedStatus()) {
            log.error("Command {} id: '{}': failed to determine the actual job status, considering as failed as per" +
                            " the command implementation",
                    cmd.getActionType(),
                    cmd.getCommandId());
            return HostJobStatus.failed;
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

        // if we couldn't determine the job status, we'll retry to poll it.
        log.error("Command {} id: '{}': failed to determine the actual job status, will retry to poll the job soon",
                cmd.getActionType(),
                cmd.getCommandId());
        return null;
    }

    private void updateStepProgress(Guid stepId, Integer progress) {
        if (stepId != null) {
            stepDao.updateStepProgress(stepId, progress);
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
}
