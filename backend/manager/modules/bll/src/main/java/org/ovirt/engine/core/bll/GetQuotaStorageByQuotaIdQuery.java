package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotaStorageByQuotaIdQueryParameters;

public class GetQuotaStorageByQuotaIdQuery<P extends GetQuotaStorageByQuotaIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaStorageByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getQuotaDAO().getQuotaStorageByQuotaGuidWithGeneralDefault(getParameters().getQuotaId()));
    }
}
