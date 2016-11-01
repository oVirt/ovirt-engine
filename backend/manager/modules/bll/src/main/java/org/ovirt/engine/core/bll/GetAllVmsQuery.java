package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmsQuery<P extends VdcQueryParametersBase> extends GetAllVmsQueryBase<P> {
    public GetAllVmsQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected List<VM> getVMs() {
        return vmDao.getAll(getUserID(), getParameters().isFiltered());
    }
}
