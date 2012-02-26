package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionsToConsumeQuotaByQuotaId<P extends GetPermissionsForObjectParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionsToConsumeQuotaByQuotaId(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getPermissionDAO()
                .getConsumedPermissionsForQuotaId(getParameters().getObjectId()));
    }
}
