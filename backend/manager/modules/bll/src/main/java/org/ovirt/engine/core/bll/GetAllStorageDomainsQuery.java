package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllStorageDomainsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllStorageDomainsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStorageDomainDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }
}
