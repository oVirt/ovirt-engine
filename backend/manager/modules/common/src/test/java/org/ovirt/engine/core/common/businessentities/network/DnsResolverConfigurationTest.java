package org.ovirt.engine.core.common.businessentities.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DnsResolverConfigurationTest {
    @Test
    public void testToString() throws Exception {
        List<NameServer> nameServers =
                Arrays.asList(new NameServer("192.168.1.1"), new NameServer("192.168.1.2"));

        DnsResolverConfiguration dnsResolverConfiguration = new DnsResolverConfiguration();
        dnsResolverConfiguration.setNameServers(nameServers);

        assertThat(dnsResolverConfiguration.toString(),
                is("DnsResolverConfiguration:{id='null', nameServers='[NameServer:{address='192.168.1.1'}, "
                        + "NameServer:{address='192.168.1.2'}]'}"));
    }

}
