package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_VDS_RESTART;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__ACTION__RESTART;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__TYPE__HOST;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VDS_FENCE_OPERATION_FAILED;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
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

    private static final String INTERNAL_FENCE_USER = "Engine";

    protected boolean skippedDueToFencingPolicy;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestartVdsCommand(Guid commandId) {
        super(commandId);
        skippedDueToFencingPolicy = false;
    }

    public RestartVdsCommand(T parameters) {
        this(parameters, null);
    }

    public RestartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        skippedDueToFencingPolicy = false;
    }

    @Override
    protected boolean canDoAction() {
        FenceValidator fenceValidator = new FenceValidator();
        List<String> messages = getReturnValue().getCanDoActionMessages();
        boolean canDo =
                fenceValidator.isHostExists(getVds(), messages)
                        && fenceValidator.isPowerManagementEnabledAndLegal(getVds(), getVdsGroup(), messages)
                        && fenceValidator.isStartupTimeoutPassed(messages)
                        && isQuietTimeFromLastActionPassed()
                        && fenceValidator.isProxyHostAvailable(getVds(), messages);
        if (!canDo) {
            handleError();
        }
        getReturnValue().setSucceeded(canDo);
        return canDo;
    }

    protected boolean isQuietTimeFromLastActionPassed() {
        // Check Quiet time between PM operations, this is done only if command is not internal.
        int secondsLeftToNextPmOp = (isInternalExecution()) ? 0 :
                DbFacade.getInstance()
                        .getAuditLogDao()
                        .getTimeToWaitForNextPmOp(getVds().getName(), AuditLogType.USER_VDS_RESTART.name());
        if (secondsLeftToNextPmOp > 0) {
            addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_QUIET_TIME);
            addCanDoActionMessageVariable("seconds", secondsLeftToNextPmOp);
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
        if (isPmReportsStatusDown()) {
            returnValue.setSucceeded(true);
        }
        else {
            // execute StopVds action
            returnValue = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Stop, VdcActionType.StopVds);
        }
        if (wasSkippedDueToPolicy(returnValue)) {
            // fence execution was skipped due to fencing policy, host should be alive
            skippedDueToFencingPolicy = true;
            setSucceeded(false);
            runVdsCommand(VDSCommandType.SetVdsStatus, new SetVdsStatusVDSCommandParameters(vdsId,
                    VDSStatus.NonResponsive));
            return;
        } else if (returnValue.getSucceeded()) {
            executeFenceVdsManuallyAction(vdsId, sessionId);

            // execute StartVds action
            returnValue = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Start, VdcActionType.StartVds);
            setSucceeded(returnValue.getSucceeded());
        } else {
            handleError();
            setSucceeded(false);
        }
        if (!getSucceeded()) {
            log.warn("Restart host action failed, updating host '{}' to '{}'",
                    vdsId,
                    VDSStatus.NonResponsive.name());
            runVdsCommand(VDSCommandType.SetVdsStatus, new SetVdsStatusVDSCommandParameters(vdsId,
                    VDSStatus.NonResponsive));
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
                        FenceActionType fenceAction,
                        VdcActionType action) {
        FenceVdsActionParameters params = new FenceVdsActionParameters(vdsId, fenceAction);
        params.setParentCommand(VdcActionType.RestartVds);
        params.setSessionId(sessionId);
        params.setFencingPolicy(getParameters().getFencingPolicy());
        // If Host was in Maintenance, and was restarted manually , it should preserve its status after reboot
        if (getParameters().getParentCommand() != VdcActionType.VdsNotRespondingTreatment && getVds().getStatus() == VDSStatus.Maintenance) {
            params.setChangeHostToMaintenanceOnStart(true);
        }
        return runInternalAction(action, params, getContext());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESTART);
    }

    protected void handleError() {
        addCanDoActionMessage(VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VAR__TYPE__HOST);
        addCanDoActionMessage(VAR__ACTION__RESTART);
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
        if (result.getActionReturnValue() instanceof VDSFenceReturnValue) {
            VDSFenceReturnValue fenceReturnValue = result.getActionReturnValue();
            if (fenceReturnValue.getReturnValue() instanceof FenceStatusReturnValue) {
                skipped = ((FenceStatusReturnValue) fenceReturnValue.getReturnValue()).getIsSkipped();
            }
        }
        return skipped;
    }

    @Override
    public String getUserName() {
        String userName = super.getUserName();
        return StringUtils.isEmpty(userName) ? INTERNAL_FENCE_USER : userName;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return createFenceExclusiveLocksMap(getVdsId());
    }

    public static Map<String, Pair<String, String>> createFenceExclusiveLocksMap(Guid vdsId) {
        return Collections.singletonMap(vdsId.toString(), LockMessagesMatchUtil.makeLockingPair(
                LockingGroup.VDS_FENCE,
                VdcBllMessages.POWER_MANAGEMENT_ACTION_ON_ENTITY_ALREADY_IN_PROGRESS));
    }

}
