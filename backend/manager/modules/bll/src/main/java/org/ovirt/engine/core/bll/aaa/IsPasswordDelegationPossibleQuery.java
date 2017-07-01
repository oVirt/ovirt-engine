package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class IsPasswordDelegationPossibleQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public IsPasswordDelegationPossibleQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String password = SsoUtils.getPassword(sessionDataContainer.getSsoAccessToken(getParameters().getSessionId()));
        getQueryReturnValue().setReturnValue(password != null);
        getQueryReturnValue().setSucceeded(true);
    }
}
