package org.ovirt.engine.core.bll.network.cluster;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NetworkClusterHelperTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid MANAGEMENT_NETWORK_ID = Guid.newGuid();
    private static final Guid NETWORK_ID1 = Guid.newGuid();
    private static final Guid NETWORK_ID2 = Guid.newGuid();
    private static final String MANAGEMENT_NETWORK_NAME = "management network name";
    private static final String NETWORK_NAME1 = "network name 1";
    private static final String NETWORK_NAME2 = "network name 2";

    @Mock
    private NetworkClusterDao networkClusterDao;
    @Mock
    private NetworkAttachmentDao networkAttachmentDao;
    @Mock
    private VdsStaticDao vdsStaticDao;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    @InjectMocks
    private NetworkClusterHelper underTest;

    private NetworkCluster networkCluster;
    private Network managementNetwork;
    private NetworkCluster managementNetworkCluster;

    @BeforeEach
    public void setUp() {
        networkCluster = createNetworkCluster(CLUSTER_ID, NETWORK_ID1);
        when(networkClusterDao.get(networkCluster.getId())).thenReturn(networkCluster);
        when(networkClusterDao.getAllForCluster(CLUSTER_ID)).thenReturn(Arrays.asList(networkCluster));

        managementNetworkCluster = createNetworkCluster(CLUSTER_ID, MANAGEMENT_NETWORK_ID);
        managementNetwork = createNetwork(MANAGEMENT_NETWORK_ID, MANAGEMENT_NETWORK_NAME);
        when(managementNetworkUtil.getManagementNetwork(CLUSTER_ID)).thenReturn(managementNetwork);
        when(networkClusterDao.get(new NetworkClusterId(CLUSTER_ID, MANAGEMENT_NETWORK_ID)))
                .thenReturn(managementNetworkCluster);
        when(vdsDynamicDao.checkIfExistsHostWithStatusInCluster(CLUSTER_ID, VDSStatus.Up)).thenReturn(true);
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

    @Test
    public void testSetStatusForRequiredNetworkAbsentOnHost() {
        networkCluster.setStatus(NetworkStatus.OPERATIONAL);

        when(vdsStaticDao.checkIfExistsHostThatMissesNetworkInCluster(CLUSTER_ID, NETWORK_NAME1, VDSStatus.Up))
                .thenReturn(true);

        underTest.setStatus(CLUSTER_ID, singletonList(createNetwork(NETWORK_ID1, NETWORK_NAME1)));

        verify(networkClusterDao).updateStatus(same(networkCluster));
        assertThat(networkCluster.getStatus(), is(NetworkStatus.NON_OPERATIONAL));
    }

    @Test
    public void testSetStatusForRequiredNetworksAbsentOnHost() {
        networkCluster.setStatus(NetworkStatus.OPERATIONAL);

        final List<Network> networks = asList(
                createNetwork(NETWORK_ID1, NETWORK_NAME1),
                createNetwork(NETWORK_ID2, NETWORK_NAME2));
        when(vdsStaticDao.checkIfExistsHostThatMissesNetworkInCluster(CLUSTER_ID, NETWORK_NAME1, VDSStatus.Up))
                .thenReturn(true);

        underTest.setStatus(CLUSTER_ID, networks);

        verify(networkClusterDao).updateStatus(same(networkCluster));
        assertThat(networkCluster.getStatus(), is(NetworkStatus.NON_OPERATIONAL));
    }

    @Test
    public void testSetStatusForRequiredNetworkPresentOnHost() {
        networkCluster.setStatus(NetworkStatus.OPERATIONAL);

        underTest.setStatus(CLUSTER_ID, singletonList(createNetwork(NETWORK_ID1, NETWORK_NAME1)));

        verify(vdsStaticDao).checkIfExistsHostThatMissesNetworkInCluster(CLUSTER_ID, NETWORK_NAME1, VDSStatus.Up);
        verify(networkClusterDao, never()).updateStatus(same(networkCluster));
        assertThat(networkCluster.getStatus(), is(NetworkStatus.OPERATIONAL));
    }

    @Test
    public void testSetStatusForNonRequiredNetworkUpdated() {
        networkCluster.setRequired(false);
        networkCluster.setStatus(NetworkStatus.NON_OPERATIONAL);

        underTest.setStatus(CLUSTER_ID, createNetwork(NETWORK_ID1, NETWORK_NAME1));

        verify(networkClusterDao).updateStatus(same(networkCluster));
        assertThat(networkCluster.getStatus(), is(NetworkStatus.OPERATIONAL));
    }

    @Test
    public void testSetStatusForNonRequiredNetworkAlreadyOperational() {
        networkCluster.setRequired(false);
        networkCluster.setStatus(NetworkStatus.OPERATIONAL);

        underTest.setStatus(CLUSTER_ID, createNetwork(NETWORK_ID1, NETWORK_NAME1));

        verify(networkClusterDao, never()).updateStatus(same(networkCluster));
    }

    private void testRemoveNetworkAndReassignRoles() {
        underTest.removeNetworkAndReassignRoles(networkCluster);

        verify(networkClusterDao).remove(CLUSTER_ID, NETWORK_ID1);
        verify(networkAttachmentDao).removeByNetworkId(NETWORK_ID1);
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
}
