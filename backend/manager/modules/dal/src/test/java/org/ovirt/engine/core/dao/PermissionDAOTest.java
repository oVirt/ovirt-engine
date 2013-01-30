package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final Guid STORAGE_ENTITY_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid USER_ENTITY_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid ROLE_ENTITY_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");
    private static final Guid VDS_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private PermissionDAO dao;
    private permissions new_permissions;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getPermissionDao();

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
    public void testGetAllForQuotaIdWithNoPermissions() {
        List<permissions> result = dao.getConsumedPermissionsForQuotaId(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForQuotaId() {
        List<permissions> result = dao.getConsumedPermissionsForQuotaId(FixturesTool.QUOTA_GENERAL);
        assertEquals(result.get(0).getad_element_id(), FixturesTool.USER_EXISTING_ID);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementWithInvalidId() {
        List<permissions> result = dao.getAllForAdElement(Guid.NewGuid());

        assertInvalidGetPermissionList(result);
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

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementOnlyWithInvalidId() {
        List<permissions> result = dao.getAllDirectPermissionsForAdElement(Guid.NewGuid());

        assertInvalidGetPermissionList(result);
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

        assertInvalidGetPermissionList(result);
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
     * Ensures that the right permissions are returned for the specified id.
     */
    @Test
    public void testGetAllForEntity() {
        List<permissions> result = dao.getAllForEntity(VM_ENTITY_ID);
        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures that the right permissions are returned for the specified id., for a user with permissions.
     */
    @Test
    public void testGetAllForEntityFilteredWithPermissions() {
        List<permissions> result = dao.getAllForEntity(VM_ENTITY_ID, PRIVILEGED_USER_ID, true);

        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures that the right permissions are returned for the specified id., for a user without permissions but with the filtering mechanism disabled.
     */
    @Test
    public void testGetAllForEntityFilteredWithNoPermissionsFilteringDisabled() {
        List<permissions> result = dao.getAllForEntity(VM_ENTITY_ID, UNPRIVILEGED_USER_ID, false);

        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures that the right permissions are returned for the specified id., for a user without permissions.
     */
    @Test
    public void testGetAllForEntityFilteredWithNoPermissions() {
        List<permissions> result = dao.getAllForEntity(VM_ENTITY_ID, UNPRIVILEGED_USER_ID, true);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures a call to {@link PermissionDAO#getAllForEntity(Guid)} works properly
     * @param result
     */
    private static void assertGetAllForEntityResult(List<permissions> result) {
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

        assertInvalidGetPermissionList(result);
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

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that an empty collection is returned if the specified element doesn't have any permissions with the
     * specified role.
     */
    @Test
    public void testGetAllForRoleAndAdElementWithInvalidElement() {
        List<permissions> result = dao.getAllForRoleAndAdElement(ROLE_ID,
                Guid.NewGuid());

        assertInvalidGetPermissionList(result);
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

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures an empty collection is returned when the specified object and role don't have permissions.
     */
    @Test
    public void testGetAllForRoleAndObjectWithInvalidEntity() {
        List<permissions> result = dao.getAllForRoleAndObject(ROLE_ID,
                Guid.NewGuid());

        assertInvalidGetPermissionList(result);
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

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that the type must match to get the right permissions.
     */
    @Test
    public void testGetTreeForEntityWithIncorrectType() {
        List<permissions> result = dao.getTreeForEntity(VM_ENTITY_ID,
                VdcObjectType.Bookmarks);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures the right list of permissions are returned for a given entity and type.
     */
    @Test
    public void testGetTreeForEntityWithVmType() {
        baseTestGetTreeForEntity(VM_ENTITY_ID, VdcObjectType.VM, VDS_GROUP_ID);
    }

    @Test
    public void testGetTreeForEntityWithVdsType() {
        baseTestGetTreeForEntity(VDS_ENTITY_ID, VdcObjectType.VDS, VDS_GROUP_ID);
    }

    @Test
    public void testGetTreeForEntityWithVmTemplateType() {
        baseTestGetTreeForEntity(VM_TEMPLATE_ENTITY_ID, VdcObjectType.VmTemplate, VDS_GROUP_ID);
    }

    @Test
    public void testGetTreeForEntityWithVmPoolType() {
        baseTestGetTreeForEntity(VM_POOL_ENTITY_ID, VdcObjectType.VmPool, VDS_GROUP_ID);
    }

    @Test
    public void testGetTreeForEntityWithClusterType() {
        baseTestGetTreeForEntity(CLUSTER_ENTITY_ID, VdcObjectType.VdsGroups, VDS_GROUP_ID);
    }

    @Test
    public void testGetTreeForEntityWithSystemType() {
        baseTestGetTreeForEntity(SYSTEM_ENTITY_ID, VdcObjectType.System);
    }

    @Test
    public void testGetTreeForEntityWithStoragePoolType() {
        baseTestGetTreeForEntity(STORAGE_POOL_ENTITY_ID, VdcObjectType.StoragePool);
    }

    @Test
    public void testGetTreeForEntityWithStorageType() {
        baseTestGetTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage);
    }

    @Test
    public void testGetTreeForEntityWithUserType() {
        baseTestGetTreeForEntity(USER_ENTITY_ID, VdcObjectType.User);
    }

    @Test
    public void testGetTreeForEntityWithRoleType() {
        baseTestGetTreeForEntity(ROLE_ENTITY_ID, VdcObjectType.Role);
    }

    @Test
    public void testGetTreeForEntityWithRoleTypeFilteredWithPermissions() {
        baseTestGetTreeForEntityFiltered(STORAGE_ENTITY_ID, VdcObjectType.Storage, PRIVILEGED_USER_ID, true);
    }

    @Test
    public void testGetTreeForEntityWithRoleTypeFilteredWithNoPermissionsCheckDisabled() {
        baseTestGetTreeForEntityFiltered(STORAGE_ENTITY_ID, VdcObjectType.Storage, UNPRIVILEGED_USER_ID, false);
    }

    @Test
    public void testGetTreeForEntityWithRoleTypeFilteredWithNoPermissions() {
        List<permissions> result =
                dao.getTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage, UNPRIVILEGED_USER_ID, true);
        assertInvalidGetPermissionList(result);
    }

    /**
     * Tests {@link PermissionDAO#getTreeForEntity(Guid, VdcObjectType)}
     * @param entityID The object to retrieve tree for
     * @param objectType The type of {@link #entityID}
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    private void baseTestGetTreeForEntity(Guid entityID, VdcObjectType objectType, Guid... alternativeObjectIds) {
        List<permissions> result = dao.getTreeForEntity(entityID, objectType);

        assertGetTreeForEntityResult(entityID, result, alternativeObjectIds);
    }

    /**
     * Tests {@link PermissionDAO#getTreeForEntity(Guid, VdcObjectType, Guid, boolean))}
     * @param entityID The object to retrieve tree for
     * @param objectType The type of {@link #entityID}
     * @param userID The user to use
     * @param isFiltered are the results filtered or not
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    private void baseTestGetTreeForEntityFiltered(Guid entityID,
            VdcObjectType objectType,
            Guid userID,
            boolean isFiltered,
            Guid... alternativeObjectIds) {
        List<permissions> result = dao.getTreeForEntity(entityID, objectType, userID, isFiltered);

        assertGetTreeForEntityResult(entityID, result, alternativeObjectIds);
    }

    /**
     * asserts the result of a call to {@link PermissionDAO#getTreeForEntity(Guid, VdcObjectType)}
     * @param entityID The object to retrieve tree for
     * @param objectType The type of {@link #entityID}
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    protected void assertGetTreeForEntityResult(Guid entityID, List<permissions> result, Guid... alternativeObjectIds) {
        Set<Guid> expectedObjectIds = new HashSet<Guid>();
        expectedObjectIds.add(entityID);
        expectedObjectIds.addAll(Arrays.asList(alternativeObjectIds));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (permissions permission : result) {
            assertTrue(expectedObjectIds.contains(permission.getObjectId()));
        }
    }

    /**
     * Asserts that the result of a getXXX call that should return no permissions is empty as expected
     * @param result result The result to check
     */
    private static void assertInvalidGetPermissionList(List<permissions> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
