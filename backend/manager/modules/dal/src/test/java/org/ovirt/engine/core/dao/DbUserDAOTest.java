package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class DbUserDAOTest extends BaseDAOTestCase {
    private DbUserDAO dao;
    private DbUser existingUser;
    private DbUser deletableUser;
    private DbUser newUser;
    private Guid vm;

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
    public void testGetAll() {
        List<DbUser> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

        @Test
    public void testGetFilteredWithPermissions() {
        List<DbUser> result = dao.getAll(PRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetFilteredWithoutPermissions() {
        List<DbUser> result = dao.getAll(UNPRIVILEGED_USER_ID, true);
        assertNull(result);
        assertTrue(result.isEmpty());
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
}
