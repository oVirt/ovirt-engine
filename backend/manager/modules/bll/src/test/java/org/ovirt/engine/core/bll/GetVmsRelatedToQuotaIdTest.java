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
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for {@link GetVmsRelatedToQuotaIdQuery}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class GetVmsRelatedToQuotaIdTest extends AbstractQueryTest<GetEntitiesRelatedToQuotaIdParameters, GetVmsRelatedToQuotaIdQuery<GetEntitiesRelatedToQuotaIdParameters>> {

    @Mock
    DbFacade db;

    @Mock
    VmDAO vmDAO;

    Guid quotaId = Guid.NewGuid();
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
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        when(db.getVmDAO()).thenReturn(vmDAO);

        returnedVms = new ArrayList<VM>();
        when(getQueryParameters().getQuotaId()).thenReturn(quotaId);
        Mockito.when(vmDAO.getAllVmsRelatedToQuotaId(quotaId)).thenReturn(returnedVms);
    }
}
