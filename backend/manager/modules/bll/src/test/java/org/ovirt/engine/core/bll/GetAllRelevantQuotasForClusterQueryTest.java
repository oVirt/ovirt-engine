package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

/**
 * A test for the {@link GetAllRelevantQuotasForClusterQuery} class.
 * It tests that flow (i.e., that the query delegates properly to the Dao}).
 * The internal workings of the Dao are not tested.
 */
public class GetAllRelevantQuotasForClusterQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetAllRelevantQuotasForClusterQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        Guid quotaID = Guid.newGuid();
        when(params.getId()).thenReturn(quotaID);

        // Set up the Daos
        List<Quota> expected = Collections.singletonList(new Quota());
        QuotaDao quotaDaoMock = mock(QuotaDao.class);
        when(quotaDaoMock.getAllRelevantQuotasForCluster(quotaID, getQuery().getEngineSessionSeqId(), false)).thenReturn(expected);
        when(getDbFacadeMockInstance().getQuotaDao()).thenReturn(quotaDaoMock);

        // Run the query
        GetAllRelevantQuotasForClusterQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
