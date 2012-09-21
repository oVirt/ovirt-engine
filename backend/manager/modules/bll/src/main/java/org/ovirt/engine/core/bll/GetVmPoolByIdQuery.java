package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmPoolByIdParameters;

public class GetVmPoolByIdQuery<P extends GetVmPoolByIdParameters> extends QueriesCommandBase<P> {
    public GetVmPoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVmPoolDao()
                        .get(getParameters().getPoolId(), getUserID(), getParameters().isFiltered()));
    }
}
