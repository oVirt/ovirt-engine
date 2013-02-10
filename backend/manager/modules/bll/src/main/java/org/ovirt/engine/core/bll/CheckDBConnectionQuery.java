package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class CheckDBConnectionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public CheckDBConnectionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        log.debug("Start checking connection to db");
        getQueryReturnValue().setReturnValue(getDbFacade().checkDBConnection());
        log.debug("Completed checking connection to db");
    }
}
