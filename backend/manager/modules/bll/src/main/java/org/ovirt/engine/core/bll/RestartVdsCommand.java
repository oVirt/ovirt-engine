package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_VDS_RESTART;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__ACTION__RESTART;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__TYPE__HOST;
import static org.ovirt.engine.core.common.errors.VdcBllMessages.VDS_FENCE_OPERATION_FAILED;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Send a Stop followed by Start action to a power management device.
 *
 * This command should be run exclusively on a host for it is assuming that when
 * a host is down it can mark all VMs as DOWN and start them on other host.
 * 2 parallel action like that on the same server can lead to a race where
 * the 1st flow end by starting VMs and the 2nd flow marking them as down and
 * starting another instance of VMs on other hosts, leading to split-brain where
 * 2 exact instances of VMs running in 2 different hosts and writing to the same disk.
 *
 * In order to make this flow distinct the child commands, Start, FenceManually and Stop
 * are under the same lock as the parent, preventing other Restart, Start, Stop,FenceVdsManually to interleave.
 *
 * @see FenceVdsBaseCommand#restartVdsVms() The critical section restaring the VMs
 */
@NonTransactiveCommandAttribute
public class RestartVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {

    protected List<VM> getVmList() {
        return mVmList;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestartVdsCommand(Guid commandId) {
        super(commandId);
    }

    public RestartVdsCommand(T parameters) {
        this(parameters, null);
    }

    public RestartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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
        VdcReturnValueBase returnValueBase = new VdcReturnValueBase();
        final Guid vdsId = getVdsId();
        final String sessionId = getParameters().getSessionId();

        // do not try to stop Host if Host is reported as Down via PM
        if (isPmReportsStatusDown()) {
            returnValueBase.setSucceeded(true);
        }
        else {
            // execute StopVds action
            returnValueBase = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Stop, VdcActionType.StopVds);
        }
        if (wasSkippedDueToPolicy(returnValueBase.getActionReturnValue())) {
            // fence execution was skipped due to fencing policy, host should be alive
            setSucceeded(false);
            setFenceSucceeded(false);
            skippedDueToFencingPolicy = true;
            runVdsCommand(VDSCommandType.SetVdsStatus, new SetVdsStatusVDSCommandParameters(vdsId,
                    VDSStatus.NonResponsive));
            return;
        }
        if (returnValueBase.getSucceeded()) {
            executeFenceVdsManuallyAction(vdsId, sessionId);

            // execute StartVds action
            returnValueBase = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Start, VdcActionType.StartVds);
            setSucceeded(returnValueBase.getSucceeded());
            setFenceSucceeded(getSucceeded());
        } else {
            super.handleError();
            setSucceeded(false);
        }
        if (!getSucceeded()) {
            log.warnFormat("Restart host action failed, updating host {0} to {1}",
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

    @Override
    protected void handleError() {
        addCanDoActionMessage(VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VAR__TYPE__HOST);
        addCanDoActionMessage(VAR__ACTION__RESTART);
        log.errorFormat("Failed to run RestartVdsCommand on vds :{0}", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return isInternalExecution() ? SYSTEM_VDS_RESTART : USER_VDS_RESTART;
        } else {
            return isInternalExecution() ? SYSTEM_FAILED_VDS_RESTART : USER_FAILED_VDS_RESTART;
        }
    }

    @Override
    protected int getRerties() {
        return 0;
    }

    @Override
    protected int getDelayInSeconds() {
        return 0;
    }

    @Override
    protected void handleSpecificCommandActions() {
    }

}
