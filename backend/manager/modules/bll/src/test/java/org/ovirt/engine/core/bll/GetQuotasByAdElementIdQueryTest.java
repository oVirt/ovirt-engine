package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDAO;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetQuotasByAdElementIdQueryTest extends AbstractQueryTest<GetQuotasByAdElementIdQueryParameters, GetQuotasByAdElementIdQuery<GetQuotasByAdElementIdQueryParameters>> {
    @Mock
    QuotaDAO quotaDAO;

    Guid adElementId = new Guid();
    Guid storagePoolId = new Guid();

    List<Quota> returnedQuotas;

    @Test
    public void testExecuteQuery() {
        mockDAOForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedQuotas, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     */
    private void mockDAOForQuery() {
        when(getDbFacadeMockInstance().getQuotaDao()).thenReturn(quotaDAO);

        returnedQuotas = new ArrayList<Quota>();
        when(getQueryParameters().getAdElementId()).thenReturn(adElementId);
        when(getQueryParameters().getStoragePoolId()).thenReturn(storagePoolId);
        Mockito.when(quotaDAO.getQuotaByAdElementId(adElementId, storagePoolId, false)).thenReturn(returnedQuotas);
    }
}
