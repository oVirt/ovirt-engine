package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageServerConnectionExtensionByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<IdQueryParameters> {
    public GetStorageServerConnectionExtensionByIdQuery(P parameters) {
        super(parameters);
    }

    public GetStorageServerConnectionExtensionByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStorageServerConnectionExtensionDao().get(getParameters().getId()));
    }
}
