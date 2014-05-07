package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;

public class GetVdsFenceStatusQuery<P extends VdsIdParametersBase> extends FenceQueryBase<P> {

    public GetVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    public GetVdsFenceStatusQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = DbFacade.getInstance().getVdsDao().get(getParameters().getVdsId());
        FenceExecutor executor = new FenceExecutor(vds);
        VDSFenceReturnValue result = executor.checkStatus();
        if (result.getSucceeded()) {
            // Remove all alerts including NOT CONFIG alert
            AlertDirector.RemoveAllVdsAlerts(getParameters().getVdsId(), true);
            getQueryReturnValue().setReturnValue(result);
        } else {
            handleError(result);
        }
    }

    private void handleError(VDSFenceReturnValue returnValue) {
        String msg = returnValue.getExceptionString();
        alertPowerManagementStatusFailed(msg);
        getQueryReturnValue().setReturnValue(new FenceStatusReturnValue("unknown", msg));
    }
}
