package org.ovirt.engine.core.bll.network.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/**
 * A test case for {@link GetTemplateInterfacesByTemplateIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetTemplateInterfacesByTemplateIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetTemplateInterfacesByTemplateIdQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid templateID = Guid.newGuid();
        List<VmNetworkInterface> expectedResult = Collections.singletonList(new VmNetworkInterface());

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(templateID);

        VmNetworkInterfaceDao vmNetworkInterfaceDaoMock = mock(VmNetworkInterfaceDao.class);
        when(vmNetworkInterfaceDaoMock.getAllForTemplate(templateID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);
        when(getDbFacadeMockInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDaoMock);

        getQuery().executeQueryCommand();

        List<VmNetworkInterface> result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong interfaces returned", expectedResult, result);
    }
}
