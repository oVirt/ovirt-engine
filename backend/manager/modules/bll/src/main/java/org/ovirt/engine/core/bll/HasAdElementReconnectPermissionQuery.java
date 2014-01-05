package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.HasAdElementReconnectPermissionParameters;
import org.ovirt.engine.core.compat.Guid;

public class HasAdElementReconnectPermissionQuery<P extends HasAdElementReconnectPermissionParameters>
    extends QueriesCommandBase<P> {

    public HasAdElementReconnectPermissionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid perm = getDbFacade().getPermissionDao().getEntityPermissions(getParameters().getAdElementId(),
                ActionGroup.RECONNECT_TO_VM,
                getParameters().getObjectId(),
                VdcObjectType.VM);

        getQueryReturnValue().setReturnValue(perm != null);
    }

}
