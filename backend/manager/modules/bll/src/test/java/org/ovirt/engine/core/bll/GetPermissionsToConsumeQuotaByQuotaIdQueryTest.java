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
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class GetPermissionsToConsumeQuotaByQuotaIdQueryTest extends AbstractQueryTest<GetEntitiesRelatedToQuotaIdParameters, GetPermissionsToConsumeQuotaByQuotaIdQuery<GetEntitiesRelatedToQuotaIdParameters>> {

    @Mock
    DbFacade db;

    @Mock
    PermissionDAO permissionDAO;

    Guid quotaId = Guid.NewGuid();
    List<permissions> returnedPermissions;

    @Test
    public void testExecuteQuery() {
        mockDAOForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedPermissions, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     */
    private void mockDAOForQuery() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        when(db.getPermissionDAO()).thenReturn(permissionDAO);

        returnedPermissions = new ArrayList<permissions>();
        permissions permissions = new permissions();
        returnedPermissions.add(permissions);
        when(getQueryParameters().getQuotaId()).thenReturn(quotaId);
        Mockito.when(permissionDAO.getConsumedPermissionsForQuotaId(quotaId)).thenReturn(returnedPermissions);
    }
}
