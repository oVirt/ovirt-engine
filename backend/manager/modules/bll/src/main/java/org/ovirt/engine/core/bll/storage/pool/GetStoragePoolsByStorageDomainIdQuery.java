package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStoragePoolsByStorageDomainIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetStoragePoolsByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance()
                        .getStoragePoolDao()
                        .getAllForStorageDomain(
                                getParameters().getId()));
    }
}
