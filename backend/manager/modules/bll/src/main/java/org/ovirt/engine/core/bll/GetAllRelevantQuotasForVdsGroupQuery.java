package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllRelevantQuotasForVdsGroupQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForVdsGroupQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getAllRelevantQuotasForVdsGroup(getParameters().getId(),
                        getEngineSessionSeqId(),
                        getParameters().isFiltered()));
    }
}
