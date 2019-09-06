package org.ovirt.engine.core.bll.pm;

import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_VDS_RESTART;
import static org.ovirt.engine.core.common.errors.EngineMessage.VAR__ACTION__RESTART;
import static org.ovirt.engine.core.common.errors.EngineMessage.VAR__TYPE__HOST;
import static org.ovirt.engine.core.common.errors.EngineMessage.VDS_FENCE_OPERATION_FAILED;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.VdsDynamicDaoImpl;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Send a Stop followed by Start action to a power management device.
 *
 * This command should be run exclusively on a host for it is assuming that when a host is down it can mark all VMs as
 * DOWN and start them on other host. 2 parallel action like that on the same server can lead to a race where the 1st
 * flow end by starting VMs and the 2nd flow marking them as down and starting another instance of VMs on other hosts,
 * leading to split-brain where 2 exact instances of VMs running in 2 different hosts and writing to the same disk.
 *
 * In order to make this flow distinct the child commands, Start, FenceManually and Stop are under the same lock as the
 * parent, preventing other Restart, Start, Stop,FenceVdsManually to interleave.
 *
 */
@NonTransactiveCommandAttribute
public class RestartVdsCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {

    @Inject
    private PreviousHostedEngineHost previousHostedEngineHost;
    @Inject
    private AuditLogDao auditLogDao;
    @Inject
    private HostLocking hostLocking;

    @Inject
    private VdsDynamicDaoImpl vdsDynamicDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RestartVdsCommand(Guid commandId) {
        super(commandId);
    }

