package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDAO;

/**
 * A test case for {@link GetPermissionsByAdElementIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetPermissionsByAdElementIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetPermissionsByAdElementIdQuery<IdQueryParameters>> {

    @Test
    public void testQueryExecution() {
        // Prepare the query parameters
        Guid adElementGuid = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(adElementGuid);

        // Create expected result
        Permissions expected = new Permissions();
        expected.setAdElementId(adElementGuid);

        // Mock the DAOs
        PermissionDAO permissionDAOMock = mock(PermissionDAO.class);
        when(permissionDAOMock.getAllForAdElement
                (adElementGuid, getQuery().getEngineSessionSeqId(), getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getPermissionDao()).thenReturn(permissionDAOMock);

        getQuery().executeQueryCommand();

        // Assert the query's results
        @SuppressWarnings("unchecked")
        List<Permissions> actual = (List<Permissions>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong number of returned permissions", 1, actual.size());
        assertEquals("Wrong returned permissions", expected, actual.get(0));
    }
}
