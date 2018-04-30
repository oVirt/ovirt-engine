package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ActionGroupDao;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetRoleActionGroupsByRoleIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetRoleActionGroupsByRoleIdQuery<IdQueryParameters>> {
    @Mock
    private ActionGroupDao actionGroupDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Mock parameters
        Guid roleId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(roleId);

        // Mock the expected result
        ActionGroup group = RandomUtils.instance().nextEnum(ActionGroup.class);
        List<ActionGroup> expected = Collections.singletonList(group);

        // Mock the Dao
        when(actionGroupDaoMock.getAllForRole(roleId)).thenReturn(expected);

        // Execute the query and assert the result
        getQuery().executeQueryCommand();

        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong query result");
    }
}
