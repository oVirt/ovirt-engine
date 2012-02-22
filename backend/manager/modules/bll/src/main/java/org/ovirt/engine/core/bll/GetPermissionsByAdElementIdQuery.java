package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionsByAdElementIdQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance()
                        .getPermissionDAO()
                        .getAllForAdElement(getParameters().getAdElementId(), getUserID(), getParameters().isFiltered()));
    }
}
