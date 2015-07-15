package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    /**
     * VDS which acts as fence proxy
     */
    private VDS proxyVds;

    /**
     * VDS which should be fenced
     */
    private VDS targetVds;

    @Inject
    private AuditLogDirector auditLogDirector;

    public FenceVdsVDSCommand(P parameters) {
        super(parameters);
    }

    protected VDS getProxyVds() {
        if (proxyVds == null) {
            proxyVds = getDbFacade().getVdsDao().get(getParameters().getVdsId());
        }
        return proxyVds;
    }

    protected VDS getTargetVds() {
        if (targetVds == null) {
            targetVds = getDbFacade().getVdsDao().get(getParameters().getTargetVdsID());
        }
        return targetVds;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        // ignore starting already started host or stopping already stopped host.
        if (getParameters().getAction() == FenceActionType.STATUS
                || !isAlreadyInRequestedStatus()) {
            FenceStatusReturnForXmlRpc result = fenceNode(getParameters().getAction());

            FenceOperationResult actionResult = new FenceOperationResult(
                    getParameters().getAction(),
                    result.getXmlRpcStatus().code,
                    result.getXmlRpcStatus().message,
                    result.power,
                    result.operationStatus);
            setReturnValue(actionResult);
            getVDSReturnValue().setSucceeded(actionResult.getStatus() != Status.ERROR);

            if (getParameters().getAction() == FenceActionType.STATUS
                    && actionResult.getPowerStatus() == PowerStatus.UNKNOWN
                    && !getParameters().getTargetVdsID().equals(Guid.Empty)) {
                alertPowerManagementStatusFailed(actionResult.getMessage());
            }

        } else {
            // start/stop action was skipped, host is already turned on/off
            alertActionSkippedAlreadyInStatus();
            getVDSReturnValue().setSucceeded(true);
            setReturnValue(
                    new FenceOperationResult(Status.SKIPPED_ALREADY_IN_STATUS, PowerStatus.UNKNOWN));
        }
    }

    /**
     * Alerts if power management status failed.
     *
     * @param reason
     *            The reason.
     */
    protected void alertPowerManagementStatusFailed(String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getParameters().getTargetVdsID());
        alert.addCustomValue("Reason", reason);
        auditLogDirector.log(alert, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
    }

    /**
     * Alerts when power management stop was skipped because host is already down.
     */
    protected void alertActionSkippedAlreadyInStatus() {
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("HostName", getTargetVds().getName());
        auditLogable.addCustomValue("AgentStatus", getParameters().getAction().getValue());
        auditLogable.addCustomValue("Operation", getParameters().getAction().toString());
        auditLogDirector.log(auditLogable, AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS);
    }

    /**
     * Checks if Host is already in the requested status. If Host is Down and a Stop command is issued or if Host is Up
     * and a Start command is issued command should do nothing.
     */
    private boolean isAlreadyInRequestedStatus() {
        FenceStatusReturnForXmlRpc result = fenceNode(FenceActionType.STATUS);
        FenceOperationResult actionResult = new FenceOperationResult(
                FenceActionType.STATUS,
                result.getXmlRpcStatus().code,
                result.getXmlRpcStatus().message,
                result.power,
                result.operationStatus);

        return actionResult.getStatus() == Status.SUCCESS
                && actionResult.getPowerStatus() == getRequestedPowerStatus(getParameters().getAction());
    }

    /**
     * Returns requested power status for specified fence operations
     */
    protected PowerStatus getRequestedPowerStatus(FenceActionType fenceAction) {
        return fenceAction == FenceActionType.START ? PowerStatus.ON : PowerStatus.OFF;
    }

    protected FenceStatusReturnForXmlRpc fenceNode(FenceActionType fenceAction) {
        FenceAgent agent = getParameters().getFenceAgent();
        return getBroker().fenceNode(
                agent.getIp(),
                agent.getPort() == null ? "" : agent.getPort().toString(),
                agent.getType(),
                agent.getUser(),
                agent.getPassword(),
                fenceAction.getValue(),
                "",
                agent.getOptions(),
                getParameters().getAction() != FenceActionType.STATUS
                        ? getParameters().getFencingPolicyParams()
                        : null);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        StatusForXmlRpc status = new StatusForXmlRpc();
        FenceOperationResult result = (FenceOperationResult) getReturnValue();
        if (result == null) {
            // unexpected error happened
            status.code = 1;
            status.message = "";
        } else {
            // status result from action result
            status.code = result.getStatus() == Status.ERROR ? 1 : 0;
            status.message = result.getMessage();
        }
        return status;
    }
}
