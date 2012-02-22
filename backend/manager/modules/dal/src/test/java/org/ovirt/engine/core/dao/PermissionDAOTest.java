package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;

public class PermissionDAOTest extends BaseDAOTestCase {
    private static final Guid STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
    private static final Guid ROLE_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");
    private static final Guid AD_ELEMENT_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final Guid VM_ENTITY_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid EXISTING_PERMISSION_ID = new Guid("9304ce01-2f5f-41b5-92c7-9d69ef0bcfbc");
    private static final Guid VDS_ENTITY_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VM_TEMPLATE_ENTITY_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid VM_POOL_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");
    private static final Guid CLUSTER_ENTITY_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid SYSTEM_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0077");
    private static final Guid STORAGE_POOL_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0078");
    private static final Guid STORAGE_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0079");
    private static final Guid USER_ENTITY_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid ROLE_ENTITY_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");
    private static final Guid VDS_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private PermissionDAO dao;
    private permissions new_permissions;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getPermissionDAO());

        new_permissions = new permissions(AD_ELEMENT_ID, ROLE_ID, STORAGE_POOL_ID,
                VdcObjectType.StoragePool);
    }

    /**
     * Ensures that getting a permission with a bad id fails.
     */
    @Test
    public void testGetWithInvalidId() {
        permissions result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right permission is retrieved.
     */
    @Test
    public void testGet() {
        permissions result = dao.get(EXISTING_PERMISSION_ID);

        assertNotNull(result);
        assertEquals(EXISTING_PERMISSION_ID, result.getId());
    }

    /**
     * Ensures that a null object is returned when the role is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidRole() {
        permissions result = dao.getForRoleAndAdElementAndObject(
                Guid.NewGuid(), AD_ELEMENT_ID, VM_ENTITY_ID);

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the element is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidAdElement() {
        permissions result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                Guid.NewGuid(), VM_ENTITY_ID);

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the object is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidObject() {
        permissions result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                AD_ELEMENT_ID, Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the role is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObject() {
        permissions result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                AD_ELEMENT_ID, VM_ENTITY_ID);

        assertNotNull(result);
        assertEquals(ROLE_ID, result.getrole_id());
        assertEquals(AD_ELEMENT_ID, result.getad_element_id());
        assertEquals(VM_ENTITY_ID, result.getObjectId());
    }

    @Test
    public void testGetAllForQuotaId() {
        permissions result = dao.getConsumedPermissionsForQuotaId(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertNull(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementWithInvalidId() {
        List<permissions> result = dao.getAllForAdElement(Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that the right set of permissions are returned.
     */
    @Test
    public void testGetAllForAdElement() {
        List<permissions> result = dao.getAllForAdElement(AD_ELEMENT_ID);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that the right set of permissions are returned, for a user with permissions.
     */
    @Test
    public void testGetAllForAdElementFilteredWithPermissions() {
        List<permissions> result = dao.getAllForAdElement(AD_ELEMENT_ID, PRIVILEGED_USER_ID, true);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that no permissions are returned, for a user without permissions.
     */
    @Test
    public void testGetAllForAdElementFilteredWithNoPermissions() {
        List<permissions> result = dao.getAllForAdElement(AD_ELEMENT_ID, UNPRIVILEGED_USER_ID, true);

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementOnlyWithInvalidId() {
        List<permissions> result = dao.getAllDirectPermissionsForAdElement(Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that the right set of permissions are returned.
     */
    @Test
    public void testGetAllForAdElementOnly() {
        List<permissions> result = dao.getAllDirectPermissionsForAdElement(AD_ELEMENT_ID);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForEntityWithInvalidId() {
        List<permissions> result = dao.getAllForEntity(Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Asserts that the result of get for AD element is correct
     * @param result The result to check
     */
    private static void assertValidGetByAdElement(List<permissions> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(AD_ELEMENT_ID, permission.getad_element_id());

        }
    }

    /**
     * Asserts that the result of get for AD element with no permissions is indeed empty
     * @param result result The result to check
     */
    private static void assertInvalidGetByAdElement(List<permissions> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right permissions are returned for the specified id.
     */
    @Test
    public void testGetAllForEntity() {
        List<permissions> result = dao.getAllForEntity(VM_ENTITY_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(VM_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForRoleWithInvalidRole() {
        List<permissions> result = dao.getAllForRole(Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that a collection of permissions are returned.
     */
    @Test
    public void testGetAllForRole() {
        List<permissions> result = dao.getAllForRole(ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(ROLE_ID, permission.getrole_id());
        }
    }

    /**
     * Ensures that an empty collection is returned if the specified role doesn't have any permissions with the
     * specified ad element.
     */
    @Test
    public void testGetAllForRoleAndAdElementWithInvalidRole() {
        List<permissions> result = dao.getAllForRoleAndAdElement(
                Guid.NewGuid(), AD_ELEMENT_ID);

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that an empty collection is returned if the specified element doesn't have any permissions with the
     * specified role.
     */
    @Test
    public void testGetAllForRoleAndAdElementWithInvalidElement() {
        List<permissions> result = dao.getAllForRoleAndAdElement(ROLE_ID,
                Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures a collection is returned.
     */
    @Test
    public void testGetAllForRoleAndAdElement() {
        List<permissions> result = dao.getAllForRoleAndAdElement(ROLE_ID,
                AD_ELEMENT_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(ROLE_ID, permission.getrole_id());
            assertEquals(AD_ELEMENT_ID, permission.getad_element_id());
        }
    }

    /**
     * Ensures an empty collection is returned when the specified object and role don't have permissions.
     */
    @Test
    public void testGetAllForRoleAndObjectWithInvalidRole() {
        List<permissions> result = dao.getAllForRoleAndObject(Guid.NewGuid(),
                VM_ENTITY_ID);

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures an empty collection is returned when the specified object and role don't have permissions.
     */
    @Test
    public void testGetAllForRoleAndObjectWithInvalidEntity() {
        List<permissions> result = dao.getAllForRoleAndObject(ROLE_ID,
                Guid.NewGuid());

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that the correct collection is returned.
     */
    @Test
    public void testGetAllForRoleAndObject() {
        List<permissions> result = dao.getAllForRoleAndObject(ROLE_ID, VM_ENTITY_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(ROLE_ID, permission.getrole_id());
            assertEquals(VM_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures an empty collection is returned when the entity has no permissions tree.
     */
    @Test
    public void testGetTreeForEntityWithInvalidEntity() {
        List<permissions> result = dao.getTreeForEntity(Guid.NewGuid(),
                VdcObjectType.AdElements);

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures that the type must match to get the right permissions.
     */
    @Test
    public void testGetTreeForEntityWithIncorrectType() {
        List<permissions> result = dao.getTreeForEntity(VM_ENTITY_ID,
                VdcObjectType.Bookmarks);

        assertInvalidGetByAdElement(result);
    }

    /**
     * Ensures the right list of permissions are returned for a given entity and type.
     */
    @Test
    public void testGetTreeForEntityWithVmType() {
        List<permissions> result = dao.getTreeForEntity(VM_ENTITY_ID,
                VdcObjectType.VM);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(VDS_GROUP_ID.equals(permission.getObjectId()) || VM_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithVdsType() {
        List<permissions> result = dao.getTreeForEntity(VDS_ENTITY_ID,
                VdcObjectType.VDS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(VDS_GROUP_ID.equals(permission.getObjectId()) || VDS_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithVmTemplateType() {
        List<permissions> result = dao.getTreeForEntity(VM_TEMPLATE_ENTITY_ID,
                VdcObjectType.VmTemplate);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(VDS_GROUP_ID.equals(permission.getObjectId())
                    || VM_TEMPLATE_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithVmPoolType() {
        List<permissions> result = dao.getTreeForEntity(VM_POOL_ENTITY_ID,
                VdcObjectType.VmPool);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(VDS_GROUP_ID.equals(permission.getObjectId())
                    || VM_POOL_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithClusterType() {
        List<permissions> result = dao.getTreeForEntity(CLUSTER_ENTITY_ID,
                VdcObjectType.VdsGroups);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(VDS_GROUP_ID.equals(permission.getObjectId())
                    || CLUSTER_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithSystemType() {
        List<permissions> result = dao.getTreeForEntity(SYSTEM_ENTITY_ID,
                VdcObjectType.System);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(SYSTEM_ENTITY_ID.equals(permission.getObjectId()));
        }
    }

    @Test
    public void testGetTreeForEntityWithStoragePoolType() {
        List<permissions> result = dao.getTreeForEntity(STORAGE_POOL_ENTITY_ID,
                VdcObjectType.StoragePool);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(STORAGE_POOL_ENTITY_ID, permission.getObjectId());
        }
    }

    @Test
    public void testGetTreeForEntityWithStorageType() {
        List<permissions> result = dao.getTreeForEntity(STORAGE_ENTITY_ID,
                VdcObjectType.Storage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(STORAGE_ENTITY_ID, permission.getObjectId());
        }
    }

    @Test
    public void testGetTreeForEntityWithUserType() {
        List<permissions> result = dao.getTreeForEntity(USER_ENTITY_ID,
                VdcObjectType.User);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(USER_ENTITY_ID, permission.getObjectId());
        }
    }

    @Test
    public void testGetTreeForEntityWithRoleType() {
        List<permissions> result = dao.getTreeForEntity(ROLE_ENTITY_ID,
                VdcObjectType.Role);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertEquals(ROLE_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures that saving a permission works as expected.
     */
    @Test
    public void testSave() {
        dao.save(new_permissions);

        permissions result = dao.getForRoleAndAdElementAndObject(
                new_permissions.getrole_id(),
                new_permissions.getad_element_id(),
                new_permissions.getObjectId());

        assertNotNull(result);
        assertEquals(new_permissions, result);
    }

    /**
     * Ensures that remove works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(EXISTING_PERMISSION_ID);

        permissions result = dao.get(EXISTING_PERMISSION_ID);
        assertNull(result);
    }

    /**
     * Ensures that all permissions for the specified entity are removed.
     */
    @Test
    public void testRemoveForEntity() {
        List<permissions> before = dao.getAllForEntity(VM_ENTITY_ID);

        // make sure we have some actual data to work with
        assertFalse(before.isEmpty());

        dao.removeForEntity(VM_ENTITY_ID);

        List<permissions> after = dao.getAllForEntity(VM_ENTITY_ID);

        assertTrue(after.isEmpty());
    }
}
