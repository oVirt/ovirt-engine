package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;
import org.ovirt.engine.core.utils.RandomUtils;

public class InterfaceDaoImplTest extends BaseDaoTestCase<InterfaceDao> {
    private static final String IP_ADDR = "10.35.110.10";
    private static final Guid VDS_ID = FixturesTool.VDS_RHEL6_NFS_SPM;
    private static final String TARGET_ID = "0cc146e8-e5ed-482c-8814-270bc48c297b";
    private static final String LABEL = "abc";

    private VdsNetworkInterface existingVdsInterface;
    private VdsNetworkInterface newVdsInterface;
    private VdsNetworkStatistics newVdsStatistics;
    private HostNetworkQos newQos;

    @Inject
    private NetworkQoSDao networkQoSDao;
    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingVdsInterface = dao.get(FixturesTool.VDS_NETWORK_INTERFACE);

        newQos = new HostNetworkQos();
        newQos.setOutAverageLinkshare(30);
        newQos.setOutAverageUpperlimit(30);
        newQos.setOutAverageRealtime(30);

        newVdsInterface = new VdsNetworkInterface();
        newVdsInterface.setStatistics(new VdsNetworkStatistics());
        newVdsInterface.setId(Guid.newGuid());
        newVdsInterface.setName("eth77");
        newVdsInterface.setNetworkName("enginet");
        newVdsInterface.setSpeed(1000);
        newVdsInterface.setType(3);
        newVdsInterface.setMacAddress("01:C0:81:21:71:17");

        newVdsInterface.setIpv4BootProtocol(Ipv4BootProtocol.STATIC_IP);
        newVdsInterface.setIpv4Address("192.168.122.177");
        newVdsInterface.setIpv4Subnet("255.255.255.0");
        newVdsInterface.setIpv4Gateway("192.168.122.1");

        newVdsInterface.setIpv6BootProtocol(Ipv6BootProtocol.AUTOCONF);
        newVdsInterface.setIpv6Address("ipv6 address");
        newVdsInterface.setIpv6Prefix(666);
        newVdsInterface.setIpv6Gateway("ipv6 gateway");

        newVdsInterface.setMtu(1500);
        newVdsInterface.setQos(newQos);

