package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getVmDAO().getAll(getUserID(), getParameters().isFiltered()));
    }
}
