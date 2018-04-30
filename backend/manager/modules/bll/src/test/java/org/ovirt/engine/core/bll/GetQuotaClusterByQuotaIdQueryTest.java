package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

/**
 * A test for the {@link GetQuotaClusterByQuotaIdQuery} class.
 * This is a flow test that uses mocking to verify the correct Daos are called.
 */
public class GetQuotaClusterByQuotaIdQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetQuotaClusterByQuotaIdQuery<IdQueryParameters>> {

    @Mock
    private QuotaDao quotaDao;


    @Test
    public void testExecuteQueryCommand() {
        // Mock the parameters
        Guid quotaId = Guid.newGuid();
        when(params.getId()).thenReturn(quotaId);

        // Create the return value
        QuotaCluster group = new QuotaCluster();
        group.setQuotaId(quotaId);

        // Mock the Dao
        when(quotaDao.getQuotaClusterByQuotaGuidWithGeneralDefault(quotaId)).thenReturn(Collections.singletonList(group));

        // Execute the query
        getQuery().executeQueryCommand();

        // Assert the result
        @SuppressWarnings("unchecked")
        List<QuotaCluster> results = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, results.size(), "Wrong number of results returned");
        assertEquals(group, results.get(0), "Wrong results returned");
    }
}
