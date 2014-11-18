package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllRelevantQuotasForStorageQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForStorageQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getAllRelevantQuotasForStorage(getParameters().getId(),
                        getEngineSessionSeqId(),
                        getParameters().isFiltered()));
    }
}
