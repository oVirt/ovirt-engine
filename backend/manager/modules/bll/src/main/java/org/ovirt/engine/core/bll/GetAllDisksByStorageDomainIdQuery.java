package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;

public class GetAllDisksByStorageDomainIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllDisksByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getDiskImageDao().getImagesByStorageId(getParameters().getStorageDomainId()));
    }

}
