package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionsToConsumeQuotaByQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public GetPermissionsToConsumeQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(permissionDao.getConsumedPermissionsForQuotaId(getParameters().getId()));
    }
}
