package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetRoleActionGroupsByRoleIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetRoleActionGroupsByRoleIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getActionGroupDao()
                        .getAllForRole(getParameters().getId()));
    }
}
