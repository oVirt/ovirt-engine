package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllClustersHavingHostsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllClustersHavingHostsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getVdsGroupDao().getClustersHavingHosts());
    }
}
