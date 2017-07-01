package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.PermissionDao;

public class IsUserApplicationContainerManagerQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public IsUserApplicationContainerManagerQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
