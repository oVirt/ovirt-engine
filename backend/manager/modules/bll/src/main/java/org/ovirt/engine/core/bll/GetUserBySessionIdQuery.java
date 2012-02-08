package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetUserBySessionIdQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetUserBySessionIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                SessionDataContainer.getInstance().getUser(getParameters().getSessionId()));
    }

}
