package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDAO;

/**
 * A test for the {@link GetAllRelevantQuotasForStorageQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the DAO}).
 * The internal workings of the DAO are not tested.
 */
public class GetAllRelevantQuotasForStorageQueryTest extends AbstractQueryTest<GetAllRelevantQuotasForStorageParameters, GetAllRelevantQuotasForStorageQuery<GetAllRelevantQuotasForStorageParameters>> {
    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid quotaID = Guid.NewGuid();
        when(params.getStorageId()).thenReturn(quotaID);

        // Set up the DAOs
        List<Quota> expected = Collections.singletonList(new Quota());
        QuotaDAO quotaDAOMock = mock(QuotaDAO.class);
        when(quotaDAOMock.getAllRelevantQuotasForStorage(quotaID, null, false)).thenReturn(expected);
        when(getDbFacadeMockInstance().getQuotaDAO()).thenReturn(quotaDAOMock);

        // Run the query
        GetAllRelevantQuotasForStorageQuery<GetAllRelevantQuotasForStorageParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
