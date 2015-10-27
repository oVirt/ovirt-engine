package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.dao.RoleDao;

/** A test case for the {@link GetAllRolesQuery} class. */
public class GetAllRolesQueryTest extends AbstractUserQueryTest<MultilevelAdministrationsQueriesParameters, GetAllRolesQuery<MultilevelAdministrationsQueriesParameters>> {
    @Override
    protected void initQuery(GetAllRolesQuery<MultilevelAdministrationsQueriesParameters> query) {
        doReturn(Boolean.TRUE).when(query).isAdminUser();
    }

    @Test
    public void testExecuteQueryCommand() {
        // Prepare the result
        Role role = new Role();
        role.setName("test role");
        List<Role> result = Collections.singletonList(role);

        // Mock the Dao
        RoleDao roleDaoMock = mock(RoleDao.class);
        when(roleDaoMock.getAll()).thenReturn(result);
        when(getDbFacadeMockInstance().getRoleDao()).thenReturn(roleDaoMock);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong roles returned", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
