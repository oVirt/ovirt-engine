package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmPoolDao;

/**
 * A test case for {@link GetVmPoolByIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetVmPoolByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmPoolByIdQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid vmPoolID = Guid.newGuid();
        VmPool expectedResult = new VmPool();
        expectedResult.setVmPoolId(vmPoolID);

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmPoolID);

        VmPoolDao vmPoolDaoMock = mock(VmPoolDao.class);
        when(vmPoolDaoMock.get(vmPoolID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        when(dbFacadeMock.getVmPoolDao()).thenReturn(vmPoolDaoMock);

        getQuery().executeQueryCommand();

        VmPool result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong VM pool returned", expectedResult, result);
    }
}
