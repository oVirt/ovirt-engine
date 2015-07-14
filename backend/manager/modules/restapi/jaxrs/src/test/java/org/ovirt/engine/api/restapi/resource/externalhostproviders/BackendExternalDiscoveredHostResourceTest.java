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

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    public void testBadId() throws Exception {
        control.replay();
        try {
            new BackendExternalHostProviderResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
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
        verifyModel(resource.get(), 1);
    }

    private Provider getProvider() {
        Provider provider = control.createMock(Provider.class);
        expect(provider.getId()).andReturn(GUIDS[0]).anyTimes();
        expect(provider.getName()).andReturn(NAMES[0]).anyTimes();
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
                control.createMock(org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        expect(provider.getIp()).andReturn(NAMES[index]).anyTimes();
        expect(provider.getName()).andReturn(NAMES[index]).anyTimes();
        return provider;
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpEntityQueryExpectations(
             VdcQueryType.GetProviderById,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { GUIDS[0] },
             getProvider()
        );
        setUpGetEntityExpectations(
            VdcQueryType.GetDiscoveredHostListFromExternalProvider,
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
