package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetPermissionsForObjectQueryTest extends AbstractUserQueryTest<GetPermissionsForObjectParameters, GetPermissionsForObjectQuery<GetPermissionsForObjectParameters>> {
    /** The object id to query on */
    private Guid objectID;

    /** The mocked permissions the Dao should return */
    private List<Permission> mockedPermissions;

    @Mock
    private PermissionDao permissionDaoMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        objectID = Guid.newGuid();

        Permission permission = new Permission();
        permission.setObjectId(objectID);
        mockedPermissions = Collections.singletonList(permission);
    }

    @Test
    public void testExecuteQueryWithDirectOnly() {
        when(permissionDaoMock.getAllForEntity(objectID, UNPRIVILEGED_USER_SESSION_ID, getQueryParameters().isFiltered(), false)).thenReturn(mockedPermissions);
        assertQueryDaoCall(true);
    }

    @Test
    public void testExecuteQueryWithoutDirectOnly() {
        VdcObjectType type = RandomUtils.instance().pickRandom(VdcObjectType.values());
        when(getQueryParameters().getVdcObjectType()).thenReturn(type);

        when(permissionDaoMock.getTreeForEntity(objectID,
                type,
                UNPRIVILEGED_USER_SESSION_ID,
                getQueryParameters().isFiltered())).thenReturn(mockedPermissions);

        assertQueryDaoCall(false);
    }

    private void assertQueryDaoCall(boolean isDirectOnly) {
        when(getQueryParameters().getObjectId()).thenReturn(objectID);
        when(getQueryParameters().getDirectOnly()).thenReturn(isDirectOnly);
        when(getQuery().getEngineSessionSeqId()).thenReturn(UNPRIVILEGED_USER_SESSION_ID);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<Permission> result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(mockedPermissions, result, "Wrong permissions returned from the query");
    }
}
