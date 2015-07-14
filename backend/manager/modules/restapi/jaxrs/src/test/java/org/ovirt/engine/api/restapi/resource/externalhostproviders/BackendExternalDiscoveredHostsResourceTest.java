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

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetProviderById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getProvider()
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetDiscoveredHostListFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            getHosts(),
            failure
        );
        control.replay();
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
        org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost host =
                control.createMock(org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        expect(host.getIp()).andReturn(NAMES[index]).anyTimes();
        expect(host.getName()).andReturn(NAMES[index]).anyTimes();
        return host;
    }

    @Override
    protected void verifyModel(ExternalDiscoveredHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
