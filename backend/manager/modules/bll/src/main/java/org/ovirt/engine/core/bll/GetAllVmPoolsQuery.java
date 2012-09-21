package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllVmPoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmPoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getVmPoolDao().getAll());
    }
}
