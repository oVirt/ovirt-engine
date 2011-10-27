package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetRoleByNameQuery<P extends MultilevelAdministrationByRoleNameParameters> extends QueriesCommandBase<P> {
    public GetRoleByNameQuery(P parameters)

    {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getRoleDAO()
                .getByName(getParameters().getRoleName()));
    }
}
