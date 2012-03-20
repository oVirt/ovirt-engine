package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByImageGuidParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDAO;

/**
 * A test for the {@link GetVmTempaltesByImageGuidQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the DAO}).
 * The internal workings of the DAO are not tested.
 */
public class GetVmTemplatesByImageGuidQueryTest extends AbstractQueryTest<GetVmTemplatesByImageGuidParameters, GetVmTemplatesByImageGuidQuery<GetVmTemplatesByImageGuidParameters>> {
    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid imageGuid = Guid.NewGuid();
        when(params.getImageGuid()).thenReturn(imageGuid);

        // Set up the DAOs
        Map<Boolean, List<VmTemplate>> expected =
                Collections.singletonMap(true, Collections.singletonList(new VmTemplate()));
        VmTemplateDAO vmTemplateDAOMock = mock(VmTemplateDAO.class);
        when(vmTemplateDAOMock.getAllForImage(imageGuid)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVmTemplateDAO()).thenReturn(vmTemplateDAOMock);

        // Run the query
        GetVmTemplatesByImageGuidQuery<GetVmTemplatesByImageGuidParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
