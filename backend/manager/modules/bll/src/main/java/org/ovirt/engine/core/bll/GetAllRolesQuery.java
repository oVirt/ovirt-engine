package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.RoleDao;

public class GetAllRolesQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private RoleDao roleDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    public GetAllRolesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected boolean isAdminUser() {
        return multiLevelAdministrationHandler.isAdminUser(getUser());
    }

    @Override
    protected void executeQueryCommand() {
        if (!isAdminUser()) {
            setReturnValue(roleDao.getAllNonAdminRoles());
        } else {
            setReturnValue(roleDao.getAll());
        }
    }
}
