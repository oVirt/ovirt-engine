package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetSystemPermissionsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private PermissionDao permissionDao;

    public GetSystemPermissionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Permission> perms = permissionDao.getAllForEntity(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        getQueryReturnValue().setReturnValue(perms);
    }

}
