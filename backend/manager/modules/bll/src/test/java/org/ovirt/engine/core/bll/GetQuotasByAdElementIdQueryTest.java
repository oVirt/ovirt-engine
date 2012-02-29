package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class GetQuotasByAdElementIdQueryTest extends AbstractQueryTest<GetQuotasByAdElementIdQueryParameters, GetQuotasByAdElementIdQuery<GetQuotasByAdElementIdQueryParameters>> {

    @Mock
    DbFacade db;

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
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        when(db.getQuotaDAO()).thenReturn(quotaDAO);

        returnedQuotas = new ArrayList<Quota>();
        when(getQueryParameters().getAdElementId()).thenReturn(adElementId);
        when(getQueryParameters().getStoragePoolId()).thenReturn(storagePoolId);
        Mockito.when(quotaDAO.getQuotaByAdElementId(adElementId, storagePoolId)).thenReturn(returnedQuotas);
    }
}
