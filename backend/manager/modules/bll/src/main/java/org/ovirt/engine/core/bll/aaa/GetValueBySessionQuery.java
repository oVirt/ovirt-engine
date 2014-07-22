package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetValueBySessionQueryParameters;

public class GetValueBySessionQuery<P extends GetValueBySessionQueryParameters> extends QueriesCommandBase<P> {
    public GetValueBySessionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        boolean succeeded = true;
        if (SessionDataContainer.getInstance().isSessionExists(getParameters().getSessionId())) {
            setReturnValue(SessionDataContainer.getInstance().getData(getParameters().getSessionId(), getParameters().getKey(), false));
        } else {
            succeeded = false;
        }
        getQueryReturnValue().setSucceeded(succeeded);

    }
}

