package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class PermissionDaoTest extends BaseDaoTestCase<PermissionDao> {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.ApplicationMode, 255));
    }

    private static final Guid ROLE_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");
    private static final Guid AD_ELEMENT_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final Guid VM_ENTITY_ID = FixturesTool.VM_RHEL5_POOL_50;
    private static final Guid EXISTING_PERMISSION_ID = new Guid("9304ce01-2f5f-41b5-92c7-9d69ef0bcfbc");
    private static final Guid VM_TEMPLATE_ENTITY_ID = FixturesTool.VM_TEMPLATE_RHEL5;
    private static final Guid VM_POOL_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");
    private static final Guid SYSTEM_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0077");
    private static final Guid STORAGE_POOL_ENTITY_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0078");
    private static final Guid STORAGE_ENTITY_ID = FixturesTool.STORAGE_DOMAIN_SCALE_SD5;
    private static final Guid USER_ENTITY_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid ROLE_ENTITY_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");

    private static final Guid DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS =
            new Guid("88D4301A-17AF-496C-A793-584640853D4B");

    private Permission new_permissions;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        new_permissions = new Permission(AD_ELEMENT_ID, ROLE_ID, FixturesTool.DATA_CENTER,
                VdcObjectType.StoragePool);
    }

    /**
     * Ensures that getting a permission with a bad id fails.
     */
    @Test
    public void testGetWithInvalidId() {
        Permission result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right permission is retrieved.
     */
    @Test
    public void testGet() {
        Permission result = dao.get(EXISTING_PERMISSION_ID);

        assertNotNull(result);
        assertEquals(EXISTING_PERMISSION_ID, result.getId());
    }

    /**
     * Ensures that a null object is returned when the role is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidRole() {
        Permission result = dao.getForRoleAndAdElementAndObject(
                Guid.newGuid(), AD_ELEMENT_ID, VM_ENTITY_ID);

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the element is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidAdElement() {
        Permission result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                Guid.newGuid(), VM_ENTITY_ID);

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the object is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObjectWithInvalidObject() {
        Permission result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                AD_ELEMENT_ID, Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that a null object is returned when the role is invalid.
     */
    @Test
    public void testGetAllForRoleAndAdElementAndObject() {
        Permission result = dao.getForRoleAndAdElementAndObject(ROLE_ID,
                AD_ELEMENT_ID, VM_ENTITY_ID);

        assertNotNull(result);
        assertEquals(ROLE_ID, result.getRoleId());
        assertEquals(AD_ELEMENT_ID, result.getAdElementId());
        assertEquals(VM_ENTITY_ID, result.getObjectId());
    }

    @Test
    public void testGetAllForQuotaIdWithNoPermissions() {
        List<Permission> result = dao.getConsumedPermissionsForQuotaId(FixturesTool.QUOTA_SPECIFIC_AND_GENERAL);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForQuotaId() {
        List<Permission> result = dao.getConsumedPermissionsForQuotaId(FixturesTool.QUOTA_GENERAL);
        assertEquals(FixturesTool.USER_EXISTING_ID, result.get(0).getAdElementId());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementWithInvalidId() {
        List<Permission> result = dao.getAllForAdElement(Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that the right set of permissions are returned.
     */
    @Test
    public void testGetAllForAdElement() {
        List<Permission> result = dao.getAllForAdElement(AD_ELEMENT_ID);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that the right set of permissions are returned, for a user with permissions.
     */
    @Test
    public void testGetAllForAdElementFilteredWithPermissions() {
        List<Permission> result = dao.getAllForAdElement(AD_ELEMENT_ID, FixturesTool.PRIVILEGED_SESSION_ID, true);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that no permissions are returned, for a user without permissions.
     */
    @Test
    public void testGetAllForAdElementFilteredWithNoPermissions() {
        List<Permission> result = dao.getAllForAdElement(AD_ELEMENT_ID, FixturesTool.UNPRIVILEGED_SESSION_ID, true);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForAdElementOnlyWithInvalidId() {
        List<Permission> result = dao.getAllDirectPermissionsForAdElement(Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that the right set of permissions are returned.
     */
    @Test
    public void testGetAllForAdElementOnly() {
        List<Permission> result = dao.getAllDirectPermissionsForAdElement(AD_ELEMENT_ID);

        assertValidGetByAdElement(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForEntityWithInvalidId() {
        List<Permission> result = dao.getAllForEntity(Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Asserts that the result of get for AD element is correct
     * @param result The result to check
     */
    private static void assertValidGetByAdElement(List<Permission> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(AD_ELEMENT_ID, permission.getAdElementId());

        }
    }

    /**
     * Ensures that the right permissions are returned for the specified id.
     */
    @Test
    public void testGetAllForEntity() {
        List<Permission> result = dao.getAllForEntity(VM_ENTITY_ID);
        assertGetAllForEntityResult(result);
    }
    /**
     * Ensures that the right permissions are returned for the specified id., for a user with permissions.
     */
    @Test
    public void testGetAllForEntityFilteredWithPermissions() {
        List<Permission> result = dao.getAllForEntity(VM_ENTITY_ID, FixturesTool.PRIVILEGED_SESSION_ID, true);

        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures that the right permissions are returned for the specified id., for a user without permissions but with the filtering mechanism disabled.
     */
    @Test
    public void testGetAllForEntityFilteredWithNoPermissionsFilteringDisabled() {
        List<Permission> result = dao.getAllForEntity(VM_ENTITY_ID, FixturesTool.UNPRIVILEGED_SESSION_ID, false);

        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures that the right permissions are returned for the specified id., for a user without permissions.
     */
    @Test
    public void testGetAllForEntityFilteredWithNoPermissions() {
        List<Permission> result = dao.getAllForEntity(VM_ENTITY_ID, FixturesTool.UNPRIVILEGED_SESSION_ID, true);

        assertInvalidGetPermissionList(result);
    }

    @Test
    public void testGetAllUsersWithPermissionsOnEntity() {
        List<Permission> result = dao.getAllForEntity(VM_ENTITY_ID, FixturesTool.PRIVILEGED_SESSION_ID, true, true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertGetAllForEntityResult(result);
    }

    /**
     * Ensures a call to {@link PermissionDao#getAllForEntity(Guid)} works properly
     */
    private static void assertGetAllForEntityResult(List<Permission> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(VM_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForRoleWithInvalidRole() {
        List<Permission> result = dao.getAllForRole(Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that a collection of permissions are returned.
     */
    @Test
    public void testGetAllForRole() {
        List<Permission> result = dao.getAllForRole(ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(ROLE_ID, permission.getRoleId());
        }
    }

    /**
     * Ensures that an empty collection is returned if the specified role doesn't have any permissions with the
     * specified ad element.
     */
    @Test
    public void testGetAllForRoleAndAdElementWithInvalidRole() {
        List<Permission> result = dao.getAllForRoleAndAdElement(
                Guid.newGuid(), AD_ELEMENT_ID);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that an empty collection is returned if the specified element doesn't have any permissions with the
     * specified role.
     */
    @Test
    public void testGetAllForRoleAndAdElementWithInvalidElement() {
        List<Permission> result = dao.getAllForRoleAndAdElement(ROLE_ID,
                Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures a collection is returned.
     */
    @Test
    public void testGetAllForRoleAndAdElement() {
        List<Permission> result = dao.getAllForRoleAndAdElement(ROLE_ID,
                AD_ELEMENT_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(ROLE_ID, permission.getRoleId());
            assertEquals(AD_ELEMENT_ID, permission.getAdElementId());
        }
    }

    /**
     * Ensures an empty collection is returned when the specified object and role don't have permissions.
     */
    @Test
    public void testGetAllForRoleAndObjectWithInvalidRole() {
        List<Permission> result = dao.getAllForRoleAndObject(Guid.newGuid(),
                VM_ENTITY_ID);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures an empty collection is returned when the specified object and role don't have permissions.
     */
    @Test
    public void testGetAllForRoleAndObjectWithInvalidEntity() {
        List<Permission> result = dao.getAllForRoleAndObject(ROLE_ID,
                Guid.newGuid());

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that the correct collection is returned.
     */
    @Test
    public void testGetAllForRoleAndObject() {
        List<Permission> result = dao.getAllForRoleAndObject(ROLE_ID, VM_ENTITY_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(ROLE_ID, permission.getRoleId());
            assertEquals(VM_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures that the correct collection is returned.
     */
    @Test
    public void testGetAllForAdElementAndObject() {
        List<Permission> result = dao.getAllForAdElementAndObjectId(AD_ELEMENT_ID, VM_ENTITY_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertEquals(AD_ELEMENT_ID, permission.getAdElementId());
            assertEquals(VM_ENTITY_ID, permission.getObjectId());
        }
    }

    /**
     * Ensures an empty collection is returned when the entity has no permissions tree.
     */
    @Test
    public void testGetTreeForEntityWithInvalidEntity() {
        List<Permission> result = dao.getTreeForEntity(Guid.newGuid(),
                VdcObjectType.AdElements);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures that the type must match to get the right permissions.
     */
    @Test
    public void testGetTreeForEntityWithIncorrectType() {
        List<Permission> result = dao.getTreeForEntity(VM_ENTITY_ID,
                VdcObjectType.Bookmarks);

        assertInvalidGetPermissionList(result);
    }

    /**
     * Ensures the right list of permissions are returned for a given entity and type.
     */
    @Test
    public void testGetTreeForEntityWithVmType() {
        baseTestGetTreeForEntity(VM_ENTITY_ID, VdcObjectType.VM, FixturesTool.CLUSTER);
    }

    @Test
    public void testGetTreeForEntityWithVdsType() {
        baseTestGetTreeForEntity(FixturesTool.VDS_RHEL6_NFS_SPM, VdcObjectType.VDS, FixturesTool.CLUSTER);
    }

    @Test
    public void testGetTreeForEntityWithVmTemplateType() {
        baseTestGetTreeForEntity(VM_TEMPLATE_ENTITY_ID, VdcObjectType.VmTemplate, FixturesTool.CLUSTER);
    }

    @Test
    public void testGetTreeForEntityWithVmPoolType() {
        baseTestGetTreeForEntity(VM_POOL_ENTITY_ID, VdcObjectType.VmPool, FixturesTool.CLUSTER);
    }

    @Test
    public void testGetTreeForEntityWithClusterType() {
        baseTestGetTreeForEntity(FixturesTool.CLUSTER, VdcObjectType.Cluster, FixturesTool.CLUSTER);
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
        baseTestGetTreeForEntityFiltered(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.PRIVILEGED_SESSION_ID, true);
    }

    @Test
    public void testGetTreeForEntityWithRoleTypeFilteredWithNoPermissionsCheckDisabled() {
        baseTestGetTreeForEntityFiltered(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.UNPRIVILEGED_SESSION_ID, false);
    }

    @Test
    public void testGetTreeForEntityWithRoleTypeFilteredWithNoPermissions() {
        List<Permission> result =
                dao.getTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.UNPRIVILEGED_SESSION_ID, true);
        assertInvalidGetPermissionList(result);
    }
    @Test
    public void testGetTreeForEntityWithAppMode() {
        List<Permission> result = dao.getTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.PRIVILEGED_SESSION_ID, true, ApplicationMode.AllModes.getValue());
        assertEquals(1, result.size());

        List<Permission> result2 = dao.getTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.PRIVILEGED_SESSION_ID, true, ApplicationMode.VirtOnly.getValue());
        assertEquals(1, result2.size());

        List<Permission> result3 = dao.getTreeForEntity(STORAGE_ENTITY_ID, VdcObjectType.Storage, FixturesTool.PRIVILEGED_SESSION_ID, true, ApplicationMode.GlusterOnly.getValue());
        assertEquals(1, result3.size());
    }
    /**
     * Tests {@link PermissionDao#getTreeForEntity(Guid, VdcObjectType)}
     * @param entityID The object to retrieve tree for
     * @param objectType The type of {@code entityID}
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    private void baseTestGetTreeForEntity(Guid entityID, VdcObjectType objectType, Guid... alternativeObjectIds) {
        List<Permission> result = dao.getTreeForEntity(entityID, objectType);

        assertGetTreeForEntityResult(entityID, result, alternativeObjectIds);
    }

    /**
     * Tests {@link PermissionDao#getTreeForEntity(Guid, VdcObjectType, long, boolean)}
     * @param entityID The object to retrieve tree for
     * @param objectType The type of {@code entityID}
     * @param sessionId The session to use
     * @param isFiltered are the results filtered or not
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    private void baseTestGetTreeForEntityFiltered(Guid entityID,
            VdcObjectType objectType,
            long sessionId,
            boolean isFiltered,
            Guid... alternativeObjectIds) {
        List<Permission> result = dao.getTreeForEntity(entityID, objectType, sessionId, isFiltered);

        assertGetTreeForEntityResult(entityID, result, alternativeObjectIds);
    }

    /**
     * asserts the result of a call to {@link PermissionDao#getTreeForEntity(Guid, VdcObjectType)}
     * @param entityID The object to retrieve tree for
     * @param alternativeObjectIds Additional object IDs that are allowed in the resulting permissions
     */
    protected void assertGetTreeForEntityResult(Guid entityID, List<Permission> result, Guid... alternativeObjectIds) {
        Set<Guid> expectedObjectIds = new HashSet<>();
        expectedObjectIds.add(entityID);
        expectedObjectIds.addAll(Arrays.asList(alternativeObjectIds));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Permission permission : result) {
            assertTrue(expectedObjectIds.contains(permission.getObjectId()));
        }
    }

    /**
     * Asserts that the result of a getXXX call that should return no permissions is empty as expected
     * @param result result The result to check
     */
    private static void assertInvalidGetPermissionList(List<Permission> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEntityPermissions() {
        // Should not return null since the user has the relevant permission
        assertNotNull(dao.getEntityPermissions(DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS,
                ActionGroup.RUN_VM,
                VM_TEMPLATE_ENTITY_ID,
                VdcObjectType.VM));

        // Should return null since the user does not has the relevant permission
        assertNull(dao.getEntityPermissions(DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS,
                ActionGroup.CREATE_TEMPLATE,
                VM_TEMPLATE_ENTITY_ID,
                VdcObjectType.VM));
    }

    @Test
    public void testGetEntityPermissionsByUserAndGroups() {
        // Should not return null since the user has the relevant permission
        assertNotNull(dao.getEntityPermissionsForUserAndGroups(Guid.newGuid(),
                DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS.toString(),
                ActionGroup.RUN_VM,
                VM_TEMPLATE_ENTITY_ID,
                VdcObjectType.VM,
                false));

        // Should return null since the user does not has the relevant permission
        assertNull(dao.getEntityPermissionsForUserAndGroups(Guid.newGuid(),
                DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS.toString(),
                ActionGroup.CREATE_TEMPLATE,
                VM_TEMPLATE_ENTITY_ID,
                VdcObjectType.VM,
                false));
    }

    /**
     * Ensures that saving a permission works as expected.
     */
    @Test
    public void testSave() {
        dao.save(new_permissions);

        Permission result = dao.getForRoleAndAdElementAndObject(
                new_permissions.getRoleId(),
                new_permissions.getAdElementId(),
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

        Permission result = dao.get(EXISTING_PERMISSION_ID);
        assertNull(result);
    }

    /**
     * Ensures that all permissions for the specified entity are removed.
     */
    @Test
    public void testRemoveForEntity() {
        List<Permission> before = dao.getAllForEntity(VM_ENTITY_ID);

        // make sure we have some actual data to work with
        assertFalse(before.isEmpty());

        dao.removeForEntity(VM_ENTITY_ID);

        List<Permission> after = dao.getAllForEntity(VM_ENTITY_ID);

        assertTrue(after.isEmpty());
    }

    @Test
    public void creationTimestampIsInThePast() {
        List<Permission> vmPermissions = dao.getAllForEntity(VM_ENTITY_ID);
        for (Permission perms : vmPermissions) {
            assertTrue(perms.getCreationDate() < TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        }
    }
}
