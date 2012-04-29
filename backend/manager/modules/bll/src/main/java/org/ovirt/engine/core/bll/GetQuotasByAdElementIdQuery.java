package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;

public class GetQuotasByAdElementIdQuery<P extends GetQuotasByAdElementIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotasByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDAO()
                .getQuotaByAdElementId(getParameters().getAdElementId(), getParameters().getStoragePoolId()));
    }
}
