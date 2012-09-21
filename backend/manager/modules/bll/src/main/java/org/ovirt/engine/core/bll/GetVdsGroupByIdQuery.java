package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;

public class GetVdsGroupByIdQuery<P extends GetVdsGroupByIdParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade()
                        .getVdsGroupDao()
                        .get(getParameters().getVdsId(), getUserID(), getParameters().isFiltered()));
    }
}
