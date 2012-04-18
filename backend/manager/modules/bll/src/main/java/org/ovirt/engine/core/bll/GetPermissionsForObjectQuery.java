package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermissionsForObjectQuery<P extends GetPermissionsForObjectParameters> extends QueriesCommandBase<P> {

    public GetPermissionsForObjectQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid objectId = getParameters().getObjectId();
        List<permissions> perms;
        if (getParameters().getDirectOnly()) {
            perms =
                    DbFacade.getInstance()
                            .getPermissionDAO()
                            .getAllForEntity(objectId, getUserID(), getParameters().isFiltered());
        } else {
            perms =
                    DbFacade.getInstance()
                            .getPermissionDAO()
                            .getTreeForEntity(objectId,
                                    getParameters().getVdcObjectType(),
                                    getUserID(),
                                    getParameters().isFiltered());
        }
        getQueryReturnValue().setReturnValue(perms);
    }
}
