package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.DbGroupDao;

public class GetAllDbGroupsQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private DbGroupDao dbGroupDao;

    public GetAllDbGroupsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(dbGroupDao.getAll());
    }
}
