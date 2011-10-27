package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStorageDomainByIdAndStoragePoolIdQuery<P extends StorageDomainAndPoolQueryParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainByIdAndStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId()));
    }
}
