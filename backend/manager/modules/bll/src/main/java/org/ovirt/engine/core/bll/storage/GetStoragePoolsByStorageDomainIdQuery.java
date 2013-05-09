package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStoragePoolsByStorageDomainIdQuery<P extends StorageDomainQueryParametersBase>
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
                                getParameters().getStorageDomainId()));
    }
}
