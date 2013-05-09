package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionByRoleIdQuery<P extends MultilevelAdministrationByRoleIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionByRoleIdQuery(P parameters)

    {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getPermissionDao().getAllForRole(getParameters().getRoleId()));
    }
}
