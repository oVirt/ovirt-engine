package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmPoolsMapByVmPoolIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmPoolsMapByVmPoolIdQuery<P extends GetVmPoolsMapByVmPoolIdParameters> extends QueriesCommandBase<P> {
    public GetVmPoolsMapByVmPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                                                     .getVmPoolDAO().getVmPoolsMapByVmPoolId(getParameters().getPoolId()));
    }
}
