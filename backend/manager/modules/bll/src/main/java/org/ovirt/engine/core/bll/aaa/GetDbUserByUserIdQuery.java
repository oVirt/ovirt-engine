package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DbUserDao;

public class GetDbUserByUserIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DbUserDao dbUserDao;

    public GetDbUserByUserIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        DbUser currentUser = getUser();
        if (!currentUser.isAdmin()) {
            // unauthorized access
            if (!currentUser.getId().equals(getParameters().getId())) {
                getQueryReturnValue().setReturnValue(null);
            } else {
                // A non-admin user can get only its own data
                getQueryReturnValue().setReturnValue(dbUserDao.get(currentUser.getId(), false));
            }
        } else {
            getQueryReturnValue().setReturnValue(dbUserDao.get(getParameters().getId(), getParameters().isFiltered()));
        }
    }
}
