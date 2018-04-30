package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
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
        returnedVmTemplates = new ArrayList<>();
        when(getQueryParameters().getId()).thenReturn(quotaId);
        when(vmTemplateDao.getAllTemplatesRelatedToQuotaId(quotaId)).thenReturn(returnedVmTemplates);
    }
}
