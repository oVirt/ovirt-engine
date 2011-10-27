package org.ovirt.engine.core.itests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Test class for roles and ActionGroups
 *
 * @see {@link ActionGroup} , {@link roles}
 *
 */
public class RoleActionGroupsTest extends AbstractBackendTest {

    private static Guid roleId;

    // create role with 1 actionGroup
    @Test
    public void createRole() {
        runAsSuperAdmin();
        // TODO Auto-generated method stub
        roles role = new roles("test role", Guid.NewGuid(), "role" + testSequence);
        role.setType(RoleType.ADMIN);
        ArrayList<ActionGroup> groups = new ArrayList<ActionGroup>();
        groups.add(ActionGroup.CHANGE_VM_CD);
        VdcReturnValueBase action = backend.RunAction(VdcActionType.AddRoleWithActionGroups,
                new RoleWithActionGroupsParameters(role, groups));
        roleId = (Guid) action.getActionReturnValue();

        Assert.assertTrue(action.getSucceeded());
        List<RoleGroupMap> getRoleGroupMapsByRoleId = DbFacade.getInstance().getRoleGroupMapDAO().getAllForRole(roleId);
        Assert.assertTrue(getRoleGroupMapsByRoleId.size() == 1);
        Assert.assertTrue(getRoleGroupMapsByRoleId.get(0).getActionGroup() == ActionGroup.CHANGE_VM_CD);

    }

    // attach 4 more actionGroups
    @Test
    public void attachGroups() {
        runAsSuperAdmin();

        ArrayList<ActionGroup> groups = new ArrayList<ActionGroup>();
        groups.add(ActionGroup.CONFIGURE_CLUSTER_NETWORK);
        groups.add(ActionGroup.CREATE_CLUSTER);
        groups.add(ActionGroup.CREATE_TEMPLATE);
        groups.add(ActionGroup.DELETE_HOST);
        VdcReturnValueBase action = backend.RunAction(VdcActionType.AttachActionGroupsToRole,
                sessionize(new ActionGroupsToRoleParameter(roleId, groups)));
        Assert.assertTrue(action.getSucceeded());
        List<RoleGroupMap> getRoleGroupMapsByRoleId = DbFacade.getInstance().getRoleGroupMapDAO().getAllForRole(roleId);
        Assert.assertTrue(getRoleGroupMapsByRoleId.size() == 5);

    }

    // detach 2 actionGroups
    @Test
    public void detachGroups() {
        runAsSuperAdmin();

        ArrayList<ActionGroup> groups = new ArrayList<ActionGroup>();
        groups.add(ActionGroup.CONFIGURE_CLUSTER_NETWORK);
        groups.add(ActionGroup.DELETE_HOST);
        VdcReturnValueBase action = backend.RunAction(VdcActionType.DetachActionGroupsFromRole,
                sessionize(new ActionGroupsToRoleParameter(roleId, groups)));

        Assert.assertTrue(action.getSucceeded());
        List<RoleGroupMap> getRoleGroupMapsByRoleId = DbFacade.getInstance().getRoleGroupMapDAO().getAllForRole(roleId);
        Assert.assertTrue(getRoleGroupMapsByRoleId.size() == 3);

    }

    // remove the role
    @Test
    public void removeRole() {
        runAsSuperAdmin();

        VdcReturnValueBase action = backend.RunAction(VdcActionType.RemoveRole, sessionize(new RolesParameterBase(roleId)));
        List<RoleGroupMap> groups = DbFacade.getInstance().getRoleGroupMapDAO().getAllForRole(roleId);
        Assert.assertTrue(groups == null || groups.isEmpty());
    }
}
