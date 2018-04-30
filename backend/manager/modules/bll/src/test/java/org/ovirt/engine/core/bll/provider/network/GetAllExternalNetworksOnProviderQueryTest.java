package org.ovirt.engine.core.bll.provider.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private GetAllExternalNetworksOnProviderQuery<IdQueryParameters> query;

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteQueryCommand() {
        when(params.getId()).thenReturn(mock(Guid.class));
        when((Provider<AdditionalProperties>) providerDao.get(any())).thenReturn(networkProvider);
        when(providerProxyFactory.create(networkProvider)).thenReturn(client);

        Network network = mock(Network.class);
        ProviderNetwork providerNetwork = mock(ProviderNetwork.class);
        when(client.getAll()).thenReturn(Collections.singletonList(network));
        when(network.getProvidedBy()).thenReturn(providerNetwork);
        when(providerNetwork.getExternalId()).thenReturn("");

        Guid id = mock(Guid.class);
        when(dcDao.getDcIdByExternalNetworkId(any())).thenReturn(Collections.singletonList(id));

        Map<Network, Set<Guid>> expected = new HashMap<>();
        expected.put(network, Collections.singleton(id));

        query = getQuery();
        query.executeQueryCommand();

        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }
}
