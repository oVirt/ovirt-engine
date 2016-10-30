package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.DbUserDao;

public class GetAllDbUsersQuery<P extends VdcQueryParametersBase>
        extends QueriesCommandBase<P> {
    @Inject
    private DbUserDao dbUserDao;

    public GetAllDbUsersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(dbUserDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
