package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByImageIdParameters;

public class GetStorageDomainsByImageIdQuery<P extends GetStorageDomainsByImageIdParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainsByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStorageDomainDao()
                .getAllStorageDomainsByImageId(getParameters().getImageId()));
    }
}
