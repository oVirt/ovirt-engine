package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetEngineSessionIdForSsoTokenQueryParameters;

public class GetEngineSessionIdForSsoTokenQuery<P extends GetEngineSessionIdForSsoTokenQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public GetEngineSessionIdForSsoTokenQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(sessionDataContainer.getSessionIdBySsoAccessToken(getParameters().getToken()));
    }
}
