package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotaVdsGroupByQuotaIdQueryParameters;

public class GetQuotaVdsGroupByQuotaIdQuery<P extends GetQuotaVdsGroupByQuotaIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaVdsGroupByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getQuotaVdsGroupByQuotaGuidWithGeneralDefault(getParameters().getQuotaId()));
    }
}
