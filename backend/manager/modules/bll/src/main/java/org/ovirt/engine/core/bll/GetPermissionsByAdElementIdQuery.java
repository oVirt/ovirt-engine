package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;

public class GetPermissionsByAdElementIdQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getPermissionDao().getAllForAdElement
                        (getParameters().getAdElementId(),
                                getUserID(),
                                getParameters().isFiltered()));
    }
}
