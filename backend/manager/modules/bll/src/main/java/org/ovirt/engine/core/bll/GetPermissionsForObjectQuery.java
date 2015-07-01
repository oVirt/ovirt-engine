package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionsForObjectQuery<P extends GetPermissionsForObjectParameters> extends QueriesCommandBase<P> {

    public GetPermissionsForObjectQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid objectId = getParameters().getObjectId();
        PermissionDao dao = getDbFacade().getPermissionDao();
        List<Permission> perms;
        if (getParameters().getDirectOnly()) {
            perms = dao.getAllForEntity(objectId, getEngineSessionSeqId(), getParameters().isFiltered(), getParameters().getAllUsersWithPermission());
        } else {
            perms = dao.getTreeForEntity(objectId,
                    getParameters().getVdcObjectType(),
                    getEngineSessionSeqId(),
                    getParameters().isFiltered());
        }
        getQueryReturnValue().setReturnValue(perms);
    }
}
