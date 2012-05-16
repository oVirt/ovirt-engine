package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;

public class RoleDAOTest extends BaseDAOTestCase {
    private static final Guid AD_ELEMENT_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final int ROLE_COUNT = 3;

    private RoleDAO dao;
    private roles existingRole;
    private roles newRole;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getRoleDAO());

        existingRole = dao.get(new Guid("f5972bfa-7102-4d33-ad22-9dd421bfba78"));

        newRole = new roles();
        newRole.setname("new role");
        newRole.setdescription("This is a new role.");
        newRole.setType(RoleType.USER);
        newRole.setInheritable(false);
    }

    /**
     * Ensures that the id must be valid.
     */
    @Test
    public void testGetRoleWithInvalidId() {
        roles result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving a role works as expected.
     */
    @Test
    public void testGetRole() {
        roles result = dao.get(existingRole.getId());

        assertNotNull(result);
        assertEquals(existingRole, result);
    }

    /**
     * Ensures that an invalid name results in a null role.
     */
    @Test
    public void testGetRoleByNameWithInvalidName() {
        roles result = dao.getByName("Farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a role by name works as expected.
     */
    @Test
    public void testGetRoleByName() {
        roles result = dao.getByName(existingRole.getname());

        assertNotNull(result);
        assertEquals(existingRole, result);
    }

    /**
     * Ensures the right number of roles are returned.
     */
    @Test
    public void testGetAllRoles() {
        List<roles> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(ROLE_COUNT, result.size());
    }

    /**
     * Ensures an empty collection is returned when no roles exist for the specified ad element.
     */
    @Test
    public void testGetAllRolesForAdElementWithInvalidId() {
        List<roles> result = dao.getAllForAdElement(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of roles are returned.
     */
    @Test
    public void testGetAllRolesForAdElement() {
        List<roles> result = dao.getAllForAdElement(AD_ELEMENT_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty collection is returned if the ad element is invalid.
     */
    @Test
    public void testGetAllRolesForUserAndGroupByAdElementWithInvalidAdElement() {
        List<roles> result = dao.getAllForAdElement(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right collection of roles is returned.
     */
    @Test
    public void testGetAllRolesForUserAndGroupByAdElement() {
        List<roles> result = dao.getAllForAdElement(AD_ELEMENT_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that saving a role works as expected.
     */
    @Test
    public void testSaveRole() {
        dao.save(newRole);

        roles result = dao.getByName(newRole.getname());

        assertNotNull(result);
        assertEquals(newRole, result);
    }

    /**
     * Ensures that updating a role works as expected.
     */
    @Test
    public void testUpdateRole() {
        existingRole.setdescription("This is an updated description");

        dao.update(existingRole);

        roles result = dao.get(existingRole.getId());

        assertNotNull(result);
        assertEquals(existingRole, result);
    }

    /**
     * Asserts removing a role works as expected
     */
    @Test
    public void testRemoveRole() {
        dao.remove(existingRole.getId());

        roles result = dao.get(existingRole.getId());

        assertNull(result);
    }
}
