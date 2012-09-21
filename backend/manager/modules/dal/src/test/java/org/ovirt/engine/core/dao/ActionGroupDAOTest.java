package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.compat.Guid;

public class ActionGroupDAOTest extends BaseDAOTestCase {
    private static final int ACTION_GROUP_COUNT = 2;
    private static final int ACTION_VERSION_MAP_COUNT = 2;
    private static final Guid EXISTING_ROLE_ID = new Guid("f5972bfa-7102-4d33-ad22-9dd421bfba78");
    private ActionGroupDAO dao;
    private action_version_map existingActionMap;
    private action_version_map newActionMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getActionGroupDao());
        existingActionMap = dao.getActionVersionMapByActionType(VdcActionType.AddVm);
        newActionMap = new action_version_map(VdcActionType.ActivateStorageDomain, "3.0", "3.0");
    }

    @Test
    public void testGetAllActionGroupsForRole() {
        List<ActionGroup> result = dao.getAllForRole(EXISTING_ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(ACTION_GROUP_COUNT, result.size());
    }

    @Test
    public void testGetAllActionVersionMap() {
        List<action_version_map> result = dao.getAllActionVersionMap();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(ACTION_VERSION_MAP_COUNT, result.size());
    }

    @Test
    public void testGetActionVersionMapByType() {
        action_version_map result = dao.getActionVersionMapByActionType(existingActionMap.getaction_type());

        assertNotNull(result);
        assertEquals(existingActionMap, result);
    }

    @Test
    public void testAddActionVersionMap() {
        dao.addActionVersionMap(newActionMap);

        action_version_map result = dao.getActionVersionMapByActionType(newActionMap.getaction_type());

        assertNotNull(result);
        assertEquals(newActionMap, result);
    }

    @Test
    public void testRemoveActionVersionMap() {
        dao.removeActionVersionMap(existingActionMap.getaction_type());

        action_version_map result = dao.getActionVersionMapByActionType(existingActionMap.getaction_type());

        assertNull(result);
    }
}
