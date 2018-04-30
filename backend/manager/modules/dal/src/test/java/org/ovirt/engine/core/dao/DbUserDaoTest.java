package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class DbUserDaoTest extends BaseDaoTestCase<DbUserDao> {
    private static final Guid ADMIN_ROLE_TYPE_FROM_FIXTURE_ID = new Guid("F5972BFA-7102-4D33-AD22-9DD421BFBA78");
    private static final Guid SYSTEM_OBJECT_ID = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    @Inject
    private PermissionDao permissionDao;
    private DbUser existingUser;
    private DbUser deletableUser;
    private DbUser newUser;
    private Guid vm;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        vm = FixturesTool.VM_RHEL5_POOL_50;

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
        newUser.setGroupNames(new LinkedList<>(Collections.singletonList("groups")));
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
    @Test
    public void testSaveUserWithoutExternalIdFails() {
        newUser.setExternalId(null);
        assertThrows(RuntimeException.class, () -> dao.save(newUser));
    }

    /**
     * Ensures that inserting an user with the same external id and domain than
     * an existing user fails, as there is a unique constraint for that pair
     * of attributes.
     */
    @Test
    public void testSaveUserDuplicatedDomainAndExternalId() {
        newUser.setDomain(existingUser.getDomain());
        newUser.setExternalId(existingUser.getExternalId());
        assertThrows(RuntimeException.class, () -> dao.save(newUser));
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

    @Test
    public void testUpdateLastAdminCheckStatus() {

        // Getting a nonAdmin user that is defined in the fixtures
        DbUser nonAdminUser = dao.getByUsernameAndDomain("userportal2@testportal.redhat.com", "testportal.redhat.com");

        assertNotNull(nonAdminUser);
        assertFalse(nonAdminUser.isAdmin());

        // execute and validate when not admin
        dao.updateLastAdminCheckStatus(nonAdminUser.getId());
        nonAdminUser = dao.get(nonAdminUser.getId());

        assertFalse(nonAdminUser.isAdmin());

        Permission perms = new Permission();
        perms.setRoleType(RoleType.ADMIN);

        // An available role from the fixtures
        perms.setRoleId(ADMIN_ROLE_TYPE_FROM_FIXTURE_ID);
        perms.setAdElementId(nonAdminUser.getId());
        perms.setObjectId(SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);

        // Save the permission to the DB and make sure it has been saved
        permissionDao.save(perms);
        assertNotNull(permissionDao.get(perms.getId()));

        // execute and validate when admin
        dao.updateLastAdminCheckStatus(nonAdminUser.getId());
        nonAdminUser = dao.get(nonAdminUser.getId());

        assertTrue(nonAdminUser.isAdmin());
    }
}
