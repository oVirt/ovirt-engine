package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class DnsResolverConfigurationDaoTest
        extends BaseGenericDaoTestCase<Guid, DnsResolverConfiguration, DnsResolverConfigurationDao> {

    @Override
    protected DnsResolverConfiguration generateNewEntity() {
        DnsResolverConfiguration dnsResolverConfiguration = new DnsResolverConfiguration();
        dnsResolverConfiguration.setNameServers(new ArrayList<>(Arrays.asList(new NameServer("1.1.1.1"))));
        return dnsResolverConfiguration;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.getNameServers().add(new NameServer("2.2.2.2"));
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.EXISTING_DNS_RESOLVER_CONFIGURATION;
    }

    @Override
    protected DnsResolverConfigurationDao prepareDao() {
        return dbFacade.getDnsResolverConfigurationDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 2;
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
}
