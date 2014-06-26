package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;


public class GetAllVmPoolsAttachedToUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllVmPoolsAttachedToUserQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getVmPoolDao().getAllForUser(getUserID()));
    }
}
