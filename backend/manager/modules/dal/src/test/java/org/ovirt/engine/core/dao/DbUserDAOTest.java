package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.user_sessions;

public class DbUserDAOTest extends BaseDAOTestCase {
    private static final int USER_SESSION_COUNT = 5;
    private static final String SESSION_ID = "21098765432109876543210987654321";
    private DbUserDAO dao;
    private DbUser existingUser;
    private DbUser deletableUser;
    private DbUser newUser;
    private Guid vm;
    private user_sessions newSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getDbUserDao();
        vm = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");

        existingUser = dao
                .get(new Guid("9bf7c640-b620-456f-a550-0348f366544a"));
        deletableUser = dao.get(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));

        newUser = new DbUser();

        newUser.setname("Bob");
        newUser.setsurname("Milqtoste");
        newUser.setusername("newuser");
        newUser.setemail("newuser@redhat.com");
        newUser.setdomain("domain");
        newUser.setgroups("groups");

        newSession = new user_sessions();
        newSession.setsession_id(SESSION_ID);
        newSession.setuser_id(existingUser.getuser_id());
        Date now = new Date();
        now.setTime(System.currentTimeMillis());
        newSession.setlogin_time(now);
    }

    /**
     * Ensures that trying to get a user using an invalid id fails.
     */
    @Test
    public void testGetWithInvalidId() {
        DbUser result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving an object by id works as expected.
     */
    @Test
    public void testGet() {
        DbUser result = dao.get(existingUser.getuser_id());

        assertNotNull(result);
        assertEquals(existingUser, result);
    }

    /**
     * Ensures that searching for a user by an unused name results in no user.
     */
    @Test
    public void testGetByUsernameWithInvalidName() {
        DbUser result = dao.getByUsername("IdoNoExist");

        assertNull(result);
    }

    /**
     * Ensures that retrieving by username works.
     */
    @Test
    public void testGetByUsername() {
        DbUser result = dao.getByUsername(existingUser.getusername());

        assertNotNull(result);
        assertEquals(existingUser, result);
    }

    /**
     * Ensures that nothing is returned when the VM is invalid.
     */
    @Test
    public void testGetAllForVmWithInvalidVm() {
        List<DbUser> result = dao.getAllForVm(Guid.NewGuid());

        // TODO this should return an empty collection
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that retrieving all users for a VM works as expected.
     */
    @Test
    public void testGetAllForVm() {
        List<DbUser> result = dao.getAllForVm(vm);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllTimeLeasedUsersForVm() {
        // TODO this API is broken and cannot be tested
    }

    @Test
    public void testGetAll() {
        List<DbUser> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    /**
     * Retrieves all that match a specified query.
     */
    @Test
    public void testGetAllWithQuery() {
        List<DbUser> result = dao.getAllWithQuery("select * from users");

        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that the right number of sessions are returned.
     */
    @Test
    public void testGetAllUserSessions() {
        List<user_sessions> result = dao.getAllUserSessions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(USER_SESSION_COUNT, result.size());
    }

    /**
     * Ensures that saving a user works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newUser);

        DbUser result = dao.getByUsername(newUser.getusername());

        assertEquals(newUser, result);
    }

    /**
     * Ensures that saving a user works as expected.
     */
    @Test
    public void testSaveUserWithTooManyGroups() {
        DbUser tooManyGroupsUser = new DbUser();
        tooManyGroupsUser.setname("I");
        tooManyGroupsUser.setsurname("Have");
        tooManyGroupsUser.setusername("too");
        tooManyGroupsUser.setemail("many@redhat.com");
        tooManyGroupsUser.setdomain("domain");
        tooManyGroupsUser.setgroups("groups");
        // Using 2048 because that was the field size before the patch that changed it to text
        tooManyGroupsUser.setGroupIds(StringUtils.leftPad("groups", 2048));

        dao.save(tooManyGroupsUser);

        DbUser result = dao.getByUsername(tooManyGroupsUser.getusername());

        assertEquals(tooManyGroupsUser, result);
    }

    /**
     * Ensures that saving a user session works as expected.
     */
    @Test
    public void testSaveSession() {
        List<user_sessions> before = dao.getAllUserSessions();

        dao.saveSession(newSession);

        List<user_sessions> after = dao.getAllUserSessions();
        assertTrue(after.size() > before.size());

        boolean itWorked = false;

        for (user_sessions session : after) {
            itWorked |= newSession.equals(session);
        }

        assertTrue(itWorked);
    }

    /**
     * Ensures that updating a user works as expected.
     */
    @Test
    public void testUpdate() {
        existingUser.setname("changedname");
        existingUser.setsurname("changedsurname");

        dao.update(existingUser);

        DbUser result = dao.get(existingUser.getuser_id());

        assertEquals(existingUser, result);
    }

    /**
     * Ensures that removing users works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(deletableUser.getuser_id());

        DbUser result = dao.get(deletableUser.getuser_id());

        assertNull(result);
    }

    /**
     * Ensures that removing a single session works as expected.
     */
    @Test
    public void testRemoveUserSession() {
        List<user_sessions> before = dao.getAllUserSessions();
        user_sessions deadSession = before.get(0);

        dao.removeUserSession(deadSession.getsession_id(), deadSession.getuser_id());

        List<user_sessions> after = dao.getAllUserSessions();

        assertTrue(after.size() < before.size());
        for (user_sessions session : after) {
            if (deadSession.equals(session))
                fail("The session should have been deleted.");
        }
    }

    /**
     * Ensures that removing all sessions works as expected.
     */
    @Test
    public void testRemoveAllSessions() {
        dao.removeAllSessions();

        List<user_sessions> result = dao.getAllUserSessions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
