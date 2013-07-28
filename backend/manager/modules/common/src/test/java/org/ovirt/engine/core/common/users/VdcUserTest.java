package org.ovirt.engine.core.common.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link VdcUser} class. */
public class VdcUserTest {

    /** The LdapUser to create VdcUsers from */
    private LdapUser adUser;

    @Before
    public void setUp() {
        adUser = new LdapUser();
        adUser.setUserName("UserName");
        adUser.setPassword("password");
        adUser.setUserId(Guid.newGuid());
        adUser.setDomainControler("DomainController");
    }

    /** Tests {@link VdcUser#VdcUser(LdapUser)) */
    @Test
    public void testAdUserConstrcutor() {
        VdcUser user = new VdcUser(adUser);
        assertFalse("By default, a user should not be an admin", user.isAdmin());
        user.setAdmin(true);
        assertTrue("after being set as such, the user should be an admin", user.isAdmin());
    }

    /** Tests {@link VdcUser#VdcUser(LdapUser, boolean)) */
    @Test
    public void testAdUserAndFalseBooleanConstrcutor() {
        VdcUser user = new VdcUser(adUser, false);
        assertFalse("If not set, a user should not be an admin", user.isAdmin());
        user.setAdmin(true);
        assertTrue("after being set as such, the user should be an admin", user.isAdmin());
    }

    /** Tests {@link VdcUser#VdcUser(LdapUser, boolean)) */
    @Test
    public void testAdUserAndTrueBooleanConstrcutor() {
        VdcUser user = new VdcUser(adUser, true);
        assertTrue("If set, a user should not be an admin", user.isAdmin());
        user.setAdmin(false);
        assertFalse("after being set not to be, the user should be an admin", user.isAdmin());
    }

    @Test
    public void getUserFQN() {
        VdcUser user = new VdcUser();

        // expect null FQN when no username is set
        assertTrue(user.getFQN() == null);

        // expect only username if the domain is not set or empty
        user.setUserName("Legolas");
        user.setDomainControler("");
        assertTrue(user.getFQN().equals("Legolas"));

        // expect user@example.com in case both username and domains are set and non-empty
        user.setDomainControler("MiddleEarth.com");
        assertTrue(user.getFQN().equals("Legolas@MiddleEarth.com"));

    }
}
