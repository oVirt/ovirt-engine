package org.ovirt.engine.core.bll.network.vm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/** A test case for {@link GetVmInterfacesByVmIdQuery} */
public class GetVmInterfacesByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmInterfacesByVmIdQuery<IdQueryParameters>> {

    /** A test that checked that all the parameters are passed properly to the Dao */
    @Test
    public void testExectueQuery() {
        DbFacade dbFacadeMock = getDbFacadeMockInstance();

        VmNetworkInterfaceDao daoMock = mock(VmNetworkInterfaceDao.class);
        when(dbFacadeMock.getVmNetworkInterfaceDao()).thenReturn(daoMock);

        Guid guid = Guid.newGuid();

        IdQueryParameters params = getQueryParameters();
        when(params.getId()).thenReturn(guid);

        GetVmInterfacesByVmIdQuery<?> query = getQuery();

        query.executeQueryCommand();

        verify(daoMock).getAllForVm(guid, getUser().getId(), getQueryParameters().isFiltered());
    }
}

