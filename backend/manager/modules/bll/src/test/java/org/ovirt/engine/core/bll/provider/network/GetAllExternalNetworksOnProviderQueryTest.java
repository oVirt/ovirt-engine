package org.ovirt.engine.core.bll.provider.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@RunWith(MockitoJUnitRunner.class)
public class GetAllExternalNetworksOnProviderQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetAllExternalNetworksOnProviderQuery<IdQueryParameters>> {

    @Mock
    private Provider<AdditionalProperties> networkProvider;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private StoragePoolDao dcDao;

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

        Network network = mock(Network.class);
        ProviderNetwork providerNetwork = mock(ProviderNetwork.class);
        when(client.getAll()).thenReturn(Arrays.asList(network));
        when(network.getProvidedBy()).thenReturn(providerNetwork);
        when(providerNetwork.getExternalId()).thenReturn("");

        Guid id = mock(Guid.class);
        when(getDbFacadeMockInstance().getStoragePoolDao()).thenReturn(dcDao);
        when(dcDao.getDcIdByExternalNetworkId(any(String.class))).thenReturn(Arrays.asList(id));

        Map<Network, Set<Guid>> expected = new HashMap<>();
        expected.put(network, Collections.singleton(id));

        GetAllExternalNetworksOnProviderQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
