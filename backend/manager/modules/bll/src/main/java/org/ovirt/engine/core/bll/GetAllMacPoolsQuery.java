package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllMacPoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllMacPoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getMacPoolDao().getAll());
    }
}
