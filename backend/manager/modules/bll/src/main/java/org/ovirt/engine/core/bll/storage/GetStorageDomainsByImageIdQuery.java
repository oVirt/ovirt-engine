package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetImageByImageIdParameters;

public class GetStorageDomainsByImageIdQuery<P extends GetImageByImageIdParameters>
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
