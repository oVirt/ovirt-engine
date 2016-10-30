package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.DbGroupDao;

public class GetAllDbGroupsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private DbGroupDao dbGroupDao;

    public GetAllDbGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(dbGroupDao.getAll());
    }
}
