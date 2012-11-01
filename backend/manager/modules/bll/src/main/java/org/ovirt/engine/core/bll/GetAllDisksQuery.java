package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllDisksQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getDiskDao().
                getAll(getUserID(), getParameters().isFiltered()));
    }
}
