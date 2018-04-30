package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test case for {@link GetVmsRelatedToQuotaIdQuery}.
 */
public class GetVmsRelatedToQuotaIdTest extends AbstractQueryTest<IdQueryParameters, GetVmsRelatedToQuotaIdQuery<IdQueryParameters>> {
    @Mock
    VmDao vmDao;

    Guid quotaId = Guid.newGuid();
    List<VM> returnedVms;

    @Test
    public void testExecuteQuery() {
        mockDaoForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedVms, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize Dao to be used in query.
     */
    private void mockDaoForQuery() {
        returnedVms = new ArrayList<>();
        when(getQueryParameters().getId()).thenReturn(quotaId);
        when(vmDao.getAllVmsRelatedToQuotaId(quotaId)).thenReturn(returnedVms);
    }
}
