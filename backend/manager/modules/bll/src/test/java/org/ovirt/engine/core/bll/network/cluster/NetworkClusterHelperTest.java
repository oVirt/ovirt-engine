package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class NetworkClusterHelperTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid MANAGEMENT_NETWORK_ID = Guid.newGuid();
    private static final Guid NETWORK_ID = Guid.newGuid();
    private static final String MANAGEMENT_NETWORK_NAME = "management network name";
    private static final String NETWORK_NAME = "network name";

    @Mock
    private NetworkClusterDao networkClusterDao;
    @Mock
    private NetworkAttachmentDao networkAttachmentDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    @InjectMocks
    private NetworkClusterHelper underTest;

    private NetworkCluster networkCluster;
    private Network managementNetwork;
    private NetworkCluster managementNetworkCluster;

    @Before
    public void setUp() {
        networkCluster = createNetworkCluster(CLUSTER_ID, NETWORK_ID);
        when(networkClusterDao.get(networkCluster.getId())).thenReturn(networkCluster);

        managementNetworkCluster = createNetworkCluster(CLUSTER_ID, MANAGEMENT_NETWORK_ID);
        managementNetwork = createNetwork(MANAGEMENT_NETWORK_ID, MANAGEMENT_NETWORK_NAME);
        when(managementNetworkUtil.getManagementNetwork(CLUSTER_ID)).thenReturn(managementNetwork);
        when(networkClusterDao.get(new NetworkClusterId(CLUSTER_ID, MANAGEMENT_NETWORK_ID)))
                .thenReturn(managementNetworkCluster);
    }

    @Test
    public void testRemoveNetworkAndReassignRolesNoReassign() {
        testRemoveNetworkAndReassignRoles();
        verify(networkClusterDao, never()).update(same(managementNetworkCluster));
        assertFalse(managementNetworkCluster.isDisplay());
        assertFalse(managementNetworkCluster.isMigration());
    }

    @Test
    public void testRemoveNetworkAndReassignRolesDisplay() {
        networkCluster.setDisplay(true);
        testRemoveNetworkAndReassignRoles();
        verify(networkClusterDao).update(same(managementNetworkCluster));
        assertTrue(managementNetworkCluster.isDisplay());
    }

    @Test
    public void testRemoveNetworkAndReassignRolesMigration() {
        networkCluster.setMigration(true);
        testRemoveNetworkAndReassignRoles();
        verify(networkClusterDao).update(same(managementNetworkCluster));
        assertTrue(managementNetworkCluster.isMigration());
    }

    @Ignore
    @Test
    public void testSetStatusForRequiredNetwork() {
        when(vdsDao.getAllForCluster(CLUSTER_ID)).thenReturn(createHosts(true, false));
        underTest.setStatus(CLUSTER_ID, createNetwork(NETWORK_ID, NETWORK_NAME));
    }

    @Test
    public void testSetStatusForNonRequiredNetworkUpdated() {
        networkCluster.setRequired(false);
        networkCluster.setStatus(NetworkStatus.NON_OPERATIONAL);

        underTest.setStatus(CLUSTER_ID, createNetwork(NETWORK_ID, NETWORK_NAME));

        verify(networkClusterDao).updateStatus(same(networkCluster));
        assertThat(networkCluster.getStatus(), is(NetworkStatus.OPERATIONAL));
    }

    @Test
    public void testSetStatusForNonRequiredNetworkAlreadyOperational() {
        networkCluster.setRequired(false);
        networkCluster.setStatus(NetworkStatus.OPERATIONAL);

        underTest.setStatus(CLUSTER_ID, createNetwork(NETWORK_ID, NETWORK_NAME));

        verify(networkClusterDao, never()).updateStatus(same(networkCluster));
    }

    private void testRemoveNetworkAndReassignRoles() {
        underTest.removeNetworkAndReassignRoles(networkCluster);

        verify(networkClusterDao).remove(CLUSTER_ID, NETWORK_ID);
        verify(networkAttachmentDao).removeByNetworkId(NETWORK_ID);
    }

    private NetworkCluster createNetworkCluster(Guid clusterId, Guid networkId) {
        NetworkCluster result = new NetworkCluster();
        result.setId(new NetworkClusterId(clusterId, networkId));
        return result;
    }

    private Network createNetwork(Guid networkId, String networkName) {
        final Network result = new Network();
        result.setId(networkId);
        result.setName(networkName);
        return result;
    }

    private List<VDS> createHosts(Boolean... isHostUp) {
        return Arrays.stream(isHostUp)
                .map(isUp -> isUp ? VDSStatus.Up : VDSStatus.Down)
                .map(hostStatus -> {
                    VDS host = new VDS();
                    host.setStatus(hostStatus);
                    return host;
                })
                .collect(Collectors.toList());
    }
}
