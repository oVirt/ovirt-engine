/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackSubnetResourceTest
        extends AbstractBackendSubResourceTest<OpenStackSubnet, ExternalSubnet, BackendOpenStackSubnetResource> {
    public BackendOpenStackSubnetResourceTest() {
        super(new BackendOpenStackSubnetResource(GUIDS[0].toString(), string2hex(NAMES[1]), string2hex(NAMES[2]), null));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        control.replay();
        verifyModel(resource.get(), 2);
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetExternalSubnetsOnProviderByExternalNetwork,
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
        ExternalSubnet subnet = control.createMock(ExternalSubnet.class);
        expect(subnet.getId()).andReturn(string2hex(NAMES[index])).anyTimes();
        expect(subnet.getName()).andReturn(NAMES[index]).anyTimes();
        return subnet;
    }

    @Override
    protected void verifyModel(OpenStackSubnet model, int index) {
        assertEquals(string2hex(NAMES[index]), model.getId());
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
