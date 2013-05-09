package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetRoleByIdQuery<P extends MultilevelAdministrationByRoleIdParameters> extends QueriesCommandBase<P> {
    public GetRoleByIdQuery(P parameters)

    {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getRoleDao().get(getParameters().getRoleId()));
    }
}
