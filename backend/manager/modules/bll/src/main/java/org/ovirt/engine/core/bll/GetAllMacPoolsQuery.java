package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.MacPoolDao;

public class GetAllMacPoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private MacPoolDao macPoolDao;

    public GetAllMacPoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(macPoolDao.getAll());
    }
}
