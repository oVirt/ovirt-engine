/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackNetworkResourceTest
        extends AbstractBackendSubResourceTest<OpenStackNetwork, Network, BackendOpenStackNetworkResource> {
    public BackendOpenStackNetworkResourceTest() {
        super(new BackendOpenStackNetworkResource(GUIDS[0].toString(), string2hex(NAMES[1])));
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

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
            QueryType.GetAllExternalNetworksOnProvider,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            notFound? null: getNetworks()
        );
    }

    private Map<Network, Set<Guid>> getNetworks() {
        Map<Network, Set<Guid>> networks = new HashMap<>();
        for (int i = 0; i < NAMES.length; i++) {
            networks.put(getEntity(i), new HashSet<>());
        }
        return networks;
    }

    @Override
    protected Network getEntity(int index) {
        Network network = mock(Network.class);
        when(network.getId()).thenReturn(GUIDS[index]);
        when(network.getName()).thenReturn(NAMES[index]);
        ProviderNetwork providedBy = new ProviderNetwork();
        providedBy.setProviderId(GUIDS[0]);
        providedBy.setExternalId(string2hex(NAMES[index]));
        when(network.getProvidedBy()).thenReturn(providedBy);
        return network;
    }

    @Override
    protected void verifyModel(OpenStackNetwork model, int index) {
        // The model can't be verified based on the index because the backend returns a map, that the resource
        // translates into a list, and the order of that list may not be the same that the original map.
        verifyLinks(model);
    }
}
