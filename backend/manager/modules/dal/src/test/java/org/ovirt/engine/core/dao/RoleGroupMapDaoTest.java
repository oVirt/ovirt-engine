package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMapDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_ROLE_ID = new Guid("f5972bfa-7102-4d33-ad22-9dd421bfba78");
    private RoleGroupMapDao dao;
    private RoleGroupMap newRoleGroupMap;
    private ActionGroup actionGroup;
    private RoleGroupMap existingRoleGroupMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getRoleGroupMapDao();

        existingRoleGroupMap = dao.getByActionGroupAndRole(ActionGroup.RUN_VM, EXISTING_ROLE_ID);

        actionGroup = ActionGroup.CONNECT_TO_VM;
        newRoleGroupMap = new RoleGroupMap(actionGroup, EXISTING_ROLE_ID);
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
        List<RoleGroupMap> result = dao.getAllForRole(EXISTING_ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (RoleGroupMap map : result) {
            assertEquals(EXISTING_ROLE_ID, map.getRoleId());
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
        List<RoleGroupMap> before = dao.getAllForRole(EXISTING_ROLE_ID);

        // make sure we have things to delete first
        assertFalse(before.isEmpty());

        for (RoleGroupMap map : before) {
            dao.remove(map.getActionGroup(), map.getRoleId());
        }

        List<RoleGroupMap> after = dao.getAllForRole(EXISTING_ROLE_ID);

        assertTrue(after.isEmpty());
    }
}
