package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * Send a Start action to a power control device.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 * @see FenceVdsBaseCommand#restartVdsVms()
 */
@NonTransactiveCommandAttribute
public class StartVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    private static final String VDSM_STATUS_ON = "on";
    public StartVdsCommand(T parameters) {
        this(parameters, null);
    }

    public StartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = super.canDoAction();
        VDS vds = getVds();
        if (vds != null) {
            VDSStatus vdsStatus = vds.getStatus();
            if (vdsStatus == VDSStatus.Connecting) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_INTERMITENT_CONNECTIVITY);

            } else if (!legalStatusForStartingVds(vdsStatus)) {
                addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_START);
                retValue = false;
                log.error("VDS status for vds '{}' '{}' is '{}'", vds.getId(), vds.getName(), vdsStatus);
            }
        }
        return retValue;
    }

    @Override
    /**
     * Attempt to 'start' the host using the provided agents. This method receives several agents which have the same
     * 'order' (thus considered 'concurrent') and runs them concurrently until the first succeeds. It returns the first
     * agent that succeeded, or any failed agent if none succeeded.
     */
    protected VDSFenceReturnValue fenceConcurrently(List<FenceAgent> agents) {
        // create a 'task' for each agent, and insert tasks into a concurrent 'executor'.
        ExecutorCompletionService<VDSFenceReturnValue> tasksExecutor = ThreadPoolUtil.createCompletionService();
        List<Future<VDSFenceReturnValue>> futures =
                ThreadPoolUtil.submitTasks(tasksExecutor, createTasks(agents));
        VDSFenceReturnValue result = null;
        for (int i = 0; i < agents.size(); ++i) {
            try {
                result = tasksExecutor.take().get();
                if (result != null && result.getSucceeded()) {
                    cancelFutureTasks(futures, result.getFenceAgentUsed().getId());
                    break;
                }
            } catch (ExecutionException | InterruptedException e) {
                log.warn("Attempt to start host '{}' using one of its agents has failed: {}",
                        getVdsName(),
                        e.getMessage());
                log.debug("Exception", e);
            }
        }
        if (result != null && !result.getSucceeded()) {
            logConcurrentAgentsFailure(FenceActionType.START, agents, result);
        }
        return result;
    }

    private void cancelFutureTasks(List<Future<VDSFenceReturnValue>> futures, Guid agentId) {
        if (!futures.isEmpty()) {
            log.info("Start of host '{}' succeeded using fencing agent '{}',"
                            + " cancelling concurrent attempts by other agents to start the host",
                    getVdsName(),
                    agentId);
        }
        for (Future<VDSFenceReturnValue> future : futures) {
            try {
                log.info("Cancelling agent '{}' ", future.get().getFenceAgentUsed().getId());
            } catch (InterruptedException | ExecutionException e) {
                // do nothing
            }
            future.cancel(true);
        }
    }
    protected boolean legalStatusForStartingVds(VDSStatus status) {
        return status == VDSStatus.Down || status == VDSStatus.NonResponsive || status == VDSStatus.Reboot || status == VDSStatus.Maintenance;
    }

    @Override
    protected void setStatus() {
        if (getParameters().isChangeHostToMaintenanceOnStart()) {
            setStatus(VDSStatus.Maintenance);
        }
        else {
            setStatus(VDSStatus.NonResponsive);
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
    }

    @Override
    protected void handleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
        log.error("Failed to run StartVdsCommand on host '{}'", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);

        return getSucceeded() ? AuditLogType.USER_VDS_START : AuditLogType.USER_FAILED_VDS_START;
    }

    @Override
    protected void handleSpecificCommandActions() {
        RestartVdsVmsOperation restartVmsOper = new RestartVdsVmsOperation(
                getContext(),
                getVds()
        );
        restartVmsOper.restartVms(getVmList());
    }

    @Override
    protected int getWaitForStatusRerties() {
        return Config.<Integer> getValue(ConfigValues.FenceStartStatusRetries);
    }

    @Override
    protected int getDelayInSeconds() {
        return Config.<Integer> getValue(ConfigValues.FenceStartStatusDelayBetweenRetriesInSec);
    }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != VdcActionType.RestartVds) {
            super.freeLock();
        }
    }

    @Override
    protected int getFenceRetries() {
        // in case of 'Start' allow one retry since there is a chance that Agent & Host use the same power supply
        // and a Start command had failed (because we just get success on the script invocation and not on its
        // result).
        return 1;
    }

    @Override
    protected String getRequiredStatus() {
        return VDSM_STATUS_ON;
    }

    @Override
    protected FenceActionType getAction() {
        return FenceActionType.START;
    }

    @Override
    protected String getRequestedAuditEvent() {
        return AuditLogType.USER_VDS_STOP.name();
    }

    @Override
    protected void setup() {
        // Set status immediately to prevent a race (BZ 636950/656224)
        // Skip setting status if action is manual Start and Host was in Maintenance
        if (getVds().getStatus() != VDSStatus.Maintenance) {
            setStatus();
        }
    }

    @Override
    protected void teardown() {
        // TODO Auto-generated method stub

    }
}
