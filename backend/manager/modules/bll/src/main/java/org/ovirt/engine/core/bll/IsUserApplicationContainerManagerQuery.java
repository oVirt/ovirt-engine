package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.PermissionDao;

public class IsUserApplicationContainerManagerQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public IsUserApplicationContainerManagerQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(
                permissionDao.getForRoleAndAdElementAndObject(
                        PredefinedRoles.SUPER_USER.getId(),
                        getUser().getId(),
                        MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID
                ) != null);
    }
}
