package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.VmIconDao;

public class GetAllVmIconsQuery extends QueriesCommandBase<QueryParametersBase> {

    @Inject
    private VmIconDao vmIconDao;

    public GetAllVmIconsQuery(QueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
