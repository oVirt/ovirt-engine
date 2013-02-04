package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;

public class GetAllVmPoolsAttachedToUserQuery<P extends GetAllVmPoolsAttachedToUserParameters> extends GetDataByUserIDQueriesBase<P> {
    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<VmPool> getPrivilegedQueryReturnValue() {
        List<VmPool> returnValue = getDbFacade().getVmPoolDao().getAllForUser(getParameters().getUserId());
        return returnValue;
    }
}
