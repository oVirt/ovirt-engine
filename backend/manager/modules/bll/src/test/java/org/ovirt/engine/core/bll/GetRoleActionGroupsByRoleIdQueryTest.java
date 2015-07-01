package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ActionGroupDao;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetRoleActionGroupsByRoleIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetRoleActionGroupsByRoleIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommand() {
        // Mock parameters
        Guid roleId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(roleId);

        // Mock the expected result
        ActionGroup group = RandomUtils.instance().nextEnum(ActionGroup.class);
        List<ActionGroup> expected = Collections.singletonList(group);

        // Mock the Dao
        ActionGroupDao actionGroupDaoMock = mock(ActionGroupDao.class);
        when(actionGroupDaoMock.getAllForRole(roleId)).thenReturn(expected);
        when(getDbFacadeMockInstance().getActionGroupDao()).thenReturn(actionGroupDaoMock);

        // Execute the query and assert the result
        getQuery().executeQueryCommand();

        assertEquals("Wrong query result", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
