package org.ovirt.engine.core.bll;


import org.ovirt.engine.core.common.queries.GetTimeLeasedUsersByVmPoolIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTimeLeasedUsersByVmPoolIdQuery<P extends GetTimeLeasedUsersByVmPoolIdParameters>
        extends QueriesCommandBase<P> {
    public GetTimeLeasedUsersByVmPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getDbUserDAO().getAllTimeLeasedUsersForVm(getParameters().getId()));
    }
}
