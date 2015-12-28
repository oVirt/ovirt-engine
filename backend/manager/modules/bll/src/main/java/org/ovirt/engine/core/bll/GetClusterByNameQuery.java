package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetClusterByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetClusterByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getClusterDao().getByName(
                getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
