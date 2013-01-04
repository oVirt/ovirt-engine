package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllNetworksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllNetworksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() == null
                || getParameters().getId().equals(Guid.Empty)) {
            getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDao().getAll(getUserID(), getParameters().isFiltered()));
        } else {
            getQueryReturnValue().setReturnValue(
                    getDbFacade().getNetworkDao().getAllForDataCenter(getParameters().getId(),
                            getUserID(),
                            getParameters().isFiltered()));
        }
    }
}
