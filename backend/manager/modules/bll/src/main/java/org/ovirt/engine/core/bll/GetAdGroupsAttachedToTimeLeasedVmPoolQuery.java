package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAdGroupsAttachedToTimeLeasedVmPoolParameters;

public class GetAdGroupsAttachedToTimeLeasedVmPoolQuery<P extends GetAdGroupsAttachedToTimeLeasedVmPoolParameters>
        extends QueriesCommandBase<P> {
    public GetAdGroupsAttachedToTimeLeasedVmPoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAdGroupDao().getAllTimeLeasedForPool(getParameters().getId()));
    }
}
