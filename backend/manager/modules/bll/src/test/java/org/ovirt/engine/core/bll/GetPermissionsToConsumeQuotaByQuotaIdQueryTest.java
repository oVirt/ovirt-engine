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
import org.ovirt.engine.core.dao.PermissionDao;

/**
 * A test case for {@link GetTemplatesRelatedToQuotaIdQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetPermissionsToConsumeQuotaByQuotaIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetPermissionsToConsumeQuotaByQuotaIdQuery<IdQueryParameters>> {
    @Mock
    PermissionDao permissionDao;

    Guid quotaId = Guid.newGuid();
    List<Permission> returnedPermissions;

    @Test
    public void testExecuteQuery() {
        mockDaoForQuery();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(returnedPermissions, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize Dao to be used in query.
     */
    private void mockDaoForQuery() {
        when(getDbFacadeMockInstance().getPermissionDao()).thenReturn(permissionDao);

        returnedPermissions = new ArrayList<>();
        Permission permissions = new Permission();
        returnedPermissions.add(permissions);
        when(getQueryParameters().getId()).thenReturn(quotaId);
        Mockito.when(permissionDao.getConsumedPermissionsForQuotaId(quotaId)).thenReturn(returnedPermissions);
    }
}
