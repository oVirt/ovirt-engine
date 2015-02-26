package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private FenceStatusReturnForXmlRpc _result;

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
            _result = fenceNode(getParameters().getAction());

            getVDSReturnValue().setSucceeded(false);
            if (getParameters().getAction() == FenceActionType.STATUS && _result.power != null) {
                String stat = _result.power.toLowerCase();
                String msg = _result.mStatus.mMessage;
                if ("on".equals(stat) || "off".equals(stat)) {
                    getVDSReturnValue().setSucceeded(true);
                } else {
                    if (!getParameters().getTargetVdsID().equals(Guid.Empty)) {
                        alertPowerManagementStatusFailed(msg);
                    }

                }
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(stat, msg);
                setReturnValue(fenceStatusReturnValue);
            } else {
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(
                        _result.operationStatus,
                        _result.mStatus.mMessage != null ? _result.mStatus.mMessage : ""
                );
                setReturnValue(fenceStatusReturnValue);
                getVDSReturnValue().setSucceeded(_result.mStatus.mCode == 0);
            }
        } else {
            // start/stop action was skipped, host is already turned on/off
            alertActionSkippedAlreadyInStatus();
            getVDSReturnValue().setSucceeded(true);
            setReturnValue(new FenceStatusReturnValue(FenceStatusReturnValue.SKIPPED_DUE_TO_STATUS, ""));
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
        boolean ret = false;
        FenceActionType action = getParameters().getAction();
        _result = fenceNode(FenceActionType.STATUS);
        if (_result.power != null) {
            String powerStatus = _result.power.toLowerCase();
            if ((action == FenceActionType.START && powerStatus.equals("on")) ||
                    action == FenceActionType.STOP && powerStatus.equals("off"))
                ret = true;
        }
        return ret;
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
        return _result.mStatus != null ? _result.mStatus : new StatusForXmlRpc();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
