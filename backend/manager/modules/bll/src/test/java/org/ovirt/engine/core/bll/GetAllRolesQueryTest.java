package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.RoleDao;

/** A test case for the {@link GetAllRolesQuery} class. */
public class GetAllRolesQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetAllRolesQuery<VdcQueryParametersBase>> {
    @Mock
    private RoleDao roleDaoMock;

    @Override
    protected void initQuery(GetAllRolesQuery<VdcQueryParametersBase> query) {
        super.initQuery(query);
        doReturn(Boolean.TRUE).when(query).isAdminUser();
    }

    @Test
    public void testExecuteQueryCommand() {
        // Prepare the result
        Role role = new Role();
        role.setName("test role");
        List<Role> result = Collections.singletonList(role);

        // Mock the Dao
        when(roleDaoMock.getAll()).thenReturn(result);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong roles returned", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
