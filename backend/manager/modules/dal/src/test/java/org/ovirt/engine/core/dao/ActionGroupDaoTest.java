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
        newActionMap.setaction_type(VdcActionType.ActivateStorageDomain);
        newActionMap.setcluster_minimal_version("3.0");
        newActionMap.setstorage_pool_minimal_version("3.0");
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
        ActionVersionMap result = dao.getActionVersionMapByActionType(existingActionMap.getaction_type());

        assertNotNull(result);
        assertEquals(existingActionMap, result);
    }

    @Test
    public void testAddActionVersionMap() {
        dao.addActionVersionMap(newActionMap);

        ActionVersionMap result = dao.getActionVersionMapByActionType(newActionMap.getaction_type());

        assertNotNull(result);
        assertEquals(newActionMap, result);
    }

    @Test
    public void testRemoveActionVersionMap() {
        ActionVersionMap dummy = new ActionVersionMap();
        dummy.setaction_type(VdcActionType.RebootVm);
        dummy.setcluster_minimal_version("3.0");
        dummy.setstorage_pool_minimal_version("3.0");
        dao.addActionVersionMap(dummy);

        ActionVersionMap result = dao.getActionVersionMapByActionType(dummy.getaction_type());
        assertNotNull(result);

        dao.removeActionVersionMap(dummy.getaction_type());

        result = dao.getActionVersionMapByActionType(dummy.getaction_type());
        assertNull(result);
    }
}
