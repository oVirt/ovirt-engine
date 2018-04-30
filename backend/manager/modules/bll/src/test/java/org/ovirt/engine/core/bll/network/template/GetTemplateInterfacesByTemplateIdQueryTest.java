package org.ovirt.engine.core.bll.network.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDaoMock;

    @Test
    public void testExecuteQuery() {
        Guid templateID = Guid.newGuid();
        List<VmNetworkInterface> expectedResult = Collections.singletonList(new VmNetworkInterface());

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(templateID);

        when(vmNetworkInterfaceDaoMock.getAllForTemplate(templateID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        getQuery().executeQueryCommand();

        List<VmNetworkInterface> result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expectedResult, result, "Wrong interfaces returned");
    }
}
