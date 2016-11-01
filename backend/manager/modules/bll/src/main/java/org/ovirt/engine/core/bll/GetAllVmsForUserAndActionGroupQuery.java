package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;

public class GetAllVmsForUserAndActionGroupQuery<P extends GetEntitiesWithPermittedActionParameters> extends GetAllVmsQueryBase<P> {
    public GetAllVmsForUserAndActionGroupQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsForUserAndActionGroupQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected List<VM> getVMs() {
        return vmDao.getAllForUserAndActionGroup(getUserID(), getParameters().getActionGroup());
    }
}
