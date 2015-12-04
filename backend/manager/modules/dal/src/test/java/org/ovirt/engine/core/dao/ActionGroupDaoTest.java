package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
import org.ovirt.engine.core.compat.Guid;

public class ActionGroupDaoTest extends BaseDaoTestCase {
    private static final int ACTION_GROUP_COUNT = 3;
    private static final int ACTION_VERSION_MAP_COUNT = 2;
    private static final Guid EXISTING_ROLE_ID = new Guid("f5972bfa-7102-4d33-ad22-9dd421bfba78");
    private ActionGroupDao dao;
    private ActionVersionMap existingActionMap;
    private ActionVersionMap newActionMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getActionGroupDao();
        existingActionMap = dao.getActionVersionMapByActionType(VdcActionType.AddVm);
        newActionMap = new ActionVersionMap();
        newActionMap.setActionType(VdcActionType.ActivateStorageDomain);
        newActionMap.setClusterMinimalVersion("3.0");
        newActionMap.setStoragePoolMinimalVersion("3.0");
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
        List<ActionVersionMap> result = dao.getAllActionVersionMap();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(ACTION_VERSION_MAP_COUNT, result.size());
    }

    @Test
    public void testGetActionVersionMapByType() {
        ActionVersionMap result = dao.getActionVersionMapByActionType(existingActionMap.getActionType());

        assertNotNull(result);
        assertEquals(existingActionMap, result);
    }

    @Test
    public void testAddActionVersionMap() {
        dao.addActionVersionMap(newActionMap);

        ActionVersionMap result = dao.getActionVersionMapByActionType(newActionMap.getActionType());

        assertNotNull(result);
        assertEquals(newActionMap, result);
    }

    @Test
    public void testRemoveActionVersionMap() {
        ActionVersionMap dummy = new ActionVersionMap();
        dummy.setActionType(VdcActionType.RebootVm);
        dummy.setClusterMinimalVersion("3.0");
        dummy.setStoragePoolMinimalVersion("3.0");
        dao.addActionVersionMap(dummy);

        ActionVersionMap result = dao.getActionVersionMapByActionType(dummy.getActionType());
        assertNotNull(result);

        dao.removeActionVersionMap(dummy.getActionType());

        result = dao.getActionVersionMapByActionType(dummy.getActionType());
        assertNull(result);
    }
}
