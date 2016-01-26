package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class SetVmTicketCommandTest extends BaseCommandTest {
    // The command that will be tested:
    private SetVmTicketCommand<SetVmTicketParameters> command;

    @Before
    public void setUp() {
        command = new SetVmTicketCommand<>(new SetVmTicketParameters(), null);
    }

    /**
     * Check that the constructed consule user name is {@code null} when the
     * user doesn't have a name.
     */
    @Test
    public void testNullConsoleUserNameWhenNoUserSet() {
        DbUser user = new DbUser();
        command.setCurrentUser(user);
        assertNull(command.getConsoleUserName());
    }

    /**
     * Check that when the user doesn't have a directory name the consule user
     * name contains only the login name.
     */
    @Test
    public void testOnlyLoginNameWhenNoDirectorySet() {
        DbUser user = new DbUser();
        user.setLoginName("Legolas");
        user.setDomain("");
        command.setCurrentUser(user);
        assertEquals(command.getConsoleUserName(), "Legolas");
    }

    /**
     * Check that when the has a name and a directory name the console user name
     * is the user name followed by {@code @} and the directory name.
     */
    @Test
    public void testLoginNameAtDirectoryWhenDirectorySet() {
        DbUser user = new DbUser();
        user.setLoginName("Legolas");
        user.setDomain("MiddleEarth.com");
        command.setCurrentUser(user);
        assertEquals(command.getConsoleUserName(), "Legolas@MiddleEarth.com");
    }

    @Test
    public void checkPermissionsAdminStealsFromOtherAdmin() {
        testCheckPermissions(true, true, true);
    }

    @Test
    public void checkPermissionsUserStealsFromOtherAdmin() {
        testCheckPermissions(false, true, false);
    }

    @Test
    public void checkPermissionsAdminStealsFromOtherUser() {
        testCheckPermissions(true, false, true);
    }

    @Test
    public void checkPermissionsUserStealsFromOtherUser() {
        testCheckPermissions(false, false, true);
    }

    private void testCheckPermissions(boolean isStealerAdmin, boolean isCurrentConsoleUserAdmin, boolean stealingAllowed) {
        Guid vmId = Guid.createGuidFromString("00000000-0000-0000-0000-000000000001");
        Guid currentUserId = Guid.createGuidFromString("00000000-0000-0000-0000-000000000002");
        Guid vmConsoleUserId = Guid.createGuidFromString("00000000-0000-0000-0000-000000000003");
        final DbUser user = mock(DbUser.class);
        when(user.getId()).thenReturn(currentUserId);
        when(user.isAdmin()).thenReturn(isStealerAdmin);
        SetVmTicketCommand<SetVmTicketParameters> commandSpy = spy(command);
        when(commandSpy.getCurrentUser()).thenReturn(user);
        VM vm = mock(VM.class);
        when(vm.getId()).thenReturn(vmId);
        when(vm.getConsoleUserId()).thenReturn(vmConsoleUserId);
        when(vm.getAllowConsoleReconnect()).thenReturn(false);
        when(commandSpy.getVm()).thenReturn(vm);
        DbUser vmConsoleUser = mock(DbUser.class);
        when(vmConsoleUser.getId()).thenReturn(currentUserId);
        when(vmConsoleUser.isAdmin()).thenReturn(isCurrentConsoleUserAdmin);
        DbFacade dbFacade = mock(DbFacade.class, RETURNS_DEEP_STUBS);
        when(dbFacade.getDbUserDao().get(eq(vmConsoleUserId))).thenReturn(vmConsoleUser);
        when(commandSpy.getDbFacade()).thenReturn(dbFacade);
        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                if (Arrays.deepEquals(invocation.getArguments(), new Object[] {
                        currentUserId, ActionGroup.CONNECT_TO_VM, vmId, VdcObjectType.VM
                })) {
                    return true;
                }
                if (Arrays.deepEquals(invocation.getArguments(), new Object[] {
                        currentUserId, ActionGroup.RECONNECT_TO_VM, vmId, VdcObjectType.VM
                })) {
                    return true;
                }
                throw new RuntimeException(
                        String.format("Unexpected arguments: %s", Arrays.toString(invocation.getArguments())));
            }
        }).when(commandSpy).checkUserAuthorization(any(), any(), any(), any());
        List<PermissionSubject> permissionSubjects = Arrays.asList(
                new PermissionSubject(vmId, VdcObjectType.VM, ActionGroup.CONNECT_TO_VM),
                new PermissionSubject(vmId, VdcObjectType.VM, ActionGroup.RECONNECT_TO_VM)
        );

        boolean permissionsValid = commandSpy.checkPermissions(permissionSubjects);
        assertEquals(stealingAllowed, permissionsValid);
    }


}
