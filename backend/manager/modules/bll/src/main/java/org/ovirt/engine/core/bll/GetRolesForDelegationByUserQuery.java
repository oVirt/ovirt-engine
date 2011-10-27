package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetRolesForDelegationByUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetRolesForDelegationByUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdcUser user = getCurrentUser();
        // check the user has SuperUser on System Object, directly or via group membership.
        List<roles> myRoles = DbFacade.getInstance().getRoleDAO().getAll();
        permissions adminPerm = DbFacade
        .getInstance()
        .getPermissionDAO()
        .getForRoleAndAdElementAndObjectWithGroupCheck(
                                                       PredefinedRoles.SUPER_USER.getId(), user.getUserId(),
                                                       MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        if (adminPerm == null) {
            // user is not super admin - remove all
            // ADMIN roles from the list
            for (Iterator i = myRoles.iterator(); i.hasNext();) {
                roles r = (roles) i.next();
                if (r.getType() == RoleType.ADMIN)
                    i.remove();
            }
        }
        getQueryReturnValue().setReturnValue(myRoles);

    }

    private VdcUser getCurrentUser() {
        String sessionId = getParameters().getSessionId();
        VdcUser user = null;
        if (!StringHelper.isNullOrEmpty(sessionId)) {
            user = (VdcUser) SessionDataContainer.getInstance().GetData(sessionId, "VdcUser");
        }
        return user;
    }
}
