package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmsByDiskGuidParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

/**
 * A test for the {@link GetVmsByDiskGuidQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the DAO}).
 * The internal workings of the DAO are not tested.
 */
public class GetVmsByImageGuidQueryTest extends AbstractQueryTest<GetVmsByDiskGuidParameters, GetVmsByDiskGuidQuery<GetVmsByDiskGuidParameters>> {
    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid imageGuid = Guid.NewGuid();
        when(params.getDiskGuid()).thenReturn(imageGuid);

        // Set up the DAOs
        Map<Boolean, List<VM>> expected = Collections.singletonMap(true, Collections.singletonList(new VM()));
        VmDAO vmDAOMock = mock(VmDAO.class);
        when(vmDAOMock.getForDisk(imageGuid)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVmDAO()).thenReturn(vmDAOMock);

        // Run the query
        GetVmsByDiskGuidQuery<GetVmsByDiskGuidParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
