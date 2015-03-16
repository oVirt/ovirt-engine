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
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDAO;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetPermissionsToConsumeQuotaByQuotaIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetPermissionsToConsumeQuotaByQuotaIdQuery<IdQueryParameters>> {
    @Mock
    PermissionDAO permissionDAO;

    Guid quotaId = Guid.newGuid();
    List<Permission> returnedPermissions;

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
        when(getDbFacadeMockInstance().getPermissionDao()).thenReturn(permissionDAO);

        returnedPermissions = new ArrayList<Permission>();
        Permission permissions = new Permission();
        returnedPermissions.add(permissions);
        when(getQueryParameters().getId()).thenReturn(quotaId);
        Mockito.when(permissionDAO.getConsumedPermissionsForQuotaId(quotaId)).thenReturn(returnedPermissions);
    }
}
