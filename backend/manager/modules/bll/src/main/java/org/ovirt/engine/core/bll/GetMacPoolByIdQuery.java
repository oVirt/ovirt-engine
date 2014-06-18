package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetMacPoolByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetMacPoolByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final MacPool macPool = getDbFacade().getMacPoolDao().get(getParameters().getId());

        getQueryReturnValue().setReturnValue(macPool);
    }
}
