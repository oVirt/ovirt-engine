package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.*;

public class GetVdsFenceStatusQuery<P extends VdsIdParametersBase> extends FencingQueryBase<P> {

    public GetVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String msg = "";
        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());
        setVdsId(vds.getvds_id());
        setVdsName(vds.getvds_name());
        FencingExecutor executor = new FencingExecutor(vds, FenceActionType.Status);
        VDSReturnValue returnValue = null;
        if (executor.FindVdsToFence()) {
            returnValue = executor.Fence();
            if (returnValue.getReturnValue() != null) {
                if (returnValue.getSucceeded()) {
                    // Remove all alerts including NOT CONFIG alert
                    AlertDirector.RemoveAllVdsAlerts(getVdsId(), true);
                    getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
                } else {
                    msg = ((FenceStatusReturnValue) returnValue.getReturnValue()).getMessage();
                    getQueryReturnValue().setReturnValue(new FenceStatusReturnValue("unknown", msg));
                    AlertPowerManagementStatusFailed(msg);
                }
            }
        } else {
            msg = String.format(
                    "Failed to run Power Management command on Host %1$s, no running proxy Host was found.",
                    vds.getvds_name());
            getQueryReturnValue().setReturnValue(new FenceStatusReturnValue("unknown", msg));
            AlertPowerManagementStatusFailed(AuditLogDirector.GetMessage(AuditLogType.VDS_ALERT_FENCING_NO_PROXY_HOST));
        }

    }
}
