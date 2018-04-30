package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AttachActionGroupsToRoleCommandTest extends AbstractRolesCommandTestBase {

    @Override
    protected ActionGroupsToRoleParameter generateParameters() {
        Guid roleId = Guid.newGuid();
        ArrayList<ActionGroup> groups =
                new ArrayList<>(Arrays.asList(ActionGroup.DELETE_HOST, ActionGroup.CONFIGURE_ENGINE));
        return new ActionGroupsToRoleParameter(roleId, groups);
    }

    @Override
    protected AttachActionGroupsToRoleCommand<? extends ActionGroupsToRoleParameter> generateCommand() {
        return new AttachActionGroupsToRoleCommand<>(getParams(), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AttachActionGroupsToRoleCommand<? extends ActionGroupsToRoleParameter> getCommand() {
        return (AttachActionGroupsToRoleCommand<? extends ActionGroupsToRoleParameter>) super.getCommand();
    }

    @Override
    protected ActionGroupsToRoleParameter getParams() {
        return (ActionGroupsToRoleParameter) super.getParams();
    }

    /* validate related tests */

    @Test
    public void testCheckGroupsCanBeAttachedAlreadyExists() {
        RoleGroupMap map = new RoleGroupMap(getParams().getActionGroups().get(0), getParams().getRoleId());
        mockGetAllForRole(Collections.singletonList(map));

        List<String> messages = new ArrayList<>(1);
        assertTrue(getCommand().checkIfGroupsCanBeAttached(messages), "validate should fail");
        assertEquals(EngineMessage.ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED.toString(), messages.get(0),
                "wrong messages");
    }

    @Test
    public void testCheckGroupsCanBeAttachedAdminIssues() {
        getRole().setType(RoleType.USER);
        RoleGroupMap map = new RoleGroupMap(ActionGroup.DELETE_STORAGE_POOL, getParams().getRoleId());
        mockGetAllForRole(Collections.singletonList(map));

        List<String> messages = new ArrayList<>(1);
        assertTrue(getCommand().checkIfGroupsCanBeAttached(messages), "validate should fail");
        assertEquals(EngineMessage.CANNOT_ADD_ACTION_GROUPS_TO_ROLE_TYPE.toString(), messages.get(0), "wrong messages");
    }

    @Test
    public void testCheckGroupsCanBeAttachedSuccess() {
        getRole().setType(RoleType.ADMIN);
        RoleGroupMap map = new RoleGroupMap(ActionGroup.DELETE_STORAGE_POOL, getParams().getRoleId());
        mockGetAllForRole(Collections.singletonList(map));

        List<String> messages = new ArrayList<>();
        assertFalse(getCommand().checkIfGroupsCanBeAttached(messages), "validate should succeed");
        assertTrue(messages.isEmpty(), "no messages sould have been added");
    }

    private void mockGetAllForRole(List<RoleGroupMap> groups) {
        when(getRoleGroupMapDaoMock().getAllForRole(getParams().getRoleId())).thenReturn(groups);
    }

    /* execute related tests */

    /** A flow test that makes sure all the action groups are set correctly if the role is not updated */
    @Test
    public void testExecuteCommandNoUpdate() {
        getRole().setAllowsViewingChildren(true);
        getCommand().executeCommand();
        verifyRoleSaving(false);
    }

    /** A flow test that makes sure all the action groups are set correctly if the role is updated*/
    @Test
    public void testExecuteCommandWithUpdate() {
        getRole().setAllowsViewingChildren(false);
        getCommand().executeCommand();
        verifyRoleSaving(true);
    }

    /** A flow test that makes sure all the action groups are set correctly if the role isn't updated because the added permission doesn't allow viewing children */
    @Test
    public void testExecuteCommandNoUpdateNonInheritableRole() {
        getRole().setAllowsViewingChildren(false);
        getParams().setActionGroups(new ArrayList<>(Collections.singletonList(ActionGroup.CREATE_VM)));
        getCommand().executeCommand();
        verifyRoleSaving(false);
    }

    private void verifyRoleSaving(boolean roleStatusChanged) {
        for (ActionGroup group : getParams().getActionGroups()) {
            verify(getRoleGroupMapDaoMock()).save(new RoleGroupMap(group, getParams().getRoleId()));
        }

        if (roleStatusChanged) {
            verify(getRoleDaoMock()).update(getRole());
        }
        verifyNoMoreInteractions(getRoleGroupMapDaoMock());
    }
}
