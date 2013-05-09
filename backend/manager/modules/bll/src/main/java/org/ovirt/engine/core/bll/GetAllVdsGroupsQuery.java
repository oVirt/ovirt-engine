package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVdsGroupsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVdsGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getVdsGroupDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }
}
