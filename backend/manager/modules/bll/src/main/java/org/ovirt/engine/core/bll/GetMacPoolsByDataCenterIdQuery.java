package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetMacPoolsByDataCenterIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetMacPoolsByDataCenterIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        MacPool macPool = getDbFacade().getMacPoolDao().getByDataCenterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(macPool);
    }
}
