package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionByRoleIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public GetPermissionByRoleIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(permissionDao.getAllForRole(getParameters().getId()));
    }
}
