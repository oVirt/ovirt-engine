package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmPoolByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmPoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVmPoolDao()
                        .get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
