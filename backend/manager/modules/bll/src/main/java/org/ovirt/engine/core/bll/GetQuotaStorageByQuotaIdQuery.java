package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetQuotaStorageByQuotaIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaStorageByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getQuotaDao().getQuotaStorageByQuotaGuidWithGeneralDefault(getParameters().getId()));
    }
}
