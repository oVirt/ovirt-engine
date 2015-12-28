package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetClusterByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetClusterByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade()
                        .getClusterDao()
                        .get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
