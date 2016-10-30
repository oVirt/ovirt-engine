package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DbUserDao;

public class GetDbUserByUserIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DbUserDao dbUserDao;

    public GetDbUserByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(dbUserDao.get(getParameters().getId(), getParameters().isFiltered()));
    }
}
