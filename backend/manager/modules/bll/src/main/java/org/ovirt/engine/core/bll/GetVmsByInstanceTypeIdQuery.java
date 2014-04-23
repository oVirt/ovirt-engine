package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * A query to retrieve all the VMs connected to a given instance type.
 */
public class GetVmsByInstanceTypeIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmsByInstanceTypeIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getVmDao()
                .getVmsListByInstanceType(getParameters().getId()));
    }
}
