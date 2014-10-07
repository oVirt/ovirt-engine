package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworkAttachmentByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkAttachmentByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // TODO populate reported configuration
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkAttachmentDao().get(getParameters().getId()));
    }
}
