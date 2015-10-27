package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;

public class GetAllRolesQuery<P extends MultilevelAdministrationsQueriesParameters> extends QueriesCommandBase<P> {
    public GetAllRolesQuery(P parameters) {
        super(parameters);
    }

    protected boolean isAdminUser() {
        return MultiLevelAdministrationHandler.isAdminUser(getUser());
    }

    @Override
    protected void executeQueryCommand() {
        if (!isAdminUser()) {
            setReturnValue(getDbFacade().getRoleDao().getAllNonAdminRoles());
        } else {
            setReturnValue(getDbFacade().getRoleDao().getAll());
        }
    }
}
