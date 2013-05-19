package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetQuotaVdsGroupByQuotaIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaVdsGroupByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getQuotaVdsGroupByQuotaGuidWithGeneralDefault(getParameters().getId()));
    }
}
