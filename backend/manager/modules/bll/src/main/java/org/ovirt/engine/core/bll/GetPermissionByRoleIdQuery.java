package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetPermissionByRoleIdQuery<P extends MultilevelAdministrationByRoleIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionByRoleIdQuery(P parameters)

    {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getPermissionDAO().getAllForRole(getParameters().getRoleId()));
    }
}
