package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;

public class GetPermissionsToConsumeQuotaByQuotaIdQuery<P extends GetEntitiesRelatedToQuotaIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsToConsumeQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getPermissionDAO()
                .getConsumedPermissionsForQuotaId(getParameters().getQuotaId()));
    }
}
