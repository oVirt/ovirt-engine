package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class IsPasswordDelegationPossibleQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public IsPasswordDelegationPossibleQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String password = null;
        try {
            password = SsoUtils.getPassword(sessionDataContainer.getSsoAccessToken(getParameters().getSessionId()));
        } catch(Exception ex) {
            log.error("Unable to execute IsPasswordDelegationPossibleQuery with message {}", ex.getMessage());
            log.debug("Exception", ex);
        }
        getQueryReturnValue().setReturnValue(password != null);
        getQueryReturnValue().setSucceeded(true);
    }
}
