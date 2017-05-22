package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllPoolVmsQuery<P extends IdQueryParameters> extends GetAllVmsQueryBase<P> {
    public GetAllPoolVmsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected List<VM> getVMs() {
        return vmDao.getAllForVmPool(getParameters().getId());
    }
}
