package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDAO;

public class GetPermissionsForObjectQuery<P extends GetPermissionsForObjectParameters> extends QueriesCommandBase<P> {

    public GetPermissionsForObjectQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid objectId = getParameters().getObjectId();
        PermissionDAO dao = getDbFacade().getPermissionDao();
        List<Permissions> perms;
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
