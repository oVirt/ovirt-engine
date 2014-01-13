package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class IsClusterEmptyQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public IsClusterEmptyQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Boolean isEmpty = getDbFacade().getVdsGroupDao().getIsEmpty(getParameters().getId());

        getQueryReturnValue().setReturnValue(isEmpty);
    }
}
