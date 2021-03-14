package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.DbUserDao;



public class GetAllDbUsersQuery<P extends QueryParametersBase>
        extends QueriesCommandBase<P> {
    @Inject
    private DbUserDao dbUserDao;

    public GetAllDbUsersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        DbUser currentUser = getUser();
        // A non-admin trying to get other user data will get its own data
        if (!currentUser.isAdmin()) {
            ArrayList<DbUser> users = new ArrayList<>();
            users.add(currentUser);
            getQueryReturnValue().setReturnValue(users);
        } else {
            getQueryReturnValue().setReturnValue(dbUserDao.getAll(getUserID(), getParameters().isFiltered()));
        }
    }
}
