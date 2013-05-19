package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsGroupsByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVdsGroupsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVdsGroupDao().getAllForStoragePool(
                        getParameters().getId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
