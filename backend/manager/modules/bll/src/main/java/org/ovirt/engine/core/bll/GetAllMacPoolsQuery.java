package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.MacPoolDao;

public class GetAllMacPoolsQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private MacPoolDao macPoolDao;

    public GetAllMacPoolsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(macPoolDao.getAll());
    }
}
