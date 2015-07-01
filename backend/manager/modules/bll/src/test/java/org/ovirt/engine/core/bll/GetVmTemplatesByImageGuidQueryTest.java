package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * A test for the {@link GetVmTempaltesByImageGuidQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the Dao}).
 * The internal workings of the Dao are not tested.
 */
public class GetVmTemplatesByImageGuidQueryTest extends AbstractQueryTest<IdQueryParameters, GetVmTemplatesByImageGuidQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid imageGuid = Guid.newGuid();
        when(params.getId()).thenReturn(imageGuid);

        // Set up the Daos
        Map<Boolean, VmTemplate> expected =
                Collections.singletonMap(true, new VmTemplate());
        VmTemplateDao vmTemplateDaoMock = mock(VmTemplateDao.class);
        when(vmTemplateDaoMock.getAllForImage(imageGuid)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVmTemplateDao()).thenReturn(vmTemplateDaoMock);

        // Mock away the handler
        doNothing().when(getQuery()).updateDisksFromDb(any(VmTemplate.class));

        // Run the query
        GetVmTemplatesByImageGuidQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