    public RestartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        VDS host = getVds();
        List<String> messages = getReturnValue().getValidationMessages();
        boolean valid =
                fenceValidator.isHostExists(host, messages)
                        && fenceValidator.isPowerManagementEnabledAndLegal(host, getCluster(), messages)
                        && (previousHostedEngineHost.isPreviousHostId(host.getId())
                                || fenceValidator.isStartupTimeoutPassed(messages)
                                || host.isInFenceFlow())
                        && isQuietTimeFromLastActionPassed()
                        && fenceValidator.isProxyHostAvailable(host, messages);
        if (!valid) {
            handleError();
        }
        getReturnValue().setSucceeded(valid);
        return valid;
    }

    protected boolean isQuietTimeFromLastActionPassed() {
        // Check Quiet time between PM operations, this is done only if command is not internal.
        int secondsLeftToNextPmOp = isInternalExecution() ? 0 :
                auditLogDao.getTimeToWaitForNextPmOp(getVds().getName(), AuditLogType.USER_VDS_RESTART.name());
        if (secondsLeftToNextPmOp > 0) {
            addValidationMessage(EngineMessage.VDS_FENCE_DISABLED_AT_QUIET_TIME);
            addValidationMessageVariable("seconds", secondsLeftToNextPmOp);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    /**
     * Restart action is implemented by issuing stop followed by start
     */
    @Override
    protected void executeCommand() {
        ActionReturnValue returnValue;
        final Guid vdsId = getVdsId();
        final String sessionId = getParameters().getSessionId();

        // execute StopVds action
        returnValue = executeVdsFenceAction(vdsId, sessionId, ActionType.StopVds);
        if (isVdsNotResponding()) {
            updateHostInFenceFlow(vdsId, true);
        }

        if (wasSkippedDueToPolicy(returnValue)) {
            // fence execution was skipped due to fencing policy, host should be alive
            RestartVdsResult restartVdsResult = new RestartVdsResult();
            restartVdsResult.setSkippedDueToFencingPolicy(true);
            setActionReturnValue(restartVdsResult);
            setSucceeded(false);
            setVdsStatus(VDSStatus.NonResponsive);
            return;
        } else if (returnValue.getSucceeded()) {
            executeFenceVdsManuallyAction(vdsId, sessionId);

            // execute StartVds action
            returnValue = executeVdsFenceAction(vdsId, sessionId, ActionType.StartVds);
            setSucceeded(returnValue.getSucceeded());
        } else {
            handleError();
            setSucceeded(false);
        }
        if (!getSucceeded()) {
            log.warn("Restart host action failed, updating host '{}' to '{}'",
                    vdsId,
                    VDSStatus.NonResponsive.name());
            setVdsStatus(VDSStatus.NonResponsive);
        }
        // reset the flag since we have completed the restart action, not matter if it succeeded or not
        updateHostInFenceFlow(vdsId, false);
    }

    private void updateHostInFenceFlow(Guid hostId, boolean isInFenceFlow) {
        TransactionSupport.executeInNewTransaction(() -> {
            VdsDynamic vdsDynamic = vdsDynamicDao.get(hostId);
            vdsDynamic.setInFenceFlow(isInFenceFlow);
            vdsDynamicDao.update(vdsDynamic);
            return null;
        });
    }

    private void executeFenceVdsManuallyAction(final Guid vdsId, String sessionId) {
        FenceVdsManualyParameters fenceVdsManuallyParams = new FenceVdsManualyParameters(true);
        fenceVdsManuallyParams.setStoragePoolId(getVds().getStoragePoolId());
        fenceVdsManuallyParams.setVdsId(vdsId);
        fenceVdsManuallyParams.setSessionId(sessionId);
        fenceVdsManuallyParams.setParentCommand(ActionType.RestartVds);

        // if fencing succeeded, call to reset irs in order to try select new spm
        runInternalAction(ActionType.FenceVdsManualy, fenceVdsManuallyParams, getContext());
    }

    private ActionReturnValue executeVdsFenceAction(final Guid vdsId,
                        String sessionId,
                        ActionType action) {
        FenceVdsActionParameters params = new FenceVdsActionParameters(vdsId);
        params.setParentCommand(ActionType.RestartVds);
        params.setSessionId(sessionId);
        params.setFencingPolicy(getParameters().getFencingPolicy());
        // If Host was in Maintenance, and was restarted manually , it should preserve its status after reboot
        // If change host to maintenance was requested by user on reboot then move it maintenance
        if (!isVdsNotResponding()
                && (isGoToMaintenanceRequested() || wasPreviouslyInMaintenance())) {
            params.setChangeHostToMaintenanceOnStart(true);
        }
        return runInternalAction(action, params, cloneContext().withoutExecutionContext());
    }

    private boolean wasPreviouslyInMaintenance() {
        return getVds().getStatus() == VDSStatus.Maintenance;
    }

    private boolean isGoToMaintenanceRequested() {
        return getParameters().isChangeHostToMaintenanceOnStart();
    }

    private boolean isVdsNotResponding() {
        return getParameters().getParentCommand() == ActionType.VdsNotRespondingTreatment;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__RESTART);
    }

    protected void handleError() {
        addValidationMessage(VDS_FENCE_OPERATION_FAILED);
        addValidationMessage(VAR__TYPE__HOST);
        addValidationMessage(VAR__ACTION__RESTART);
        log.error("Failed to run RestartVdsCommand on vds '{}'", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return isInternalExecution() ? SYSTEM_VDS_RESTART : USER_VDS_RESTART;
        } else {
            return isInternalExecution() ? SYSTEM_FAILED_VDS_RESTART : USER_FAILED_VDS_RESTART;
        }
    }

    /**
     * Determines according to the return status from the Ovirt command whether the fence-operation has been skipped due
     * to policy.
     */
    protected boolean wasSkippedDueToPolicy(ActionReturnValue result) {
        boolean skipped = false;
        if (result.getActionReturnValue() instanceof FenceOperationResult) {
            FenceOperationResult fenceResult = result.getActionReturnValue();
            skipped = fenceResult.getStatus() == Status.SKIPPED_DUE_TO_POLICY;
        }
        return skipped;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return hostLocking.getPowerManagementLock(getVdsId());
    }
}
