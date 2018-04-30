package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
public class GetQuotasByAdElementIdQueryTest extends AbstractQueryTest<GetQuotasByAdElementIdQueryParameters, GetQuotasByAdElementIdQuery<GetQuotasByAdElementIdQueryParameters>> {
    @Mock
    QuotaDao quotaDao;

    Guid adElementId = Guid.newGuid();
    Guid storagePoolId = Guid.newGuid();

    List<Quota> returnedQuotas;

    @Test
    public void testExecuteQuery() {
        mockDaoForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedQuotas, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize Dao to be used in query.
     */
    private void mockDaoForQuery() {
        returnedQuotas = new ArrayList<>();
        when(getQueryParameters().getAdElementId()).thenReturn(adElementId);
        when(getQueryParameters().getStoragePoolId()).thenReturn(storagePoolId);
        when(quotaDao.getQuotaByAdElementId(adElementId, storagePoolId, false)).thenReturn(returnedQuotas);
    }
}
