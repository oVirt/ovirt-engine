package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllVmPoolsAttachedToUserQuery<P extends GetAllVmPoolsAttachedToUserParameters> extends GetDataByUserIDQueriesBase<P> {
    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<vm_pools> getPrivilegedQueryReturnValue() {
        List<vm_pools> returnValue = DbFacade.getInstance().getVmPoolDAO()
                .getAllForUser(getParameters().getUserId());
        return returnValue;
    }
}
