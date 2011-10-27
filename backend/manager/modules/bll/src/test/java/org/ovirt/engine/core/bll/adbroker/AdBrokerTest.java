package org.ovirt.engine.core.bll.adbroker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.AdUser;

public class AdBrokerTest {

    @Test
    public void LUAuthenticateUserCommandTest() {
        try {

            if (!isRunningOnWindows()) {
                System.out.println("LUAuthenticateUserCommandTes should be run on windows");
                return;
            }

            LdapUserPasswordBaseParameters adParameters = new LdapUserPasswordBaseParameters("", "TestUser", "TestUser");
            LUAuthenticateUserCommand command = new LUAuthenticateUserCommand(adParameters);
            AdUser user = (AdUser) command.Execute().getReturnValue();
            assertTrue("command LUAuthenticateUserCommand failed for user TestUser ", command.getSucceeded());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void LUChangeUserPasswordCommandTest() {
        try {

            if (!isRunningOnWindows()) {
                System.out.println("LUChangeUserPasswordCommandTest be run on windows");
                return;
            }

            LdapChangeUserPasswordParameters adParams = new LdapChangeUserPasswordParameters("", "TestUser", "TetsUser",
                    "TestUser11");
            LUChangeUserPasswordCommand command = new LUChangeUserPasswordCommand(adParams);

            Boolean ret = (Boolean) command.Execute().getReturnValue();
            assertTrue("command LUChangeUserPasswordCommand failed for user TestUser ", command.getSucceeded());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean isRunningOnWindows() {
        String osName = System.getProperty("os.name");
        return osName.contains("Windows");
    }

    /*
     * @Test public void LUGetAdUserByUserIdCommand() { try { LdapSearchByIdParameters adParameters = new
     * LdapSearchByIdParameters( null, "", "TestUser", "TestUser"); LUAuthenticateUserCommand command = new
     * LUAuthenticateUserCommand( adParameters); AdUser user = (AdUser) command.Execute().getReturnValue(); assertTrue(
     * "command LUAuthenticateUserCommand failed for user TestUser ", command.getSucceeded()); } catch (Throwable e) {
     * e.printStackTrace(); }
     *
     * }
     */
}
