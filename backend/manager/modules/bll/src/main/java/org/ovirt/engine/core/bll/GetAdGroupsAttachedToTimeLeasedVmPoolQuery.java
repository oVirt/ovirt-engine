package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetAdGroupsAttachedToTimeLeasedVmPoolQuery<P extends GetAdGroupsAttachedToTimeLeasedVmPoolParameters>
        extends QueriesCommandBase<P> {
    public GetAdGroupsAttachedToTimeLeasedVmPoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getAdGroupDAO().getAllTimeLeasedForPool(getParameters().getId()));
    }
}
