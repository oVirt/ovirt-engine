package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmPoolByIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmPoolByIdQuery<P extends GetVmPoolByIdParameters> extends QueriesCommandBase<P> {
    public GetVmPoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmPoolDAO().get(getParameters().getPoolId()));
    }
}
