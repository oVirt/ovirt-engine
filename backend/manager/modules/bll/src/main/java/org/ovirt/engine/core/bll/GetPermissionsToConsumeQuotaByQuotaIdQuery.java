package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetPermissionsToConsumeQuotaByQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsToConsumeQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getPermissionDao()
                .getConsumedPermissionsForQuotaId(getParameters().getId()));
    }
}
