package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbConnectionUtil;

public class CheckDBConnectionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private DbConnectionUtil dbConnectionUtil;

    public CheckDBConnectionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        log.debug("Start checking connection to db");
        getQueryReturnValue().setReturnValue(dbConnectionUtil.checkDBConnection());
        log.debug("Completed checking connection to db");
    }
}
