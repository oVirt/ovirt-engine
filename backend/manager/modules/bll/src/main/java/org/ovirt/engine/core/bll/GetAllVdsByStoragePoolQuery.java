package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/** A query to return all the hosts in a given data center. */
public class GetAllVdsByStoragePoolQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllVdsByStoragePoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVdsDao().getAllForStoragePool
                        (getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
