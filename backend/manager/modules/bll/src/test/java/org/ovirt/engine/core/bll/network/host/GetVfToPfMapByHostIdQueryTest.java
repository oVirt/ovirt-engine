package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class GetVfToPfMapByHostIdQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetVfToPfMapByHostIdQuery<IdQueryParameters>> {

    @Mock
    private NetworkDeviceHelper networkDeviceHelper;

    @Test
    public void testExecuteQuery() {

        doReturn(networkDeviceHelper).when(getQuery()).getNetworkDeviceHelper();

        Guid hostId = Guid.newGuid();
        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(hostId);

        final HashMap<Guid, Guid> expected = new HashMap<>();
        when(networkDeviceHelper.getVfMap(hostId)).thenReturn(expected);

        getQuery().executeQueryCommand();

        final Map<Guid, Guid> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertSame(expected, actual);
    }
}
