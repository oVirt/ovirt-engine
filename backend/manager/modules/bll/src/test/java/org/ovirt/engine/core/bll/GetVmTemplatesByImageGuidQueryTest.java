package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByImageGuidParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test for the {@link GetVmTempaltesByImageGuidQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the DAO}).
 * The internal workings of the DAO are not tested.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(VmTemplateHandler.class)
public class GetVmTemplatesByImageGuidQueryTest extends AbstractQueryTest<GetVmTemplatesByImageGuidParameters, GetVmTemplatesByImageGuidQuery<GetVmTemplatesByImageGuidParameters>> {

    public GetVmTemplatesByImageGuidQueryTest() {
        mockStatic(VmTemplateHandler.class);
    }

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
