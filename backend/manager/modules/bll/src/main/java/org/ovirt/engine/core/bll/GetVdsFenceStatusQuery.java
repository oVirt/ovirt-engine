package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.pm.HostFenceActionExecutor;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVdsFenceStatusQuery<P extends IdQueryParameters> extends FenceQueryBase<P> {

    public GetVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    public GetVdsFenceStatusQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = DbFacade.getInstance().getVdsDao().get(getParameters().getId());
        HostFenceActionExecutor executor = new HostFenceActionExecutor(vds);
        getQueryReturnValue().setReturnValue(executor.fence(FenceActionType.STATUS));
    }
}
