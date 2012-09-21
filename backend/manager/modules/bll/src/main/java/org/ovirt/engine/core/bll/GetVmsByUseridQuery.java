package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmsByUseridParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmsByUseridQuery<P extends GetVmsByUseridParameters> extends
        QueriesCommandBase<P> {
    public GetVmsByUseridQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmDao()
                        .getAllForUser(getParameters().getUserId()));
    }
}
