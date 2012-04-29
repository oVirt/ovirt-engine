package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAdGroupByIdParameters;

public class GetAdGroupByIdQuery<P extends GetAdGroupByIdParameters> extends QueriesCommandBase<P> {
    public GetAdGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getAdGroupDAO().get(getParameters().getId()));
    }
}
