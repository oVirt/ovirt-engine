package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworkByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDao().get(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
