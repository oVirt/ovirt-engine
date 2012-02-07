package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.action.AdGroupElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class AdGroupsHandlingOperationBase<T extends AdGroupElementParametersBase> extends
        AdGroupsHandlingCommandBase<T> {
    public AdGroupsHandlingOperationBase(T parameters) {
        super(parameters);
    }

    @Override
    protected ad_groups getAdGroup() {
        return getParameters().getAdGroup();
    }

    public static void AddAdGroupToDBWithPermissionIfNeeded(ad_groups AdGroup,
            String sessionId,
            CompensationContext compensationContext) {
        ad_groups currentGroup = DbFacade.getInstance().getAdGroupDAO().get(AdGroup.getid());
        if (currentGroup == null) {
            // if group still not in db - add it
            Guid regularUser = new Guid("00000000-0000-0000-0001-000000000001");
            permissions userPermission = new permissions();
            userPermission.setad_element_id(AdGroup.getid());
            userPermission.setrole_id(regularUser);

            PermissionsOperationsParametes permissionsParams = new PermissionsOperationsParametes(userPermission,
                    AdGroup);
            permissionsParams.setSessionId(sessionId);

            Backend.getInstance().runInternalAction(VdcActionType.AddPermission,
                    permissionsParams,
                    new CommandContext(compensationContext));
        }
    }
}
