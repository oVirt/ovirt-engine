package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {

    /**
     * VDS which should be fenced
     */
    private VDS targetVds;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsDao vdsDao;

    public FenceVdsVDSCommand(P parameters) {
        super(parameters);
    }

    protected VDS getTargetVds() {
        if (targetVds == null) {
            targetVds = vdsDao.get(getParameters().getTargetVdsID());
        }
        return targetVds;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        FenceStatusReturn result = fenceNode(getParameters().getAction());

        FenceOperationResult actionResult = new FenceOperationResult(
                getParameters().getAction(),
                result.getStatus().code,
                result.getStatus().message,
                result.power,
                result.operationStatus);
        setReturnValue(actionResult);
        getVDSReturnValue().setSucceeded(actionResult.getStatus() != FenceOperationResult.Status.ERROR);

        if (getParameters().getAction() == FenceActionType.STATUS
                && actionResult.getPowerStatus() == PowerStatus.UNKNOWN
                && !getParameters().getTargetVdsID().equals(Guid.Empty)) {
            alertPowerManagementStatusFailed(actionResult.getMessage());
        }
    }

    /**
     * Alerts if power management status failed.
     *
     * @param reason
     *            The reason.
     */
    protected void alertPowerManagementStatusFailed(String reason) {
        AuditLogable alert = new AuditLogableImpl();
        alert.setVdsId(getParameters().getTargetVdsID());
        alert.setVdsName(getTargetVds().getName());
        alert.addCustomValue("Reason", reason);
        auditLogDirector.log(alert, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
    }

    protected FenceStatusReturn fenceNode(FenceActionType fenceAction) {
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
    protected Status getReturnStatus() {
        Status status = new Status();
        FenceOperationResult result = (FenceOperationResult) getReturnValue();
        if (result == null) {
            // unexpected error happened
            status.code = 1;
            status.message = "";
        } else {
            // status result from action result
            status.code = result.getStatus() == FenceOperationResult.Status.ERROR ? 1 : 0;
            status.message = result.getMessage();
        }
        return status;
    }
}
