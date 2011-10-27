package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.GetUsersByVmidParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetUsersByVmidQuery<P extends GetUsersByVmidParameters> extends
        QueriesCommandBase<P> {
    public GetUsersByVmidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<DbUser> users = DbFacade.getInstance().getDbUserDAO()
                .getAllForVm(getParameters().getVmId());

        getQueryReturnValue().setReturnValue(users);
    }
}
