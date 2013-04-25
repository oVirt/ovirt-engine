package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAdGroupByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAdGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getAdGroupDao().get(getParameters().getId()));
    }
}
