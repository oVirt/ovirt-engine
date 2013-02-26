package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;


public class GetAllVmPoolsAttachedToUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getVmPoolDao().getAllForUser(getUserID()));
    }
}
