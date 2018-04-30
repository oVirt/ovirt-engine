package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class RoleDaoTest extends BaseGenericDaoTestCase<Guid, Role, RoleDao> {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.ApplicationMode, 255));
    }

    private static final String GROUP_IDS = "26df4393-659b-4b8a-b0f6-3ee94d32e82f,08963ba9-b1c8-498d-989f-75cf8142eab7";
    private static final int ROLE_COUNT = 7;
    private static final int NON_ADMIN_ROLE_COUNT = 6;

    @Override
    protected Role generateNewEntity() {
        Role newRole = new Role();
        newRole.setName("new role");
        newRole.setDescription("This is a new role.");
        newRole.setType(RoleType.USER);
        newRole.setAllowsViewingChildren(false);
        newRole.setAppMode(ApplicationMode.AllModes);
        return newRole;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription("This is an updated description");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.ROLE_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return ROLE_COUNT;
    }

    /**
     * Ensures that an invalid name results in a null role.
     */
    @Test
    public void testGetRoleByNameWithInvalidName() {
        Role result = dao.getByName("Farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a role by name works as expected.
     */
    @Test
    public void testGetRoleByName() {
        Role result = dao.getByName(existingEntity.getName());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    /**
     * Ensures the right number of non-admin roles are returned.
     */
    @Test
    public void testGetAllNonAdminRoles() {
        List<Role> result = dao.getAllNonAdminRoles();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NON_ADMIN_ROLE_COUNT, result.size());
    }

    /**
     * Ensures an admin role is returned
     */
    @Test
    public void testAnyAdminRoleForUserAndGroups() {
        List<Role> result = dao.getAnyAdminRoleForUserAndGroups(PRIVILEGED_USER_ID,
                GROUP_IDS, ApplicationMode.AllModes.getValue());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures no admin role is returned
     */
    @Test
    public void testNoAdminRoleForUserAndGroups() {
        List<Role> result = dao.getAnyAdminRoleForUserAndGroups(UNPRIVILEGED_USER_ID,
                GROUP_IDS, ApplicationMode.AllModes.getValue());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForUsersAndGroupsInvalidUserAndGroups() {
        List<Role> result = dao.getAnyAdminRoleForUserAndGroups(Guid.newGuid(),
                Guid.newGuid().toString(), ApplicationMode.AllModes.getValue());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
