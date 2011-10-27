package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class CheckDBConnectionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public CheckDBConnectionQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        log.debug("Calling DB test...");
        getQueryReturnValue().setReturnValue(
                    DbFacade.getInstance().CheckDBConnection());
        log.debug("DB test ended.");
    }

    private static LogCompat log = LogFactoryCompat.getLog(CheckDBConnectionQuery.class);
}
