package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageDomainsByImageIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainsByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStorageDomainDao()
                .getAllStorageDomainsByImageId(getParameters().getId()));
    }
}
