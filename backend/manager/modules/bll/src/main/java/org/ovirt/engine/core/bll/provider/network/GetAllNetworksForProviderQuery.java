package org.ovirt.engine.core.bll.provider.network;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllNetworksForProviderQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllNetworksForProviderQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getNetworkViewDao().getAllForProvider(getParameters().getId()));
    }
}
