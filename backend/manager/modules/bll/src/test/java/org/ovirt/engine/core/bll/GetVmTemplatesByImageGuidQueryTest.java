package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
    @Mock
    private VmTemplateDao vmTemplateDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid imageGuid = Guid.newGuid();
        when(params.getId()).thenReturn(imageGuid);

        // Set up the Daos
        Map<Boolean, VmTemplate> expected =
                Collections.singletonMap(true, new VmTemplate());
        when(vmTemplateDaoMock.getAllForImage(imageGuid)).thenReturn(expected);

        // Mock away the handler
        doNothing().when(getQuery()).updateDisksFromDb(any());

        // Run the query
        GetVmTemplatesByImageGuidQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }
}
