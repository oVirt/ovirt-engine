package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;

public class DetachActionGroupsFromRoleCommandTest extends AbstractRolesCommandTestBase {

    @Override
    protected ActionGroupsToRoleParameter generateParameters() {
        Guid roleId = Guid.newGuid();
        ArrayList<ActionGroup> groups =
                new ArrayList<>(Arrays.asList(ActionGroup.DELETE_HOST, ActionGroup.CONFIGURE_ENGINE));
        return new ActionGroupsToRoleParameter(roleId, groups);
    }

    @Override
    protected DetachActionGroupsFromRoleCommand<? extends ActionGroupsToRoleParameter> generateCommand() {
        return new DetachActionGroupsFromRoleCommand<>(getParams(), null);
    }

    @Override
    protected ActionGroupsToRoleParameter getParams() {
        return (ActionGroupsToRoleParameter) super.getParams();
    }

    /* execute related tests */

    /** A flow test that makes sure all the action groups are removed correctly if the role is not updated because it still has roles that allow viewing children */
    @Test
    public void testExecuteCommandNoUpdate() {
        mockRoleGroups(ActionGroup.CONFIGURE_HOST_NETWORK);
        getRole().setAllowsViewingChildren(true);
        getCommand().executeCommand();
        verifyRoleSaving(true, false);
    }

    /** A flow test that makes sure all the action groups are removed correctly if the role is not updated because it does not allow viewing children */
    @Test
    public void testExecuteCommandNoUpdateSinceRoleWasAlreadyNotInheritable() {
        getRole().setAllowsViewingChildren(false);
        getCommand().executeCommand();
        verifyRoleSaving(false, false);
    }

    /** A flow test that makes sure all the action groups are removed correctly if the role is updated because no roles are left in it*/
    @Test
    public void testExecuteCommandWithUpdateAllRolesRemoved() {
        mockRoleGroups();
        getRole().setAllowsViewingChildren(true);
        getCommand().executeCommand();
        verifyRoleSaving(true, true);
    }

    /** A flow test that makes sure all the action groups are removed correctly if the role is updated because no roles that allow viewing children are left in it*/
    @Test
    public void testExecuteCommandWithUpdateInheritableRolesRemoved() {
        mockRoleGroups(ActionGroup.CREATE_VM);
        getRole().setAllowsViewingChildren(true);
        getCommand().executeCommand();
        verifyRoleSaving(true, true);
    }

    /** Mock the action groups remaining on the role AFTER some were detached */
    private void mockRoleGroups(ActionGroup... groups) {
        List<RoleGroupMap> maps = new ArrayList<>();
        for (ActionGroup group : groups) {
            maps.add(new RoleGroupMap(group, getParams().getRoleId()));
        }
        when(getRoleGroupMapDaoMock().getAllForRole(getParams().getRoleId())).thenReturn(maps);
    }

    private void verifyRoleSaving(boolean wasInheritable, boolean roleStatusChanged) {
        for (ActionGroup group : getParams().getActionGroups()) {
            verify(getRoleGroupMapDaoMock()).remove(group, getParams().getRoleId());
        }

        if (wasInheritable) {
            verify(getRoleDaoMock()).get(getParams().getRoleId());
            verify(getRoleGroupMapDaoMock()).getAllForRole(getParams().getRoleId());
        }

        if (roleStatusChanged) {
            verify(getRoleDaoMock()).update(getRole());
        }

        verifyNoMoreInteractions(getRoleGroupMapDaoMock());
    }
}
