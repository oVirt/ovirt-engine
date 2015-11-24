package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageDomainsByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().
                getStorageDomainDao().getAllForStoragePool(getParameters().getId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
