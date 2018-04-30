package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;

/**
 * A test case for {@link GetPermissionsByAdElementIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetPermissionsByAdElementIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetPermissionsByAdElementIdQuery<IdQueryParameters>> {
    @Mock
    private PermissionDao permissionDaoMock;

    @Test
    public void testQueryExecution() {
        // Prepare the query parameters
        Guid adElementGuid = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(adElementGuid);

        // Create expected result
        Permission expected = new Permission();
        expected.setAdElementId(adElementGuid);

        // Mock the Daos
        when(permissionDaoMock.getAllForAdElement
                (adElementGuid, getQuery().getEngineSessionSeqId(), getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(expected));

        getQuery().executeQueryCommand();

        // Assert the query's results
        @SuppressWarnings("unchecked")
        List<Permission> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(1, actual.size(), "Wrong number of returned permissions");
        assertEquals(expected, actual.get(0), "Wrong returned permissions");
    }
}
