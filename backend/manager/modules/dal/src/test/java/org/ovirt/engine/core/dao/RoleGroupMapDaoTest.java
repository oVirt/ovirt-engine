package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMapDaoTest extends BaseDaoTestCase<RoleGroupMapDao> {
    private RoleGroupMap newRoleGroupMap;
    private ActionGroup actionGroup;
    private RoleGroupMap existingRoleGroupMap;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingRoleGroupMap = dao.getByActionGroupAndRole(ActionGroup.RUN_VM, FixturesTool.ROLE_ID);

        actionGroup = ActionGroup.CONNECT_TO_VM;
        newRoleGroupMap = new RoleGroupMap(actionGroup, FixturesTool.ROLE_ID);
    }

    @Test
    public void testGetByActionGroupAndRole() {
        RoleGroupMap result =
                dao.getByActionGroupAndRole(existingRoleGroupMap.getActionGroup(), existingRoleGroupMap.getRoleId());

        assertNotNull(result);
        assertEquals(existingRoleGroupMap, result);
    }

    /**
     * Ensures an empty collection is returned when the role is invalid.
     */
    @Test
    public void testGetAllRoleGroupMapsWithInvalidRole() {
        List<RoleGroupMap> result = dao.getAllForRole(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right mappings are returned.
     */
    @Test
    public void testGetAllRoleGroupMaps() {
        List<RoleGroupMap> result = dao.getAllForRole(FixturesTool.ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (RoleGroupMap map : result) {
            assertEquals(FixturesTool.ROLE_ID, map.getRoleId());
        }
    }

    /**
     * Ensures saving such a mapping works as expected.
     */
    @Test
    public void testSaveRoleGroupMap() {
        dao.save(newRoleGroupMap);

        List<RoleGroupMap> result = dao.getAllForRole(newRoleGroupMap.getRoleId());
        boolean worked = false;

        for (RoleGroupMap map : result) {
            worked |= newRoleGroupMap.equals(map);
        }
        assertTrue(worked);
    }

    /**
     * Ensures deleting a mapping works as expected.
     */
    @Test
    public void testRemoveRoleGroupMap() {
        List<RoleGroupMap> before = dao.getAllForRole(FixturesTool.ROLE_ID);

        // make sure we have things to delete first
        assertFalse(before.isEmpty());

        for (RoleGroupMap map : before) {
            dao.remove(map.getActionGroup(), map.getRoleId());
        }

        List<RoleGroupMap> after = dao.getAllForRole(FixturesTool.ROLE_ID);

        assertTrue(after.isEmpty());
    }
}
