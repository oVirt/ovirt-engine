package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageServerConnectionExtensionsByHostIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetStorageServerConnectionExtensionsByHostIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStorageServerConnectionExtensionDao().getByHostId(getParameters().getId()));
    }
}
