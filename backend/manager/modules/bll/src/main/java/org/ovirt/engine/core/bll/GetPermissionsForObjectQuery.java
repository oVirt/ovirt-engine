package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionsForObjectQuery<P extends GetPermissionsForObjectParameters> extends QueriesCommandBase<P> {
    @Inject
    private PermissionDao dao;

    public GetPermissionsForObjectQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid objectId = getParameters().getObjectId();
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
