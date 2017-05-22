package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.HasAdElementReconnectPermissionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;

public class HasAdElementReconnectPermissionQuery<P extends HasAdElementReconnectPermissionParameters>
    extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public HasAdElementReconnectPermissionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid perm = permissionDao.getEntityPermissions(getParameters().getAdElementId(),
                ActionGroup.RECONNECT_TO_VM,
                getParameters().getObjectId(),
                VdcObjectType.VM);

        getQueryReturnValue().setReturnValue(perm != null);
    }

}
