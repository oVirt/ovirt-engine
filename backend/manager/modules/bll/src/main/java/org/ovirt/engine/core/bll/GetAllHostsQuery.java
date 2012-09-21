package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllHostsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllHostsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVdsDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }
}
