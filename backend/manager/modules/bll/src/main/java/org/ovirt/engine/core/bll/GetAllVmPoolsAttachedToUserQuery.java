package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllVmPoolsAttachedToUserQuery<P extends GetAllVmPoolsAttachedToUserParameters>
        extends QueriesCommandBase<P> {
    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<vm_pools> returnValue = DbFacade.getInstance().getVmPoolDAO()
                .getAllForUser(getParameters().getUserId());
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
