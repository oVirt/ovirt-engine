package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;


public class GetDnsResolverConfigurationByIdQueryTest extends
        AbstractQueryTest<IdQueryParameters, GetDnsResolverConfigurationByIdQuery<IdQueryParameters>> {

    @Mock
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    @Test
    public void testExecuteQuery() {
        // Set up the query parameters
        Guid dnsResolverConfigurationId = Guid.newGuid();
        when(params.getId()).thenReturn(dnsResolverConfigurationId);

        // Set up the Daos
        DnsResolverConfiguration dnsResolverConfiguration = new DnsResolverConfiguration();
        dnsResolverConfiguration.setNameServers(Collections.singletonList(new NameServer("1.2.3.4")));
        when(dnsResolverConfigurationDao.get(dnsResolverConfigurationId)).thenReturn(dnsResolverConfiguration);

        // Run the query
        GetDnsResolverConfigurationByIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals(dnsResolverConfiguration, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");

    }
}
