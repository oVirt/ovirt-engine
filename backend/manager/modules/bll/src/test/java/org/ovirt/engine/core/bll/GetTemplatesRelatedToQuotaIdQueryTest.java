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
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetTemplatesRelatedToQuotaIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetTemplatesRelatedToQuotaIdQuery<IdQueryParameters>> {
    @Mock
    VmTemplateDao vmTemplateDao;

    Guid quotaId = Guid.newGuid();

    List<VmTemplate> returnedVmTemplates;

    @Test
    public void testExecuteQuery() {
        mockDaoForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedVmTemplates, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize Dao to be used in query.
     */
    private void mockDaoForQuery() {
        when(getDbFacadeMockInstance().getVmTemplateDao()).thenReturn(vmTemplateDao);

        returnedVmTemplates = new ArrayList<>();
        when(getQueryParameters().getId()).thenReturn(quotaId);
        Mockito.when(vmTemplateDao.getAllTemplatesRelatedToQuotaId(quotaId)).thenReturn(returnedVmTemplates);
    }
}
