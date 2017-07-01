package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetDbUserBySessionQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    public GetDbUserBySessionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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

