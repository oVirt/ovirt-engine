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
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class GetTemplatesRelatedToQuotaIdQueryTest extends AbstractQueryTest<GetEntitiesRelatedToQuotaIdParameters, GetTemplatesRelatedToQuotaIdQuery<GetEntitiesRelatedToQuotaIdParameters>> {

    @Mock
    DbFacade db;

    @Mock
    VmTemplateDAO vmTemplateDAO;

    Guid quotaId = Guid.NewGuid();

    List<VmTemplate> returnedVmTemplates;

    @Test
    public void testExecuteQuery() {
        mockDAOForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedVmTemplates, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     */
    private void mockDAOForQuery() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        when(db.getVmTemplateDAO()).thenReturn(vmTemplateDAO);

        returnedVmTemplates = new ArrayList<VmTemplate>();
        when(getQueryParameters().getQuotaId()).thenReturn(quotaId);
        Mockito.when(vmTemplateDAO.getAllTemplatesRelatedToQuotaId(quotaId)).thenReturn(returnedVmTemplates);
    }
}
