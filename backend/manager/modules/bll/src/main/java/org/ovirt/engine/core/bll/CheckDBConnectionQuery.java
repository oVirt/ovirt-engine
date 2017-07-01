package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbConnectionUtil;

public class CheckDBConnectionQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private DbConnectionUtil dbConnectionUtil;

    public CheckDBConnectionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        log.debug("Start checking connection to db");
        getQueryReturnValue().setReturnValue(dbConnectionUtil.checkDBConnection());
        log.debug("Completed checking connection to db");
    }
}
