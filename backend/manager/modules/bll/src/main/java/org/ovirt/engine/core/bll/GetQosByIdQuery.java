package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetQosByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetQosByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getQosBaseDao().get(getParameters().getId()));
    }

}
