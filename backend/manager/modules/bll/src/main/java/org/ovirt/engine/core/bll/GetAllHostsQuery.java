package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VdsDao;

public class GetAllHostsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetAllHostsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vdsDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
