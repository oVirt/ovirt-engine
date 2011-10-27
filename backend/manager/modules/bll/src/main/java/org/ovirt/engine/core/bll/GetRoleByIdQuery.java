package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetRoleByIdQuery<P extends MultilevelAdministrationByRoleIdParameters> extends QueriesCommandBase<P> {
    public GetRoleByIdQuery(P parameters)

    {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getRoleDAO().get(getParameters().getRoleId()));
    }
}
