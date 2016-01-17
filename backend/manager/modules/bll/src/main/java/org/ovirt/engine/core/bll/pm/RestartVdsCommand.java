package org.ovirt.engine.core.bll.pm;

import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_VDS_RESTART;
import static org.ovirt.engine.core.common.errors.EngineMessage.VAR__ACTION__RESTART;
import static org.ovirt.engine.core.common.errors.EngineMessage.VAR__TYPE__HOST;
import static org.ovirt.engine.core.common.errors.EngineMessage.VDS_FENCE_OPERATION_FAILED;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
 * @see FenceVdsBaseCommand#restartVdsVms() The critical section restaring the VMs
 */
@NonTransactiveCommandAttribute
public class RestartVdsCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {

    protected boolean skippedDueToFencingPolicy;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RestartVdsCommand(Guid commandId) {
        super(commandId);
        skippedDueToFencingPolicy = false;
    }

    public RestartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        skippedDueToFencingPolicy = false;
    }

    @Override
    protected boolean validate() {
        FenceValidator fenceValidator = new FenceValidator();
        List<String> messages = getReturnValue().getValidationMessages();
        boolean valid =
                fenceValidator.isHostExists(getVds(), messages)
                        && fenceValidator.isPowerManagementEnabledAndLegal(getVds(), getCluster(), messages)
                        && fenceValidator.isStartupTimeoutPassed(messages)
                        && isQuietTimeFromLastActionPassed()
                        && fenceValidator.isProxyHostAvailable(getVds(), messages);
        if (!valid) {
            handleError();
        }
        getReturnValue().setSucceeded(valid);
        return valid;
    }

    protected boolean isQuietTimeFromLastActionPassed() {
        // Check Quiet time between PM operations, this is done only if command is not internal.
        int secondsLeftToNextPmOp = isInternalExecution() ? 0 :
                DbFacade.getInstance()
                        .getAuditLogDao()
                        .getTimeToWaitForNextPmOp(getVds().getName(), AuditLogType.USER_VDS_RESTART.name());
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
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        final Guid vdsId = getVdsId();
        final String sessionId = getParameters().getSessionId();

        // do not try to stop Host if Host is reported as Down via PM
        if (new HostFenceActionExecutor(getVds()).isHostPoweredOff()) {
            returnValue.setSucceeded(true);
        }
        else {
            // execute StopVds action
            returnValue = executeVdsFenceAction(vdsId, sessionId, VdcActionType.StopVds);
        }
        if (wasSkippedDueToPolicy(returnValue)) {
            // fence execution was skipped due to fencing policy, host should be alive
            skippedDueToFencingPolicy = true;
            setSucceeded(false);
            setVdsStatus(VDSStatus.NonResponsive);
            return;
        } else if (returnValue.getSucceeded()) {
            executeFenceVdsManuallyAction(vdsId, sessionId);

            // execute StartVds action
            returnValue = executeVdsFenceAction(vdsId, sessionId, VdcActionType.StartVds);
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
    }

    private void executeFenceVdsManuallyAction(final Guid vdsId, String sessionId) {
        FenceVdsManualyParameters fenceVdsManuallyParams = new FenceVdsManualyParameters(false);
        fenceVdsManuallyParams.setStoragePoolId(getVds().getStoragePoolId());
        fenceVdsManuallyParams.setVdsId(vdsId);
        fenceVdsManuallyParams.setSessionId(sessionId);
        fenceVdsManuallyParams.setParentCommand(VdcActionType.RestartVds);

        // if fencing succeeded, call to reset irs in order to try select new spm
        runInternalAction(VdcActionType.FenceVdsManualy, fenceVdsManuallyParams, getContext());
    }

    private VdcReturnValueBase executeVdsFenceAction(final Guid vdsId,
                        String sessionId,
                        VdcActionType action) {
        FenceVdsActionParameters params = new FenceVdsActionParameters(vdsId);
        params.setParentCommand(VdcActionType.RestartVds);
        params.setSessionId(sessionId);
        params.setFencingPolicy(getParameters().getFencingPolicy());
        // If Host was in Maintenance, and was restarted manually , it should preserve its status after reboot
        if (getParameters().getParentCommand() != VdcActionType.VdsNotRespondingTreatment && getVds().getStatus() == VDSStatus.Maintenance) {
            params.setChangeHostToMaintenanceOnStart(true);
        }
        return runInternalAction(action, params, cloneContext().withoutExecutionContext());
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
    protected boolean wasSkippedDueToPolicy(VdcReturnValueBase result) {
        boolean skipped = false;
        if (result.getActionReturnValue() instanceof FenceOperationResult) {
            FenceOperationResult fenceResult = result.getActionReturnValue();
            skipped = fenceResult.getStatus() == Status.SKIPPED_DUE_TO_POLICY;
        }
        return skipped;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return createFenceExclusiveLocksMap(getVdsId());
    }

    public static Map<String, Pair<String, String>> createFenceExclusiveLocksMap(Guid vdsId) {
        return Collections.singletonMap(vdsId.toString(), LockMessagesMatchUtil.makeLockingPair(
                LockingGroup.VDS_FENCE,
                EngineMessage.POWER_MANAGEMENT_ACTION_ON_ENTITY_ALREADY_IN_PROGRESS));
    }

}
