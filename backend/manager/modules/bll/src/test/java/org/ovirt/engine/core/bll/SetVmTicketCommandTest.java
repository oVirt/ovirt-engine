package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class SetVmTicketCommandTest extends BaseCommandTest {
    // The command that will be tested:
    private SetVmTicketCommand<SetVmTicketParameters> command;

    @BeforeEach
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
        assertEquals("Legolas", command.getConsoleUserName());
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
        assertEquals("Legolas@MiddleEarth.com", command.getConsoleUserName());
    }
}
