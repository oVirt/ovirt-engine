package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmPoolsAttachedToAdGroupParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmPoolsAttachedToAdGroupQuery<P extends GetVmPoolsAttachedToAdGroupParameters>
        extends QueriesCommandBase<P> {
    public GetVmPoolsAttachedToAdGroupQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmPoolDAO()
                        .getAllForAdGroup(getParameters().getId()));
    }
}
