/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

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
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackSubnetResourceTest
        extends AbstractBackendSubResourceTest<OpenStackSubnet, ExternalSubnet, BackendOpenStackSubnetResource> {
    public BackendOpenStackSubnetResourceTest() {
        super(new BackendOpenStackSubnetResource(GUIDS[0].toString(), string2hex(NAMES[1]), string2hex(NAMES[2]), null));
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
        verifyModel(resource.get(), 2);
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
            QueryType.GetExternalSubnetsOnProviderByExternalNetwork,
            GetExternalSubnetsOnProviderByExternalNetworkQueryParameters.class,
            new String[] { "ProviderId", "NetworkId" },
            new Object[] { GUIDS[0], string2hex(NAMES[1]) },
            notFound? null: getSubnets()
        );
    }

    private List<ExternalSubnet> getSubnets() {
        List<ExternalSubnet> subnets = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            subnets.add(getEntity(i));
        }
        return subnets;
    }

    @Override
    protected ExternalSubnet getEntity(int index) {
        ExternalSubnet subnet = mock(ExternalSubnet.class);
        when(subnet.getId()).thenReturn(string2hex(NAMES[index]));
        when(subnet.getName()).thenReturn(NAMES[index]);
        return subnet;
    }

    @Override
    protected void verifyModel(OpenStackSubnet model, int index) {
        assertEquals(string2hex(NAMES[index]), model.getId());
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
