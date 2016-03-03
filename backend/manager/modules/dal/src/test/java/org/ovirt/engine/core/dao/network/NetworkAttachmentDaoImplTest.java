package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class NetworkAttachmentDaoImplTest extends BaseDaoTestCase {

    private NetworkAttachment networkAttachment;
    private NetworkAttachmentDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getNetworkAttachmentDao();
        networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(FixturesTool.VDS_NETWORK_INTERFACE);
        networkAttachment.setProperties(new HashMap<>());
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNetworkId(FixturesTool.NETWORK_ENGINE);
        networkAttachment.setIpConfiguration(createIpConfiguration(Ipv4BootProtocol.DHCP, Ipv6BootProtocol.AUTOCONF));
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        NetworkAttachment result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the network attachment is returned.
     */
    @Test
    public void testGet() {
        NetworkAttachment result = dao.get(FixturesTool.NETWORK_ATTACHMENT);

        assertNotNull(result);

        assertNetworkAttachmentEquals(networkAttachmentFromFixtures(), result);
    }

    public NetworkAttachment networkAttachmentFromFixtures() {
        NetworkAttachment expected = new NetworkAttachment();

        expected.setId(FixturesTool.NETWORK_ATTACHMENT);
        expected.setNetworkId(FixturesTool.NETWORK_ENGINE_2);
        expected.setNicId(FixturesTool.VDS_NETWORK_INTERFACE2);
        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.getIPv4Addresses().add(createPrimaryIpv4Address());
        ipConfiguration.getIpV6Addresses().add(createPrimaryIpv6Address());
        expected.setIpConfiguration(ipConfiguration);

        Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "true");
        properties.put("prop2", "123456");

        expected.setProperties(properties);

        return expected;
    }

    public IPv4Address createPrimaryIpv4Address() {
        IPv4Address iPv4Address = new IPv4Address();
        iPv4Address.setBootProtocol(Ipv4BootProtocol.DHCP);
        return iPv4Address;
    }

    public IpV6Address createPrimaryIpv6Address() {
        IpV6Address ipv6Address = new IpV6Address();
        ipv6Address.setBootProtocol(Ipv6BootProtocol.DHCP);
        return ipv6Address;
    }

    /**
     * Ensures that network attachments are returned.
     */
    @Test
    public void testGetAllForNic() {
        List<NetworkAttachment> result = dao.getAllForNic(FixturesTool.NETWORK_ATTACHMENT_NIC);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Ensures that network attachments are returned.
     */
    @Test
    public void testGetAllForHost() {
        List<NetworkAttachment> result = dao.getAllForHost(FixturesTool.NETWORK_ATTACHMENT_HOST);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testRemoveByNetworkId() {
        //store ids for for case it gets changed during processing somehow.
        Guid networkAttachmentId = networkAttachment.getId();
        Guid networkIdUsedForAttachmentRemoval = networkAttachment.getNetworkId();

        //persist & verify presence
        dao.save(networkAttachment);
        assertNotNull(dao.get(networkAttachmentId));

        //delete & verify absence
        dao.removeByNetworkId(networkIdUsedForAttachmentRemoval);
        assertNull(dao.get(networkAttachmentId));
    }

    /**
     * Ensures that a populated collection is returned.
     */
    @Test
    public void testGetAll() {
        List<NetworkAttachment> result = dao.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    /**
     * Ensures that the save is working correctly
     */
    @Test
    public void testSave() {
        dao.save(networkAttachment);
        NetworkAttachment result = dao.get(networkAttachment.getId());
        assertNotNull(result);

        assertNetworkAttachmentEquals(networkAttachment, result);
    }

    private void assertNetworkAttachmentEquals(NetworkAttachment expected, NetworkAttachment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getNetworkId(), actual.getNetworkId());
        assertEquals(expected.getNicId(), actual.getNicId());
        assertEquals(expected.getNicName(), actual.getNicName());
        assertEquals(expected.getIpConfiguration(), actual.getIpConfiguration());
        assertTrue(expected.getProperties().entrySet().equals(actual.getProperties().entrySet()));
    }

    /**
     * Ensures that the update is working correctly
     */
    @Test
    public void testUpdate() {
        networkAttachment.setNicId(FixturesTool.NETWORK_ATTACHMENT_NIC);
        dao.save(networkAttachment);
        IpConfiguration ipConfiguration = populateIpConfiguration(networkAttachment.getIpConfiguration(),
                Ipv4BootProtocol.STATIC_IP,
                Ipv6BootProtocol.NONE);

        networkAttachment.setIpConfiguration(ipConfiguration);

        Map<String, String> properties = new HashMap<>();
        properties.put("key", "value");
        networkAttachment.setProperties(properties);

        networkAttachment.setNicId(FixturesTool.NETWORK_ATTACHMENT_NIC2);

        dao.update(networkAttachment);
        NetworkAttachment result = dao.get(networkAttachment.getId());
        assertNotNull(result);
        assertNetworkAttachmentEquals(networkAttachment, result);
    }

    private IpConfiguration createIpConfiguration(
            Ipv4BootProtocol ipv4BootProtocol,
            Ipv6BootProtocol ipv6BootProtocol) {
        return populateIpConfiguration(new IpConfiguration(), ipv4BootProtocol, ipv6BootProtocol);
    }

    private IpConfiguration populateIpConfiguration(IpConfiguration ipConfiguration,
            Ipv4BootProtocol ipv4BootProtocol,
            Ipv6BootProtocol ipv6BootProtocol) {

        ipConfiguration.setIPv4Addresses(Collections.singletonList(createIpv4Address(ipv4BootProtocol)));
        ipConfiguration.setIpV6Addresses(Collections.singletonList(createIpv6Address(ipv6BootProtocol)));

        return ipConfiguration;
    }

    private IPv4Address createIpv4Address(Ipv4BootProtocol ipv4BootProtocol) {
        IPv4Address primaryIpv4Address = new IPv4Address();
        primaryIpv4Address.setBootProtocol(ipv4BootProtocol);
        primaryIpv4Address.setAddress(randomIpv4Address());
        primaryIpv4Address.setGateway(randomIpv4Address());
        primaryIpv4Address.setNetmask(randomIpv4Address());
        return primaryIpv4Address;
    }

    private IpV6Address createIpv6Address(Ipv6BootProtocol ipv6BootProtocol) {
        IpV6Address primaryIpv6Address = new IpV6Address();
        primaryIpv6Address.setBootProtocol(ipv6BootProtocol);
        primaryIpv6Address.setAddress(randomIpv6Address());
        primaryIpv6Address.setGateway(randomIpv6Address());
        primaryIpv6Address.setPrefix(64);
        return primaryIpv6Address;
    }

    private String randomIpv4Address() {
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }

    private String randomIpv6Address() {
        final Random r = new Random();
        final byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) r.nextInt(256);
        }
        try {
            return Inet6Address.getByAddress(bytes).toString();
        } catch (UnknownHostException e) {
            return "1::2";
        }
    }

    /**
     * Ensures that the remove is working correctly
     */
    @Test
    public void testRemove() {
        dao.save(networkAttachment);
        NetworkAttachment result = dao.get(networkAttachment.getId());
        assertNotNull(result);
        dao.remove(networkAttachment.getId());
        assertNull(dao.get(networkAttachment.getId()));
    }

    @Test
    public void testGetAllForNetwork() {
        final List<NetworkAttachment> allForNetwork = dao.getAllForNetwork(FixturesTool.NETWORK_NO_CLUSTERS_ATTACHED);
        assertNotNull(allForNetwork);
        assertEquals(2, allForNetwork.size());
    }

    @Test
    public void testGetNetworkAttachmentByNicIdAndNetworkId() {
        final Guid nicId = FixturesTool.VDS_NETWORK_INTERFACE2;
        final Guid networkId = FixturesTool.NETWORK_ENGINE_2;
        final NetworkAttachment networkAttachmentByNicIdAndNetworkId =
                dao.getNetworkAttachmentByNicIdAndNetworkId(nicId, networkId);

        assertNotNull(networkAttachmentByNicIdAndNetworkId);
        assertNetworkAttachmentEquals(networkAttachmentFromFixtures(), networkAttachmentByNicIdAndNetworkId);
    }
}
