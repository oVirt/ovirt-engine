package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetPermissionsByAdElementIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getPermissionDao().getAllForAdElement
                        (getParameters().getId(),
                                getEngineSessionSeqId(),
                                getParameters().isFiltered()));
    }
}
