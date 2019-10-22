/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalDiscoveredHostResourceTest
        extends AbstractBackendSubResourceTest<
            ExternalDiscoveredHost,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost,
        BackendExternalDiscoveredHostResource
        > {
    public BackendExternalDiscoveredHostResourceTest() {
        super(new BackendExternalDiscoveredHostResource(string2hex(NAMES[1]), GUIDS[0].toString()));
    }

    @Test
    public void testBadId() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendExternalHostProviderResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        verifyModel(resource.get(), 1);
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
        org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost provider =
                mock(org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        when(provider.getIp()).thenReturn(NAMES[index]);
        when(provider.getName()).thenReturn(NAMES[index]);
        return provider;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
             QueryType.GetProviderById,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { GUIDS[0] },
             getProvider()
        );
        setUpGetEntityExpectations(
            QueryType.GetDiscoveredHostListFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            notFound? null: getHosts()
        );
    }

    @Override
    protected void verifyModel(ExternalDiscoveredHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
