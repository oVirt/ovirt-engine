package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetVdsGroupByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVdsGroupDao().getByName(
                getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
