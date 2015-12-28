package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetQuotaClusterByQuotaIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaClusterByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getQuotaClusterByQuotaGuidWithGeneralDefault(getParameters().getId()));
    }
}
