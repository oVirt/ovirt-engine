package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** A test case for {@link GetVmInterfacesByVmIdQuery} */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DbFacade.class)
public class GetVmInterfacesByVmIdQueryTest {

    /** A test that checked that all the parameters are passed properly to the DAO */
    @Test
    public void testExectueQuery() {
        DbFacade dbFacadeMock = mock(DbFacade.class);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacadeMock);

        VmNetworkInterfaceDAO daoMock = mock(VmNetworkInterfaceDAO.class);
        when(dbFacadeMock.getVmNetworkInterfaceDAO()).thenReturn(daoMock);

        Guid guid   = new Guid();
        Guid userID = new Guid();

        IVdcUser user = mock(IVdcUser.class);
        when(user.getUserId()).thenReturn(userID);

        GetVmByVmIdParameters params = mock(GetVmByVmIdParameters.class);
        when(params.getId()).thenReturn(guid);
        when(params.isFiltered()).thenReturn(true);

        GetVmInterfacesByVmIdQuery query = spy(new GetVmInterfacesByVmIdQuery(params));
        when(query.getUser()).thenReturn(user);
        when(query.getUserID()).thenReturn(userID);

        query.executeQueryCommand();

        verify(daoMock).getAllForVm(guid, userID, true);
    }
}
