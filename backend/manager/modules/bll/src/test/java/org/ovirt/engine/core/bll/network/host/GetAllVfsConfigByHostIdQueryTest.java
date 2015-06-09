package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class GetAllVfsConfigByHostIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetAllVfsConfigByHostIdQuery<IdQueryParameters>> {

    @Mock
    private NetworkDeviceHelper networkDeviceHelper;

    @Test
    public void testExecuteQuery() {

        doReturn(networkDeviceHelper).when(getQuery()).getNetworkDeviceHelper();

        Guid hostId = Guid.newGuid();
        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(hostId);

        List<HostNicVfsConfig> vfsConfigs = new ArrayList<>();
        vfsConfigs.add(new HostNicVfsConfig());
        when(networkDeviceHelper.getHostNicVfsConfigsWithNumVfsDataByHostId(hostId)).thenReturn(vfsConfigs);

        getQuery().executeQueryCommand();

        List<HostNicVfsConfig> result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(vfsConfigs, result);
    }
}
