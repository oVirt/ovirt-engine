package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmsRunningOnVDSCountParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmsRunningOnVDSCountQuery<P extends GetVmsRunningOnVDSCountParameters> extends QueriesCommandBase<P> {
    public GetVmsRunningOnVDSCountQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsDynamicDAO().get(getParameters().getId()).getvm_count());
    }
}
