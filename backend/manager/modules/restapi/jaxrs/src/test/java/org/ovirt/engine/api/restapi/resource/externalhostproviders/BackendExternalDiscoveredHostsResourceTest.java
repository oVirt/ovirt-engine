/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalDiscoveredHostsResourceTest
        extends AbstractBackendCollectionResourceTest<
            ExternalDiscoveredHost,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost,
        BackendExternalDiscoveredHostsResource
        > {
    public BackendExternalDiscoveredHostsResourceTest() {
        super(
            new BackendExternalDiscoveredHostsResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<ExternalDiscoveredHost> getCollection() {
        return collection.list().getExternalDiscoveredHosts();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
            QueryType.GetProviderById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getProvider()
        );
        setUpEntityQueryExpectations(
            QueryType.GetDiscoveredHostListFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            getHosts(),
            failure
        );
    }

    private Provider getProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[0]);
        when(provider.getName()).thenReturn(NAMES[0]);
        return provider;
    }

    private List<org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost> getHosts() {
        List<org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost> hosts = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            hosts.add(getEntity(i));
        }
        return hosts;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost host =
                mock(org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        when(host.getIp()).thenReturn(NAMES[index]);
        when(host.getName()).thenReturn(NAMES[index]);
        return host;
    }

    @Override
    protected void verifyModel(ExternalDiscoveredHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
