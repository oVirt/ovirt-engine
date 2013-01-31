package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetRolesForDelegationByUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetRolesForDelegationByUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        IVdcUser user = getCurrentUser();
        // check the user has SuperUser on System Object, directly or via group membership.
        List<Role> myRoles = DbFacade.getInstance().getRoleDao().getAll();
        permissions adminPerm = DbFacade
                .getInstance()
                .getPermissionDao()
                .getForRoleAndAdElementAndObjectWithGroupCheck(
                        PredefinedRoles.SUPER_USER.getId(), user.getUserId(),
                        MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        if (adminPerm == null) {
            // user is not super admin - remove all
            // ADMIN roles from the list
            for (Iterator i = myRoles.iterator(); i.hasNext();) {
                Role r = (Role) i.next();
                if (r.getType() == RoleType.ADMIN)
                    i.remove();
            }
        }
        getQueryReturnValue().setReturnValue(myRoles);

    }

    private IVdcUser getCurrentUser() {
        String sessionId = getParameters().getSessionId();
        IVdcUser user = null;
        if (StringUtils.isNotEmpty(sessionId)) {
            user = SessionDataContainer.getInstance().getUser(sessionId, false);
        }
        return user;
    }
}
