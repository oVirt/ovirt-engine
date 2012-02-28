package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionsToConsumeQuotaByQuotaIdQuery<P extends GetEntitiesRelatedToQuotaIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsToConsumeQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getPermissionDAO()
                .getConsumedPermissionsForQuotaId(getParameters().getQuotaId()));
    }
}
