package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsGroupByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade()
                        .getVdsGroupDao()
                        .get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
