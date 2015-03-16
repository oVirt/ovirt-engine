package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetSystemPermissionsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetSystemPermissionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Permission> perms = DbFacade.getInstance().getPermissionDao().getAllForEntity(
                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        getQueryReturnValue().setReturnValue(perms);
    }

}
