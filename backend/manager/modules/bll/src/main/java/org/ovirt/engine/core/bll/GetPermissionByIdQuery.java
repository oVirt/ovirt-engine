package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionByIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public GetPermissionByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(permissionDao.get(getParameters().getId()));
    }
}
