package org.ovirt.engine.core.bll.network.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;

/**
 * A test case for {@link GetTemplateInterfacesByTemplateIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the DAO occur.
 */
public class GetTemplateInterfacesByTemplateIdQueryTest extends AbstractUserQueryTest<GetVmTemplateParameters, GetTemplateInterfacesByTemplateIdQuery<GetVmTemplateParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid templateID = Guid.NewGuid();
        List<VmNetworkInterface> expectedResult = Collections.singletonList(new VmNetworkInterface());

        GetVmTemplateParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(templateID);

        VmNetworkInterfaceDAO vmNetworkInterfaceDAOMock = mock(VmNetworkInterfaceDAO.class);
        when(vmNetworkInterfaceDAOMock.getAllForTemplate(templateID, getUser().getUserId(), paramsMock.isFiltered())).thenReturn(expectedResult);
        when(getDbFacadeMockInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VmNetworkInterface> result = (List<VmNetworkInterface>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong interfaces returned", expectedResult, result);
    }
}
