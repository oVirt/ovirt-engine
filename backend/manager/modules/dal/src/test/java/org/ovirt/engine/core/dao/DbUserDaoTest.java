package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class DbUserDaoTest extends BaseDaoTestCase {
    private DbUserDao dao;
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

        newUser.setExternalId("0");
        newUser.setId(Guid.newGuid());
        newUser.setFirstName("Bob");
        newUser.setLastName("Milqtoste");
        newUser.setLoginName("newuser");
        newUser.setEmail("newuser@redhat.com");
        newUser.setDomain("domain");
        newUser.setGroupNames(new LinkedList<>(Arrays.asList("groups")));
        newUser.setNamespace("*");
    }

    /**
     * Ensures that trying to get a user using an invalid id fails.
     */
    @Test
    public void testGetWithInvalidId() {
        DbUser result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving an object by id works as expected.
     */
    @Test
    public void testGet() {
        DbUser result = dao.get(existingUser.getId());

        assertNotNull(result);
        assertEquals(existingUser, result);
    }

    /**
     * Ensures that trying to get an user with an invalid external id fails.
     */
    @Test
    public void testGetWithInvalidExternalId() {
        DbUser result = dao.getByExternalId("testportal.redhat.com", "0");
        assertNull(result);
    }

    /**
     * Ensures that retrieving an user by external id works as expected.
     */
    @Test
    public void testGetByExternalId() {
        DbUser result = dao.getByExternalId("testportal.redhat.com", "a");
        assertNotNull(result);
    }

    /**
     * Ensures that update cycle doesn't change the external identifier.
     */
    @Test
    public void testUpdateDoesntChangeExternalId() {
        DbUser userBefore = dao.get(existingUser.getId());
        dao.update(userBefore);
        DbUser userAfter = dao.get(existingUser.getId());
        assertEquals(userBefore.getExternalId(), userAfter.getExternalId());
    }

    /**
     * Ensures that searching for a user by an unused name results in no user.
     */
    @Test
    public void testGetByUsernameWithInvalidName() {
        DbUser result = dao.getByUsernameAndDomain("IdoNoExist", existingUser.getDomain());

        assertNull(result);
    }

    /**
     * Ensures that retrieving by username works.
     */
    @Test
    public void testGetByUsername() {
        DbUser result = dao.getByUsernameAndDomain(existingUser.getLoginName(), existingUser.getDomain());

        assertNotNull(result);
        assertEquals(existingUser, result);
    }

    /**
     * Ensures that nothing is returned when the VM is invalid.
     */
    @Test
    public void testGetAllForVmWithInvalidVm() {
        List<DbUser> result = dao.getAllForVm(Guid.newGuid());

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
        assertEquals(4, result.size());
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
        assertNotNull(result);
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

        DbUser result = dao.getByUsernameAndDomain(newUser.getLoginName(), newUser.getDomain());

        assertEquals(newUser, result);
    }

    /**
     * Ensures that updating a user works as expected.
     */
    @Test
    public void testUpdate() {
        existingUser.setFirstName("changedname");
        existingUser.setLastName("changedsurname");

        dao.update(existingUser);

        DbUser result = dao.get(existingUser.getId());

        assertEquals(existingUser, result);
    }

    @Test
    public void testSaveOrUpdateExisting() {
        int sizeBeforeSave = dao.getAll().size();
        existingUser.setFirstName("changedname");
        existingUser.setLastName("changedsurname");
        dao.saveOrUpdate(existingUser);
        int sizeAfterSave = dao.getAll().size();
        DbUser result = dao.get(existingUser.getId());
        assertEquals(existingUser, result);
        assertEquals(0, sizeAfterSave - sizeBeforeSave);

    }

    @Test
    public void testSaveOrUpdateNew() {
        int sizeBeforeSave = dao.getAll().size();
        dao.saveOrUpdate(newUser);
        DbUser result = dao.getByUsernameAndDomain(newUser.getLoginName(), newUser.getDomain());
        int sizeAfterSave = dao.getAll().size();
        assertEquals(newUser, result);
        assertEquals(1, sizeAfterSave - sizeBeforeSave);
    }

    /**
     * Ensures that inserting an user with no external id fails, as it has a
     * not null constraint.
     */
    @Test(expected = RuntimeException.class)
    public void testSaveUserWithoutExternalIdFails() {
        newUser.setExternalId(null);
        dao.save(newUser);
    }

    /**
     * Ensures that inserting an user with the same external id and domain than
     * an existing user fails, as there is a unique constraint for that pair
     * of attributes.
     */
    @Test(expected = RuntimeException.class)
    public void testSaveUserDuplicatedDomainAndExternalId() {
        newUser.setDomain(existingUser.getDomain());
        newUser.setExternalId(existingUser.getExternalId());
        dao.save(newUser);
    }

    /**
     * Ensures that removing users works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(deletableUser.getId());

        DbUser result = dao.get(deletableUser.getId());

        assertNull(result);
    }
}
