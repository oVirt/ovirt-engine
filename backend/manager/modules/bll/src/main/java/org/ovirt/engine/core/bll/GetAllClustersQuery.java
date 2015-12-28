package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllClustersQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getClusterDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }
}
