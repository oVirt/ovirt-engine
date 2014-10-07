package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworkAttachmentsByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkAttachmentsByHostIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // TODO populate reported configuration
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkAttachmentDao()
                .getAllForHost(getParameters().getId()));
    }
}
