package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

/**
 * A test for the {@link GetVdsAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the Dao}). The internal workings of the Dao are not tested.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetVdsAndNetworkInterfacesByNetworkIdQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetVdsAndNetworkInterfacesByNetworkIdQuery<IdQueryParameters>> {

    private Guid networkId = Guid.newGuid();
    private Guid qosId = Guid.newGuid();
    private VDS vds = new VDS();
    private VdsNetworkInterface vdsNetworkInterface = new VdsNetworkInterface();

    @Mock
    private VdsDao vdsDaoMocked;
    @Mock
    private InterfaceDao interfaceDaoMocked;
    @Mock
    private NetworkDao networkDaoMocked;
    @Mock
    private HostNetworkQosDao hostNetworkQosDaoMocked;
    @Mock
    private Network networkMocked;
    @Mock
    private HostNetworkQos hostNetworkQos;
    @Mock
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;
    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getId()).thenReturn(networkId);

        setupVdsDao();
        setupVdsNetworkInterfaceDao();
        setupNetworkDao();
        setupHostNetworkQosDao();

        PairQueryable<VdsNetworkInterface, VDS> vdsInterfaceVdsPair =
                new PairQueryable<>(vdsNetworkInterface, vds);
        List<PairQueryable<VdsNetworkInterface, VDS>> expected = Collections.singletonList(vdsInterfaceVdsPair);

        // Run the query
        GetVdsAndNetworkInterfacesByNetworkIdQuery<IdQueryParameters> query = getQuery();
        setupQuerySpy();
        query.executeQueryCommand();
        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupQuerySpy(){
        doReturn(vdsDaoMocked).when(getQuery()).getVdsDao();
        doReturn(interfaceDaoMocked).when(getQuery()).getInterfaceDao();
        doReturn(networkDaoMocked).when(getQuery()).getNetworkDao();
        doReturn(networkImplementationDetailsUtils).when(getQuery()).getNetworkImplementationDetailsUtils();
        doReturn(networkAttachmentDao).when(getQuery()).getNetworkAttachmentDao();

    }

    private void setupVdsDao() {
        List<VDS> expectedVds = Collections.singletonList(vds);
        when(vdsDaoMocked.getAllForNetwork(networkId)).thenReturn(expectedVds);
    }

    private void setupVdsNetworkInterfaceDao() {
        List<VdsNetworkInterface> expectedVdsNetworkInterface = Collections.singletonList(vdsNetworkInterface);
        when(interfaceDaoMocked.getVdsInterfacesByNetworkId(networkId)).thenReturn(expectedVdsNetworkInterface);
    }

    private void setupHostNetworkQosDao() {
        when(getDbFacadeMockInstance().getHostNetworkQosDao()).thenReturn(hostNetworkQosDaoMocked);
        when(networkMocked.getQosId()).thenReturn(qosId);
        when(hostNetworkQosDaoMocked.get(networkMocked.getQosId())).thenReturn(hostNetworkQos);
    }

    private void setupNetworkDao() {
        when(getDbFacadeMockInstance().getNetworkDao()).thenReturn(networkDaoMocked);
        when(networkDaoMocked.get(networkId)).thenReturn(networkMocked);
    }

}
