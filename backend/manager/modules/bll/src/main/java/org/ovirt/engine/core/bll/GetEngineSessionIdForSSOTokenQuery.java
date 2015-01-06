package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.queries.GetEngineSessionIdForSSOTokenQueryParameters;

public class GetEngineSessionIdForSSOTokenQuery<P extends GetEngineSessionIdForSSOTokenQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public GetEngineSessionIdForSSOTokenQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(sessionDataContainer.getSessionIdBySSOAccessToken(getParameters().getToken()));
    }
}
