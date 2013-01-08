package org.ovirt.engine.core.bll.provider.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@RunWith(MockitoJUnitRunner.class)
public class GetAllExternalNetworksOnProviderQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetAllExternalNetworksOnProviderQuery<IdQueryParameters>> {

    @Mock
    private Provider<AdditionalProperties> networkProvider;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private NetworkProviderProxy client;

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteQueryCommand() {
        when(params.getId()).thenReturn(mock(Guid.class));
        when(getDbFacadeMockInstance().getProviderDao()).thenReturn(providerDao);
        when((Provider<AdditionalProperties>) providerDao.get(any(Guid.class))).thenReturn(networkProvider);
        when(getQuery().getProviderProxyFactory()).thenReturn(providerProxyFactory);
        when(providerProxyFactory.create(networkProvider)).thenReturn(client);

        List<Network> expected = Arrays.asList(mock(Network.class));
        when(client.getAll()).thenReturn(expected);

        GetAllExternalNetworksOnProviderQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
