package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.JobDao;

public class GetAllJobsQuery <P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private JobDao jobDao;

    public GetAllJobsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(jobDao.getAll());
    }
}
