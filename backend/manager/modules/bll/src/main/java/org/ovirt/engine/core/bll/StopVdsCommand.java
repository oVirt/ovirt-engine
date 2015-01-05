package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__ACTION__STOP;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send a Stop action to a power control device.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 * @see FenceVdsBaseCommand#restartVdsVms()
 */
@NonTransactiveCommandAttribute
public class StopVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    private final String VDSM_STATUS_OFF = "off";
    private static final Logger log = LoggerFactory.getLogger(StopVdsCommand.class);

    public StopVdsCommand(T parameters) {
        this(parameters, null);
    }

    public StopVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getParameters().getParentCommand() == VdcActionType.Unknown) {
            retValue = super.canDoAction();
            if (getVds() != null && getVds().getStatus() != VDSStatus.Maintenance) {
                addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_STOP);
                retValue = false;
            }
        }
        getReturnValue().setCanDoAction(retValue);
        return retValue;
    }

    @Override
    protected void setStatus() {

        VDSStatus newStatus = VDSStatus.Down;
        if (getParameters().getParentCommand() == VdcActionType.RestartVds) {
            // In case the stop was issued as a result of VDS command , we
            // cannot set the VDS to down -
            // According to bug fix #605215 it can be that backend will crash
            // during restart, and upon restart, all down VDS are not
            // monitored. Instead, we will set the status to rebooting

            newStatus = VDSStatus.Reboot;
        }
        setStatus(newStatus);

    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VAR__ACTION__STOP);
    }

    @Override
    protected void handleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__STOP);
        log.error("Failed to run StopVdsCommand on vds '{}'", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_STOP : AuditLogType.USER_FAILED_VDS_STOP;
    }

    @Override
    protected void handleSpecificCommandActions() {
        List<VM> mVmList = getVmList();
        if (mVmList.size() > 0) {
            RestartVdsVmsOperation restartVmsOper = new RestartVdsVmsOperation(
                    getContext(),
                    getVds()
            );
            restartVmsOper.restartVms(mVmList);
            runVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                    new UpdateVdsVMsClearedVDSCommandParameters(getVds().getId()));
        }
    }

    @Override
    protected int getWaitForStatusRerties() {
        return Config.<Integer> getValue(ConfigValues.FenceStopStatusRetries);
    }

    @Override
    protected int getDelayInSeconds() {
        return Config.<Integer> getValue(ConfigValues.FenceStopStatusDelayBetweenRetriesInSec);
    }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != VdcActionType.RestartVds) {
            super.freeLock();
        }
    }

    @Override
    protected int getFenceRetries() {
        return 0;
    }

    @Override
    /**
     * Attempt to stop the host using the provided agents concurrently.
     *
     */
    protected VDSFenceReturnValue fenceConcurrently(List<FenceAgent> agents) {
        try {
            // create a 'task' for each agent, and insert tasks into an 'executor' which executes tasks
            // concurrently. (meaning: attempt to stop the host using the agents concurrently).
            ExecutorCompletionService<VDSFenceReturnValue> tasksExecutor =
                    ThreadPoolUtil.createCompletionService(createTasks(agents));

            // run the tasks concurrently, return all results in a list.
            List<VDSFenceReturnValue> results = collectResults(tasksExecutor, agents.size());

            // If any agent has failed, return the result for that agent, since one failure is enough to fail 'Stop'. If
            // no agent has failed, return any successful agent.
            VDSFenceReturnValue result = getMostRelevantResult(results);

            if (result != null) {
                if (result.getSucceeded()) {
                    int numOfSkipped = countSkippedOperations(results);
                    if (numOfSkipped == 0) {
                        // no agent reported that stop operation was skipped, continue with stop operation
                        handleSpecificCommandActions();
                    } else {
                        // check if all agents reported that stop operation has skipped,
                        // if so, just return, skipping is handled in caller
                        if (numOfSkipped < agents.size()) {
                            // not all agents skipped stop operation, mark as error
                            result.setSucceeded(false);
                        }
                    }
                } else {
                    logConcurrentAgentsFailure(FenceActionType.Stop, agents, result);
                }
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception", e);
        }
        return null;
    }

    /**
     * Execute the supplied tasks concurrently.
     *
     * @return list of result objects for the executed tasks.
     * @throws ExecutionException
     */
    private List<VDSFenceReturnValue> collectResults(ExecutorCompletionService<VDSFenceReturnValue> tasksExecutor,
            int threadNum)
            throws InterruptedException, ExecutionException {
        List<VDSFenceReturnValue> executedTasks = new LinkedList<>();
        for (int i = 0; i < threadNum; i++) {
            executedTasks.add(tasksExecutor.take().get());
        }
        return executedTasks;
    }

    /**
     * Invoked when stopping a host concurrently using 2 agents. If any agent has failed, return the result for that
     * agent, since one failure is enough to fail 'Stop'. If no agent has failed, return any successful agent.
     */
    private VDSFenceReturnValue getMostRelevantResult(List<VDSFenceReturnValue> results)
            throws InterruptedException,
            ExecutionException {
        for (VDSFenceReturnValue result : results) {
            if (!result.getSucceeded()) {
                return result;
            }
        }
        return results.get(0);
    }

    @Override
    protected String getRequiredStatus() {
        return VDSM_STATUS_OFF;
    }

    @Override
    protected FenceActionType getAction() {
        return FenceActionType.Stop;
    }

    @Override
    protected String getRequestedAuditEvent() {
        return AuditLogType.USER_VDS_START.name();
    }

    @Override
    protected void setup() {
        // Set status immediately to prevent a race (BZ 636950/656224)
        setStatus();
    }

    @Override
    protected void teardown() {
        // Successful fencing with reboot or shutdown op. Clear the power management policy flag
        if (getParameters().getKeepPolicyPMEnabled() == false) {
            getVds().setPowerManagementControlledByPolicy(false);
            getDbFacade().getVdsDynamicDao().updateVdsDynamicPowerManagementPolicyFlag(
                    getVdsId(),
                    getVds().getDynamicData().isPowerManagementControlledByPolicy());
        }
    }
}
