package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.RegisterQueryParameters;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

public class RegisterQueryQuery<P extends RegisterQueryParameters> extends QueriesCommandBase<P> {
    public RegisterQueryQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (SessionDataContainer.getInstance().getUser() != null) {
            BackendCallBacksDirector.getInstance().RegisterQuery(getParameters());
        } else {
            VdcFault fault = new VdcFault();
            fault.setError(VdcBllErrors.SESSION_ERROR);
            fault.setMessage("Unkown session, please login again.");
            CallbackServer.Instance.SearchQueryException(getParameters().getQueryID(),
                    getParameters().getQueryType(), fault);
            BackendCallBacksDirector.getInstance().RegisterFaultQuery(getParameters().getQueryID(),
                    ThreadLocalParamsContainer.getHttpSessionId());
        }
    }

}
