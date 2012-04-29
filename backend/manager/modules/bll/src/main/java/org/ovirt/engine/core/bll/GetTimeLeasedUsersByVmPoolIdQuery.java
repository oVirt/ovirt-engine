package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTimeLeasedUsersByVmPoolIdParameters;

public class GetTimeLeasedUsersByVmPoolIdQuery<P extends GetTimeLeasedUsersByVmPoolIdParameters>
        extends QueriesCommandBase<P> {
    public GetTimeLeasedUsersByVmPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getDbUserDAO().getAllTimeLeasedUsersForVm(getParameters().getId()));
    }
}
