package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public class GetVdsFenceStatusQuery<P extends VdsIdParametersBase> extends FenceQueryBase<P> {

    public GetVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    public GetVdsFenceStatusQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String msg = "";
        VDS vds = DbFacade.getInstance().getVdsDao().get(getParameters().getVdsId());
        setVdsId(vds.getId());
        setVdsName(vds.getName());
        FenceExecutor executor = new FenceExecutor(vds, FenceActionType.Status);
        VDSReturnValue returnValue = null;
        if (executor.findProxyHost()) {
            returnValue = executor.fence(FenceAgentOrder.Primary);
            if (returnValue.getReturnValue() != null) {
                if (returnValue.getSucceeded()) {
                    boolean succeeded = true;
                    // check if we have secondary agent settings
                    if (vds.getPmSecondaryIp() != null && !vds.getPmSecondaryIp().isEmpty()) {
                        returnValue = executor.fence(FenceAgentOrder.Secondary);
                        succeeded = returnValue.getSucceeded();
                    }
                    if (succeeded) {
                        // Remove all alerts including NOT CONFIG alert
                        AlertDirector.RemoveAllVdsAlerts(getVdsId(), true);
                        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
                    }
                    else {
                        handleError(returnValue);
                    }
                } else {
                    handleError(returnValue);
                }
            }
        } else {
            msg = String.format(
                    "Failed to run Power Management command on Host %1$s, no running proxy Host was found.",
                    vds.getName());
            getQueryReturnValue().setReturnValue(new FenceStatusReturnValue("unknown", msg));
            alertPowerManagementStatusFailed(AuditLogDirector.getMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST));
        }
    }

    private void handleError(VDSReturnValue returnValue) {
        String msg = ((FenceStatusReturnValue) returnValue.getReturnValue()).getMessage();
        getQueryReturnValue().setReturnValue(new FenceStatusReturnValue("unknown", msg));
        alertPowerManagementStatusFailed(msg);

    }
}