        newVdsStatistics = newVdsInterface.getStatistics();
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVdsWithInvalidVds() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of interfaces are returned.
     */
    @Test
    public void testGetAllInterfacesForVds() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID);

        assertGetAllForVdsCorrectResult(result);
        testQosAppendedToResultSet(result);
    }

    private void testQosAppendedToResultSet(List<VdsNetworkInterface> result) {
        result.forEach(r-> {
            if (r.getQos() == null) {
                HostNetworkQos hostNetworkQos = new HostNetworkQos();
                hostNetworkQos.setId(r.getId());
                hostNetworkQos.setOutAverageLinkshare(31);
                hostNetworkQos.setOutAverageUpperlimit(32);
                hostNetworkQos.setOutAverageRealtime(33);
                hostNetworkQosDao.save(hostNetworkQos);
            } else {
                r.getQos().setOutAverageLinkshare(31);
                r.getQos().setOutAverageUpperlimit(32);
                r.getQos().setOutAverageRealtime(33 );
                hostNetworkQosDao.update(r.getQos());
            }
        });

        result = dao.getAllInterfacesForVds(VDS_ID);
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(r -> {
            assertNotNull(r.getQos());
            assertEquals(r.getId(), r.getQos().getId());
            assertEquals(QosType.HOSTNETWORK, r.getQos().getQosType());
            assertEquals(31, r.getQos().getOutAverageLinkshare().intValue());
            assertEquals(32, r.getQos().getOutAverageUpperlimit().intValue());
            assertEquals(33, r.getQos().getOutAverageRealtime().intValue());
        });
    }

    /**
     * Ensures that saving an interface for a VDS works as expected.
     */
    @Test
    public void testSaveInterfaceForVds() {
        newVdsInterface.setVdsId(VDS_ID);

        dao.saveInterfaceForVds(newVdsInterface);
        dao.saveStatisticsForVds(newVdsStatistics);

        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID);
        boolean found = false;

        for (VdsNetworkInterface iface : result) {
            found |=
                    iface.getName().equals(newVdsInterface.getName())
                            && iface.getQos().equals(newVdsInterface.getQos());
        }

        assertTrue(found);
    }

    /**
     * Ensures that the specified VDS's interfaces are deleted.
     */
    @Test
    public void testRemoveInterfacesForVds() {
        List<VdsNetworkInterface> before = dao.getAllInterfacesForVds(VDS_ID);

        // ensure we have records before the test
        boolean found = false;
        for (VdsNetworkInterface iface : before) {
            found |= FixturesTool.VDS_NETWORK_INTERFACE.equals(iface.getId());
        }
        assertTrue(found);
        assertNotNull(networkQoSDao.get(FixturesTool.VDS_NETWORK_INTERFACE));

        dao.removeInterfaceFromVds(FixturesTool.VDS_NETWORK_INTERFACE);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);

        for (VdsNetworkInterface iface : after) {
            assertNotSame(FixturesTool.VDS_NETWORK_INTERFACE, iface.getId());
        }
        assertNull(networkQoSDao.get(FixturesTool.VDS_NETWORK_INTERFACE));
    }

    /**
     * Ensures that statistics are removed for the specified VDS interface, in which case it shouldn't be returned by
     * the Dao (as the interface view is an inner join with the statistics).
     */
    @Test
    public void testRemoveStatisticsForVdsInterface() {
        VdsNetworkInterface before = dao.get(FixturesTool.VDS_NETWORK_INTERFACE);
        assertNotNull(before);

        dao.removeStatisticsForVds(FixturesTool.VDS_NETWORK_INTERFACE);

        VdsNetworkInterface after = dao.get(FixturesTool.VDS_NETWORK_INTERFACE);
        assertNull(after);
    }

    private void testUpdateInterface(Guid interface_id) {
        VdsNetworkInterface iface = dao.get(interface_id);

        iface.setName(iface.getName().toUpperCase());
        iface.setQos(newQos);

        dao.updateInterfaceForVds(iface);

        VdsNetworkInterface ifaced = dao.get(interface_id);
        assertEquals(iface.getName(), ifaced.getName());
        assertEquals(iface.getQos(), ifaced.getQos());

        verifyIpv6Properties(iface, ifaced);
    }

    private void verifyIpv6Properties(VdsNetworkInterface nicA, VdsNetworkInterface nicB) {
        assertEquals(nicA.getIpv6BootProtocol(), nicB.getIpv6BootProtocol());
        assertEquals(nicA.getIpv6Address(), nicB.getIpv6Address());
        assertEquals(nicA.getIpv6Prefix(), nicB.getIpv6Prefix());
        assertEquals(nicA.getIpv6Gateway(), nicB.getIpv6Gateway());
    }

    /**
     * Ensures updating an interface works, while also updating its previous QoS configuration.
     */
    @Test
    public void testUpdateInterfaceWithQos() {
        testUpdateInterface(FixturesTool.VDS_NETWORK_INTERFACE);
    }

    /**
     * Ensures updating an interface works, including a newly-reported QoS configuration.
     */
    @Test
    public void testUpdateInterfaceWithoutQos() {
        testUpdateInterface(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);
    }

    @Test
    public void testMasshUpdateStatisticsForVds() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesForVds(VDS_ID);
        List<VdsNetworkStatistics> statistics = new ArrayList<>(interfaces.size());
        for (VdsNetworkInterface iface : interfaces) {
            VdsNetworkStatistics stats = iface.getStatistics();
            stats.setReceiveDrops(new BigInteger(String.valueOf(RandomUtils.instance().nextInt())));
            stats.setStatus(RandomUtils.instance().nextEnum(InterfaceStatus.class));
            statistics.add(stats);
        }

        dao.massUpdateStatisticsForVds(statistics);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);
        for (VdsNetworkInterface iface : after) {
            boolean found = false;
            for (VdsNetworkStatistics stats : statistics) {
                if (iface.getId().equals(stats.getId())) {
                    found = true;
                    assertEquals(stats.getReceiveDrops(), iface.getStatistics().getReceiveDrops());
                    assertEquals(stats.getStatus(), iface.getStatistics().getStatus());
                }
            }
            assertTrue(found);
        }
    }

    /**
     * Asserts that the right collection containing network interfaces is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllInterfacesForVdsWithPermissionsForPriviligedUser() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID, PRIVILEGED_USER_ID, true);
        assertGetAllForVdsCorrectResult(result);
    }

    /**
     * Asserts that an empty collection is returned for an non privileged user with filtering enabled
     */
    @Test
    public void testGetAllInterfacesForVdsWithPermissionsForUnpriviligedUser() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID, UNPRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing network interfaces is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllInterfacesForVdsWithPermissionsDisabledForUnpriviligedUser() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID, UNPRIVILEGED_USER_ID, false);
        assertGetAllForVdsCorrectResult(result);
    }

    private void assertGetAllForVdsCorrectResult(List<VdsNetworkInterface> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsNetworkInterface iface : result) {
            assertEquals(VDS_ID, iface.getVdsId());
            if (FixturesTool.VDS_NETWORK_INTERFACE.equals(iface.getId())) {
                assertNotNull(iface.getQos());
                assertEquals(QosType.HOSTNETWORK, iface.getQos().getQosType());
            } else {
                assertNull(iface.getQos());
            }
        }
    }

    /**
     * Asserts that a null result is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetManagedInterfaceForVdsFilteredForUnpriviligedUser() {
        VdsNetworkInterface result = dao.getManagedInterfaceForVds(VDS_ID, UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
     * Asserts that the management network interface of a VDS is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetManagedInterfaceForVdsFilteredForPriviligedUser() {
        VdsNetworkInterface result = dao.getManagedInterfaceForVds(VDS_ID, PRIVILEGED_USER_ID, true);
        assertCorrectGetManagedInterfaceForVdsResult(result);
    }

    /**
     * Asserts that the management network interface of a VDS is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetManagedInterfaceForVdsFilteringDisabledForUnpriviligedUser() {
        VdsNetworkInterface result = dao.getManagedInterfaceForVds(VDS_ID, UNPRIVILEGED_USER_ID, false);
        assertCorrectGetManagedInterfaceForVdsResult(result);
    }

    /**
     * Ensures that get works as expected.
     */
    @Test
    public void testGet() {
        newVdsInterface.setVdsId(VDS_ID);
        dao.saveInterfaceForVds(newVdsInterface);
        dao.saveStatisticsForVds(newVdsInterface.getStatistics());
        VdsNetworkInterface result = dao.get(newVdsInterface.getId());
        assertEquals(newVdsInterface, result);
    }

    /**
     * Asserts that the correct VdsNetworkInterface is returned for the given network.
     */
    @Test
    public void testGetVdsInterfacesByNetworkId() {
        List<VdsNetworkInterface> result = dao.getVdsInterfacesByNetworkId(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingVdsInterface, result.get(0));
    }

    private static void assertCorrectGetManagedInterfaceForVdsResult(VdsNetworkInterface result) {
        assertNotNull(result);
        assertTrue(result.getIsManagement());
    }

    @Test
    public void testGetAllInterfacesWithIpAddress() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesWithIpAddress(FixturesTool.CLUSTER, IP_ADDR);
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertGetAllForVdsCorrectResult(interfaces);
    }

    @Test
    public void testGetAllInterfacesWithIpv6AddressExist() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesWithIpAddress(FixturesTool.CLUSTER, FixturesTool.IPV6_ADDR_EXISTS);
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertGetAllForVdsCorrectResult(interfaces);
    }

    @Test
    public void testGetAllInterfacesWithIpv6AddressNotExist() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesWithIpAddress(FixturesTool.CLUSTER, FixturesTool.IPV6_ADDR_NOT_EXIST);
        assertNotNull(interfaces);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void testGetAllInterfacesByClusterId() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesByClusterId(FixturesTool.CLUSTER);
        assertGetAllForVdsCorrectResult(interfaces);
        testQosAppendedToResultSet(interfaces);
    }

    @Test
    public void testGetAllInterfacesByLabelForCluster() {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesByLabelForCluster(FixturesTool.CLUSTER, LABEL);
        assertNotNull(interfaces);
        assertFalse(interfaces.isEmpty());

        for (VdsNetworkInterface nic : interfaces) {
            assertTrue(nic.getLabels().contains(LABEL));
        }
    }

    @Test
    public void testGetAllNetworkLabelsForDataCenter() {
        Set<String> result = dao.getAllNetworkLabelsForDataCenter(FixturesTool.DATA_CENTER);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetHostNetworksByCluster() {
        Map<Guid, List<String>> map = dao.getHostNetworksByCluster(FixturesTool.CLUSTER);
        assertNotNull(map);
        assertFalse(map.isEmpty());
        assertNotNull(map.get(VDS_ID));
        assertFalse(map.get(VDS_ID).isEmpty());
    }

    @Test
    public void testGetIscsiIfacesByHostIdAndStorageTargetId() {
        List<VdsNetworkInterface> interfaces =
                dao.getIscsiIfacesByHostIdAndStorageTargetId(VDS_ID, TARGET_ID);

        assertNotNull(interfaces);
        assertFalse(interfaces.isEmpty());

        for (VdsNetworkInterface nic : interfaces) {
            assertEquals(VDS_ID, nic.getVdsId());
        }
    }

    @Test
    public void massClearNetworkFromNicsTest() {
        VdsNetworkInterface nic1 = dao.get(FixturesTool.VDS_NETWORK_INTERFACE);
        VdsNetworkInterface nic2 = dao.get(FixturesTool.VDS_NETWORK_INTERFACE_WITHOUT_QOS);
        VdsNetworkInterface nic3 = dao.get(FixturesTool.VDS_NETWORK_INTERFACE2);

        assertNotNull(nic1.getNetworkName());
        assertNotNull(nic2.getNetworkName());
        assertNotNull(nic3.getNetworkName());

        dao.massClearNetworkFromNics(Arrays.asList(nic1.getId(), nic2.getId()));

        nic1 = dao.get(nic1.getId());
        nic2 = dao.get(nic2.getId());
        nic3 = dao.get(nic3.getId());

        assertNull(nic1.getNetworkName());
        assertNull(nic2.getNetworkName());
        assertNotNull(nic3.getNetworkName());
    }

    @Test
    public void testGetByName() {
        VdsNetworkInterface result = dao.get(existingVdsInterface.getVdsId(), existingVdsInterface.getName());
        assertEquals(existingVdsInterface, result);
    }
}
