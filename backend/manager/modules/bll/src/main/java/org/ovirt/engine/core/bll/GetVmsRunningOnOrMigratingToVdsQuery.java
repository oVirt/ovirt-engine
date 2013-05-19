package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmsRunningOnOrMigratingToVdsQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmsRunningOnOrMigratingToVdsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVmDao().getAllRunningOnOrMigratingToVds(getParameters().getId()));
    }

}
