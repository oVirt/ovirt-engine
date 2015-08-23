package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDbUserBySessionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDbUserBySessionQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Override
    protected void executeQueryCommand() {
        boolean succeeded = true;
        if (sessionDataContainer.isSessionExists(getParameters().getSessionId())) {
            setReturnValue(sessionDataContainer.getUser(getParameters().getSessionId(), false));
        } else {
            succeeded = false;
        }
        getQueryReturnValue().setSucceeded(succeeded);

    }
}

