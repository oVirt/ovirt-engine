package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class ActionGroupDaoTest extends BaseDaoTestCase<ActionGroupDao> {
    private static final int ACTION_GROUP_COUNT = 3;

    @Test
    public void testGetAllActionGroupsForRole() {
        List<ActionGroup> result = dao.getAllForRole(FixturesTool.ROLE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(ACTION_GROUP_COUNT, result.size());
    }
}
