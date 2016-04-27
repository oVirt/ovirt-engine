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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;

public class BackendOpenStackNetworkProvidersResourceTest extends
        AbstractBackendCollectionResourceTest<OpenStackNetworkProvider, Provider, BackendOpenStackNetworkProvidersResource> {

    public BackendOpenStackNetworkProvidersResourceTest() {
        super(
            new BackendOpenStackNetworkProvidersResource(),
            SearchType.Provider,
            "Providers: type=" + ProviderType.OPENSTACK_NETWORK.name()
        );
    }

    @Override
    protected List<OpenStackNetworkProvider> getCollection() {
        return collection.list().getOpenStackNetworkProviders();
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        if (StringUtils.isNotBlank(query)) {
            query = " AND (" + query + ")";
        }
        super.setUpQueryExpectations(query);
    }

    @Override
    protected Provider getEntity(int index) {
        Provider provider = control.createMock(Provider.class);
        expect(provider.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(provider.getName()).andReturn(NAMES[index]).anyTimes();
        expect(provider.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(provider.getType()).andReturn(ProviderType.OPENSTACK_NETWORK).anyTimes();
        return provider;
    }
}
