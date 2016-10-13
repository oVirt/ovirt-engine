package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class DnsResolverConfigurationDaoTest extends BaseDaoTestCase {

    private DnsResolverConfigurationDao dao;
    private DnsResolverConfiguration dnsResolverConfiguration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getDnsResolverConfigurationDao();
        dnsResolverConfiguration = new DnsResolverConfiguration();
        dnsResolverConfiguration.setNameServers(new ArrayList<>(Arrays.asList(new NameServer("1.1.1.1"))));
    }

    /**
     * Ensures that the DnsResolverConfiguration is returned.
     */
    @Test
    public void testGet() {
        DnsResolverConfiguration result = dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION);

        assertNotNull(result);
        assertEquals(dnsResolverConfigurationFromFixtures(), result);
    }

    public DnsResolverConfiguration dnsResolverConfigurationFromFixtures() {
        DnsResolverConfiguration expected = new DnsResolverConfiguration();

        expected.setId(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION);
        expected.setNameServers(Arrays.asList(
                new NameServer("192.168.1.2"),
                new NameServer("2002:0db8:85a3:0000:0000:8a2e:0370:7334")
        ));

        return expected;
    }

    /**
     * Ensures that a populated collection is returned.
     */
    @Test
    public void testGetAll() {
        List<DnsResolverConfiguration> result = dao.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testRemoveByNetworkAttachmentId() {
        assertNotNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
        dao.removeByNetworkAttachmentId(FixturesTool.NETWORK_ATTACHMENT);
        assertNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
    }

    @Test
    public void testRemoveByNetworkId() {
        assertNotNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
        dao.removeByNetworkId(FixturesTool.NETWORK_ENGINE);
        assertNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
    }

    @Test
    public void testRemoveByVdsDynamicId() {
        assertNotNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
        dao.removeByVdsDynamicId(FixturesTool.GLUSTER_SERVER_UUID3);
        assertNull(dao.get(FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION));
    }

    /**
     * Ensures that the save is working correctly
     */
    @Test
    public void testSave() {
        dao.save(dnsResolverConfiguration);
        DnsResolverConfiguration result = dao.get(dnsResolverConfiguration.getId());

        assertNotNull(result);
        assertEquals(dnsResolverConfiguration, result);
    }

    /**
     * Ensures that the update is working correctly
     */
    @Test
    public void testUpdate() {
        dao.save(dnsResolverConfiguration);

        dnsResolverConfiguration.getNameServers().add(new NameServer("2.2.2.2"));

        dao.update(dnsResolverConfiguration);
        DnsResolverConfiguration result = dao.get(dnsResolverConfiguration.getId());
        assertNotNull(result);
        assertEquals(dnsResolverConfiguration, result);
    }

    /**
     * Ensures that the remove is working correctly
     */
    @Test
    public void testRemove() {
        dao.save(dnsResolverConfiguration);
        DnsResolverConfiguration result = dao.get(dnsResolverConfiguration.getId());
        assertNotNull(result);
        dao.remove(dnsResolverConfiguration.getId());
        assertNull(dao.get(dnsResolverConfiguration.getId()));
    }
}
