package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmPoolDao;

public class GetVmPoolByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmPoolDao vmPoolDao;

    public GetVmPoolByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vmPoolDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
