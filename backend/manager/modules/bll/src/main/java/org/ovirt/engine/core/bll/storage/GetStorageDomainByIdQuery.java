package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;

public class GetStorageDomainByIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageDomainByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getStorageDomainDao().get(getParameters().getStorageDomainId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
