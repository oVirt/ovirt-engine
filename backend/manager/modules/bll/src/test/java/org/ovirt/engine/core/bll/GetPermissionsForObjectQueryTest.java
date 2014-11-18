package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetPermissionsForObjectQueryTest extends AbstractUserQueryTest<GetPermissionsForObjectParameters, GetPermissionsForObjectQuery<GetPermissionsForObjectParameters>> {
    /** The object id to query on */
    private Guid objectID;

    /** The mocked permissions the DAO should return */
    private List<Permissions> mockedPermissions;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        objectID = Guid.newGuid();

        Permissions permission = new Permissions();
        permission.setObjectId(objectID);
        mockedPermissions = Collections.singletonList(permission);
    }

    @Test
    public void testExecuteQueryWithDirectOnly() {
        PermissionDAO permissionDAOMock = mock(PermissionDAO.class);
        when(permissionDAOMock.getAllForEntity(objectID, UNPRIVILEGED_USER_SESSION_ID, getQueryParameters().isFiltered(), false)).thenReturn(mockedPermissions);
        when(getDbFacadeMockInstance().getPermissionDao()).thenReturn(permissionDAOMock);

        assertQueryDAOCall(true);
    }

    @Test
    public void testExecuteQueryWithoutDirectOnly() {
        VdcObjectType type = RandomUtils.instance().pickRandom(VdcObjectType.values());
        when(getQueryParameters().getVdcObjectType()).thenReturn(type);

        PermissionDAO permissionDAOMock = mock(PermissionDAO.class);
        when(permissionDAOMock.getTreeForEntity(objectID,
                type,
                UNPRIVILEGED_USER_SESSION_ID,
                getQueryParameters().isFiltered())).thenReturn(mockedPermissions);
        when(getDbFacadeMockInstance().getPermissionDao()).thenReturn(permissionDAOMock);

        assertQueryDAOCall(false);
    }

    private void assertQueryDAOCall(boolean isDirectOnly) {
        when(getQueryParameters().getObjectId()).thenReturn(objectID);
        when(getQueryParameters().getDirectOnly()).thenReturn(isDirectOnly);
        when(getQuery().getEngineSessionSeqId()).thenReturn(UNPRIVILEGED_USER_SESSION_ID);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Permissions> result = (List<Permissions>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong permissions returned from the query", mockedPermissions, result);
    }
}
