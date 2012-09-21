package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;

public class GetAllVmPoolsAttachedToUserQuery<P extends GetAllVmPoolsAttachedToUserParameters> extends GetDataByUserIDQueriesBase<P> {
    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<vm_pools> getPrivilegedQueryReturnValue() {
        List<vm_pools> returnValue = getDbFacade().getVmPoolDao().getAllForUser(getParameters().getUserId());
        return returnValue;
    }
}
