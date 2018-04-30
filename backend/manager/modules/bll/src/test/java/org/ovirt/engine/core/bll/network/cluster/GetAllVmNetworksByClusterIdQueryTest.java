package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllVmNetworksByClusterIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetAllVmNetworksByClusterIdQuery<? extends IdQueryParameters>> {

    @Mock
    private NetworkDao networkDao;

    @Mock
    private ClusterDao clusterDao;

    private Guid clusterId = Guid.newGuid();
    private Cluster cluster;
    private Network externalNetwork;
    private List<Network> networks;

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(clusterId);

        externalNetwork = new Network();
        setIsValidProvidedBy(true);

        networks = new ArrayList<>();
        networks.add(new Network());
        networks.add(externalNetwork);
        setIsVmNetworks(true);

        when(getQueryParameters().getId()).thenReturn(clusterId);
        when(clusterDao.get(clusterId)).thenReturn(cluster);
        when(networkDao.getAllForCluster(clusterId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(
                networks);
    }

    private void setIsValidProvidedBy(boolean validProvidedBy) {
        externalNetwork.setProvidedBy(validProvidedBy ? new ProviderNetwork() : null);
    }

    private void setIsOvsCluster(boolean ovsCluster) {
        cluster.setRequiredSwitchTypeForCluster(ovsCluster ? SwitchType.OVS : SwitchType.LEGACY);
    }

    private void setIsVmNetworks(boolean isVmNetwork) {
        networks.forEach(network -> network.setVmNetwork(isVmNetwork));
    }

    @Test
    public void testOvsClusterReturnValue() {
        setIsOvsCluster(true);
        getQuery().executeQueryCommand();
        List<Network> networks = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, networks.size());
    }

    @Test
    public void testLagacyClusterReturnValue() {
        setIsOvsCluster(false);
        getQuery().executeQueryCommand();
        List<Network> networks = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(2, networks.size());
    }

    @Test
    public void testInvalidExternalNetworkList() {
        setIsOvsCluster(true);
        setIsValidProvidedBy(false);
        getQuery().executeQueryCommand();
        List<Network> networks = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(0, networks.size());
    }

    @Test
    public void testNonVmNetworksOnly() {
        setIsOvsCluster(true);
        setIsValidProvidedBy(true);
        setIsVmNetworks(false);
        getQuery().executeQueryCommand();
        List<Network> networks = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(0, networks.size());
    }
}
