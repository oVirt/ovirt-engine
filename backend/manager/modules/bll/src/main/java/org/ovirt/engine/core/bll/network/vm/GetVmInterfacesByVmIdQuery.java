package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmInterfacesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmInterfacesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVmNetworkInterfaceDao()
                        .getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
