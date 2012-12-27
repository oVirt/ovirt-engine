package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;

public class GetVmInterfacesByVmIdQuery<P extends GetVmByVmIdParameters> extends QueriesCommandBase<P> {
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
