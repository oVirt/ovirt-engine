package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionByIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getPermissionDao().get(getParameters().getId()));
    }
}
