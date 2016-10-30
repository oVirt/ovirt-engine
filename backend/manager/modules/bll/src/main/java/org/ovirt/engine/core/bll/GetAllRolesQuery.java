package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.RoleDao;

public class GetAllRolesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private RoleDao roleDao;

    public GetAllRolesQuery(P parameters) {
        super(parameters);
    }

    protected boolean isAdminUser() {
        return MultiLevelAdministrationHandler.isAdminUser(getUser());
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
