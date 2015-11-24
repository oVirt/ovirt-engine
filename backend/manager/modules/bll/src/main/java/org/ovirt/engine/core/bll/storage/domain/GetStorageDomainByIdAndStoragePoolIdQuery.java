package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageDomainByIdAndStoragePoolIdQuery<P extends StorageDomainAndPoolQueryParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainByIdAndStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getStorageDomainDao().getForStoragePool(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId()));
    }
}
