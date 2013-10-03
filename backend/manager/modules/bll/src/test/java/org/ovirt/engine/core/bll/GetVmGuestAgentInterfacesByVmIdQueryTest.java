package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;

/** A test for {@link org.ovirt.engine.core.bll.GetVmGuestAgentInterfacesByVmIdQuery} */
public class GetVmGuestAgentInterfacesByVmIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetVmGuestAgentInterfacesByVmIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQuery() {
        IdQueryParameters params = getQueryParameters();
        Guid vmId = Guid.newGuid();
        when(params.getId()).thenReturn(vmId);

        VmGuestAgentInterfaceDao vmGuestAgentInterfaceDao = mock(VmGuestAgentInterfaceDao.class);
        when(getQuery().getDbFacade().getVmGuestAgentInterfaceDao()).thenReturn(vmGuestAgentInterfaceDao);

        getQuery().executeQueryCommand();
        verify(vmGuestAgentInterfaceDao).getAllForVm(vmId,
                getUser().getId(),
                getQueryParameters().isFiltered());
    }
}
