package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDbUserBySessionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDbUserBySessionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        boolean succeeded = true;
        if (SessionDataContainer.getInstance().isSessionExists(getParameters().getSessionId())) {
            setReturnValue(SessionDataContainer.getInstance().getUser(getParameters().getSessionId(), false));
        } else {
            succeeded = false;
        }
        getQueryReturnValue().setSucceeded(succeeded);

    }
}

