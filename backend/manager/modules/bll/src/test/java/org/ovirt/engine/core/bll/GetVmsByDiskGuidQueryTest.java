package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test for the {@link GetVmsByDiskGuidQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the Dao}).
 * The internal workings of the Dao are not tested.
 */
public class GetVmsByDiskGuidQueryTest extends AbstractQueryTest<IdQueryParameters, GetVmsByDiskGuidQuery<IdQueryParameters>> {
    @Mock
    private VmDao vmDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid imageGuid = Guid.newGuid();
        when(params.getId()).thenReturn(imageGuid);

        // Set up the Daos
        Map<Boolean, List<VM>> expected = Collections.singletonMap(true, Collections.singletonList(new VM()));
        when(vmDaoMock.getForDisk(imageGuid, true)).thenReturn(expected);

        // Run the query
        GetVmsByDiskGuidQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }
}
