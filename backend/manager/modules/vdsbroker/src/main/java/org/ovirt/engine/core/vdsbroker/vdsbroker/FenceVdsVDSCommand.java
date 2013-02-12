package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private FenceStatusReturnForXmlRpc _result;

    public FenceVdsVDSCommand(P parameters) {
        super(parameters);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param reason
     *            The reason.
     */
    private void Alert(AuditLogType logType, String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getParameters().getTargetVdsID());
        alert.addCustomValue("Reason", reason);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts if power management status failed.
     *
     * @param reason
     *            The reason.
     */
    protected void AlertPowerManagementStatusFailed(String reason) {
        Alert(AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, reason);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        VdsFenceOptions vdsFencingOptions = new VdsFenceOptions(getParameters().getType(),
                getParameters().getOptions());
        String options = vdsFencingOptions.ToInternalString();
        // ignore starting already started host or stopping already stopped host.
        if (!isAlreadyInRequestedStatus(options)) {
            _result = getBroker().fenceNode(getParameters().getIp(), "",
                    getParameters().getType(), getParameters().getUser(),
                    getParameters().getPassword(), GetActualActionName(), "", options);

            ProceedProxyReturnValue();
            getVDSReturnValue().setSucceeded(false);
            if (getParameters().getAction() == FenceActionType.Status && _result.Power != null) {
                String stat = _result.Power.toLowerCase();
                String msg = _result.mStatus.mMessage;
                if ("on".equals(stat) || "off".equals(stat)) {
                    getVDSReturnValue().setSucceeded(true);
                } else {
                    if (!getParameters().getTargetVdsID().equals(Guid.Empty)) {
                        AlertPowerManagementStatusFailed(msg);
                    }

                }
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(stat, msg);
                setReturnValue(fenceStatusReturnValue);
            } else {
                setReturnValue((_result.mStatus.mMessage != null) ? _result.mStatus.mMessage : "");
                getVDSReturnValue().setSucceeded(true);
            }
        } else {
            handleSkippedOperation();
        }
    }

    /**
    * Handles cases where fencing operation was skipped (host is already in requested state)
    */
    private void handleSkippedOperation() {
        FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(FenceStatusReturnValue.SKIPPED, "");
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("HostName",
                (DbFacade.getInstance().getVdsDao().get(getParameters().getTargetVdsID())).getName());
        auditLogable.addCustomValue("AgentStatus", GetActualActionName());
        auditLogable.addCustomValue("Operation", getParameters().getAction().toString());
        AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS);
        getVDSReturnValue().setSucceeded(true);
        setReturnValue(fenceStatusReturnValue);
    }

    /**
     * Checks if Host is already in the requested status.
     * If Host is Down and a Stop command is issued or
     * if Host is Up and a Start command is issued
     * command should do nothing.
     *
     * @param options
     *            Fencing options passed to the agent
     * @return
     */
    private boolean isAlreadyInRequestedStatus(String options) {
        boolean ret = false;
        FenceActionType action = getParameters().getAction();
        _result = getBroker().fenceNode(getParameters().getIp(), "",
                getParameters().getType(), getParameters().getUser(),
                getParameters().getPassword(), "status", "", options);
        if (_result.Power != null) {
            String powerStatus = _result.Power.toLowerCase();
            if ((action == FenceActionType.Start && powerStatus.equals("on")) ||
                    action == FenceActionType.Stop && powerStatus.equals("off"))
                ret = true;
        }
        return ret;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return (_result.mStatus != null) ? _result.mStatus : new StatusForXmlRpc();
    }

    private String GetActualActionName() {
        String actualActionName;
        switch (getParameters().getAction()) {
        case Restart:
            actualActionName = "reboot";
            break;
        case Start:
            actualActionName = "on";
            break;
        case Stop:
            actualActionName = "off";
            break;
        default:
            actualActionName = "status";
            break;
        }
        return actualActionName;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
