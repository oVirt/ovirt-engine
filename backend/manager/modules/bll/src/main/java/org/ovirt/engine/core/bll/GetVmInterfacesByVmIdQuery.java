package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmInterfacesByVmIdQuery<P extends GetVmByVmIdParameters> extends QueriesCommandBase<P> {
    public GetVmInterfacesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmNetworkInterfaceDAO()
                        .getAllForVm(getParameters().getId()));
    }
}
