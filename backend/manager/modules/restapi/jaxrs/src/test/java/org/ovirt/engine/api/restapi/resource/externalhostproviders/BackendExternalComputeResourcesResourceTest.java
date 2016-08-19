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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalComputeResourcesResourceTest
        extends AbstractBackendCollectionResourceTest<
            ExternalComputeResource,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource,
            BackendExternalComputeResourcesResource
        > {
    public BackendExternalComputeResourcesResourceTest() {
        super(
            new BackendExternalComputeResourcesResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<ExternalComputeResource> getCollection() {
        return collection.list().getExternalComputeResources();
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
            VdcQueryType.GetComputeResourceFromExternalProvider,
            ProviderQueryParameters.class,
            new String[] { "Provider.Id" },
            new Object[] { GUIDS[0] },
            getResources(),
            failure
        );
    }

    private Provider getProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[0]);
        when(provider.getName()).thenReturn(NAMES[0]);
        return provider;
    }

    private List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> getResources() {
        List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> resources = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            resources.add(getEntity(i));
        }
        return resources;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.ExternalComputeResource getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.ExternalComputeResource resource =
                mock(org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class);
        when(resource.getName()).thenReturn(NAMES[index]);
        return resource;
    }

    @Override
    protected void verifyModel(ExternalComputeResource model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
