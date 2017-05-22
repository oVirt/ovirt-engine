package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.pm.HostFenceActionExecutor;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetVdsFenceStatusQuery<P extends IdQueryParameters> extends FenceQueryBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetVdsFenceStatusQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = vdsDao.get(getParameters().getId());
        HostFenceActionExecutor executor = new HostFenceActionExecutor(vds);
        getQueryReturnValue().setReturnValue(executor.fence(FenceActionType.STATUS));
    }
}
