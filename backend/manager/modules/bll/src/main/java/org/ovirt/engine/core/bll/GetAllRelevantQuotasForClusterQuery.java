package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllRelevantQuotasForClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForClusterQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getAllRelevantQuotasForCluster(getParameters().getId(),
                        getEngineSessionSeqId(),
                        getParameters().isFiltered()));
    }
}
