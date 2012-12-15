package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmGuestAgentInterfacesByVmIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    public GetVmGuestAgentInterfacesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVmGuestAgentInterfaceDao()
                .getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
