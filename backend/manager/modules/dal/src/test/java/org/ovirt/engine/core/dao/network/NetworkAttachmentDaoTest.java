package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;


public class NetworkAttachmentDaoTest extends BaseDaoTestCase {

    private NetworkAttachment networkAttachment;
    private NetworkAttachmentDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getNetworkAttachmentDao();
        networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(FixturesTool.VDS_NETWORK_INTERFACE);
        networkAttachment.setProperties(new HashMap<String, String>());
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNetworkId(FixturesTool.NETWORK_ENGINE);
        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setBootProtocol(NetworkBootProtocol.DHCP);
        ipConfiguration.getIPv4Addresses().add(new IPv4Address());
        networkAttachment.setIpConfiguration(ipConfiguration);
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
        ipConfiguration.setBootProtocol(NetworkBootProtocol.DHCP);
        ipConfiguration.getIPv4Addresses().add(createPrimaryAddress());
        expected.setIpConfiguration(ipConfiguration);

        Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "true");
        properties.put("prop2", "123456");

        expected.setProperties(properties);

        return expected;
    }

    public IPv4Address createPrimaryAddress() {
        return new IPv4Address();
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
        IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        ipConfiguration.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        IPv4Address primaryAddress = new IPv4Address();
        primaryAddress.setAddress("192.168.1.2");
        primaryAddress.setGateway("192.168.1.1");
        primaryAddress.setNetmask("255.255.255.0");
        ipConfiguration.setIPv4Addresses(Collections.singletonList(primaryAddress));

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
}
