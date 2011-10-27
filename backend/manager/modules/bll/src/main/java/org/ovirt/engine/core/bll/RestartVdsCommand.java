package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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
        returnValueBase = executeStopVdsFenceAction(vdsId, sessionId);
        if (returnValueBase.getSucceeded()) {
            executeFenceVdsManulalyAction(vdsId, sessionId);

            // execute StartVds action
            returnValueBase = executeStartVdsFenceAction(vdsId, sessionId);
            setSucceeded(returnValueBase.getSucceeded());
            setFencingSucceeded(getSucceeded());
        } else {
            setSucceeded(false);
        }
    }

    private void executeFenceVdsManulalyAction(final Guid vdsId, String sessionId) {
        FenceVdsManualyParameters fenceVdsManuallyParams = new FenceVdsManualyParameters(false);
        fenceVdsManuallyParams.setStoragePoolId(getVds().getstorage_pool_id());
        fenceVdsManuallyParams.setVdsId(vdsId);
        fenceVdsManuallyParams.setShouldBeLogged(false);
        fenceVdsManuallyParams.setSessionId(sessionId);

        // if fencing succeeded, call to reset irs in order to try select new spm
        Backend.getInstance().runInternalAction(VdcActionType.FenceVdsManualy, fenceVdsManuallyParams);
    }

    private VdcReturnValueBase executeStopVdsFenceAction(final Guid vdsId, String sessionId) {
        FenceVdsActionParameters stopVdsParams = new FenceVdsActionParameters(vdsId, FenceActionType.Stop);
        stopVdsParams.setParentCommand(VdcActionType.RestartVds);
        stopVdsParams.setShouldBeLogged(false);
        stopVdsParams.setSessionId(sessionId);
        return Backend.getInstance().runInternalAction(VdcActionType.StopVds, stopVdsParams);
    }

    private VdcReturnValueBase executeStartVdsFenceAction(final Guid vdsId, String sessionId) {
        // TODO 1: evaluate the impact of having parent command of StartVds to be RestartVds
        // TODO 2: consider merge executeStopVdsFenceAction and executeStartVdsFenceAction
        FenceVdsActionParameters startVdsParams = new FenceVdsActionParameters(vdsId, FenceActionType.Start);
        startVdsParams.setShouldBeLogged(false);
        startVdsParams.setSessionId(sessionId);
        return Backend.getInstance().runInternalAction(VdcActionType.StartVds, startVdsParams);
    }

    /**
     * If failed to restart the host, move its status to NonResponsive
     */
    @Override
    public void Rollback() {
        super.Rollback();
        final Guid vdsId = getVdsId();
        log.warnFormat("Restart host action failed, updating host {0} to {1}", vdsId, VDSStatus.NonResponsive.name());
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                                new SetVdsStatusVDSCommandParameters(vdsId, VDSStatus.NonResponsive));
                return null;
            }
        });
    }

    @Override
    protected void HandleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCING_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESTART);
        log.errorFormat("Failed to run RestartVdsCommand on vds :{0}", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_RESTART : AuditLogType.USER_FAILED_VDS_RESTART;
    }

    private static LogCompat log = LogFactoryCompat.getLog(RestartVdsCommand.class);

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
