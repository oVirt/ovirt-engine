package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetIscsiBondByIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetIscsiBondByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getIscsiBondDao().get(getParameters().getId()));
    }
}
