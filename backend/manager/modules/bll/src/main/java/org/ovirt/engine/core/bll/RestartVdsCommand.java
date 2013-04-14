package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.SYSTEM_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_FAILED_VDS_RESTART;
import static org.ovirt.engine.core.common.AuditLogType.USER_VDS_RESTART;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

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
        super(parameters);
    }

    /**
     * Restart action is implemented by issuing stop followed by start
     */
    @Override
    protected void executeCommand() {
        VdcReturnValueBase returnValueBase;
        final Guid vdsId = getVdsId();
        final String sessionId = getParameters().getSessionId();

        // execute StopVds action
        returnValueBase = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Stop, VdcActionType.StopVds);
        if (returnValueBase.getSucceeded()) {
            executeFenceVdsManuallyAction(vdsId, sessionId);

            // execute StartVds action
            returnValueBase = executeVdsFenceAction(vdsId, sessionId, FenceActionType.Start, VdcActionType.StartVds);
            setSucceeded(returnValueBase.getSucceeded());
            setFenceSucceeded(getSucceeded());
        } else {
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

        // if fencing succeeded, call to reset irs in order to try select new spm
        Backend.getInstance().runInternalAction(VdcActionType.FenceVdsManualy, fenceVdsManuallyParams);
    }

    private VdcReturnValueBase executeVdsFenceAction(final Guid vdsId,
                        String sessionId,
                        FenceActionType fenceAction,
                        VdcActionType action) {
        FenceVdsActionParameters params = new FenceVdsActionParameters(vdsId, fenceAction);
        params.setParentCommand(VdcActionType.RestartVds);
        params.setSessionId(sessionId);
        return Backend.getInstance().runInternalAction(action, params);
    }

    @Override
    protected void HandleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESTART);
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
