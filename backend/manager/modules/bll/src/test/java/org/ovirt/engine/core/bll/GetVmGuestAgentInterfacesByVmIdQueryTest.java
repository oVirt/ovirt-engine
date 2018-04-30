package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;

/** A test for {@link org.ovirt.engine.core.bll.GetVmGuestAgentInterfacesByVmIdQuery} */
public class GetVmGuestAgentInterfacesByVmIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetVmGuestAgentInterfacesByVmIdQuery<IdQueryParameters>> {

    @Mock
    private VmGuestAgentInterfaceDao vmGuestAgentInterfaceDao;

    @Test
    public void testExecuteQuery() {
        IdQueryParameters params = getQueryParameters();
        Guid vmId = Guid.newGuid();
        when(params.getId()).thenReturn(vmId);

        getQuery().executeQueryCommand();
        verify(vmGuestAgentInterfaceDao).getAllForVm(vmId,
                getUser().getId(),
                getQueryParameters().isFiltered());
    }
}
