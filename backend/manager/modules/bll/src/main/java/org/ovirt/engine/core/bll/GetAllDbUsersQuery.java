package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllDbUsersQuery<P extends VdcQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetAllDbUsersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getDbUserDao()
                        .getAll(getUserID(), getParameters().isFiltered()));
    }
}
