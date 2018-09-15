package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

/**
 * A test for the {@link GetVdsAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the Dao}). The internal workings of the Dao are not tested.
 */
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
    private Network networkMocked;
    @Mock
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;
    @Mock
    private LockManager lockManager;
    @Mock
    private HostLocking hostLocking;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getId()).thenReturn(networkId);

        setupVdsDao();
        setupVdsNetworkInterfaceDao();
        setupNetworkDao();
        setLockManager();

        PairQueryable<VdsNetworkInterface, VDS> vdsInterfaceVdsPair =
                new PairQueryable<>(vdsNetworkInterface, vds);
        List<PairQueryable<VdsNetworkInterface, VDS>> expected = Collections.singletonList(vdsInterfaceVdsPair);

        // Run the query
        getQuery().executeQueryCommand();
        // Assert the result
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }

    private void setupVdsDao() {
        List<VDS> expectedVds = Collections.singletonList(vds);
        when(vdsDaoMocked.getAllForNetwork(networkId)).thenReturn(expectedVds);
    }

    private void setupVdsNetworkInterfaceDao() {
        List<VdsNetworkInterface> expectedVdsNetworkInterface = Collections.singletonList(vdsNetworkInterface);
        when(interfaceDaoMocked.getVdsInterfacesByNetworkId(networkId)).thenReturn(expectedVdsNetworkInterface);
    }

    private void setupNetworkDao() {
        when(networkDaoMocked.get(networkId)).thenReturn(networkMocked);
    }

    private void setLockManager() {
        when(lockManager.isExclusiveLockPresent(ArgumentMatchers.any())).thenReturn(false);
    }
}
