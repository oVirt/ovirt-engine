package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;

public class GetRoleActionGroupsByRoleIdQuery<P extends MultilevelAdministrationByRoleIdParameters>
        extends QueriesCommandBase<P> {
    public GetRoleActionGroupsByRoleIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getActionGroupDao()
                        .getAllForRole(getParameters().getRoleId()));
    }
}
