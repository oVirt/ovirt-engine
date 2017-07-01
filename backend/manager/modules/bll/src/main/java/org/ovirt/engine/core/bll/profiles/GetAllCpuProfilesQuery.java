package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class GetAllCpuProfilesQuery extends QueriesCommandBase<QueryParametersBase> {
    @Inject
    private CpuProfileDao cpuProfileDao;

    public GetAllCpuProfilesQuery(QueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuProfileDao.getAll());
    }

}
