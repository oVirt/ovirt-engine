package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

/**
 * A test case for {@link GetVmsRelatedToQuotaIdQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetVmsRelatedToQuotaIdTest extends AbstractQueryTest<IdQueryParameters, GetVmsRelatedToQuotaIdQuery<IdQueryParameters>> {
    @Mock
    VmDAO vmDAO;

    Guid quotaId = Guid.newGuid();
    List<VM> returnedVms;

    @Test
    public void testExecuteQuery() {
        mockDAOForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedVms, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     */
    private void mockDAOForQuery() {
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDAO);

        returnedVms = new ArrayList<VM>();
        when(getQueryParameters().getId()).thenReturn(quotaId);
        when(vmDAO.getAllVmsRelatedToQuotaId(quotaId)).thenReturn(returnedVms);
    }
}
