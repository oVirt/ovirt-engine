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

import static org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackNetworkProvidersResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworksResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderCertificatesResource;

public class BackendOpenStackNetworkProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackNetworkProvider>
        implements OpenStackNetworkProviderResource {
    public BackendOpenStackNetworkProviderResource(String id) {
        super(id, OpenStackNetworkProvider.class, SUB_COLLECTIONS);
    }

    @Override
    public OpenStackNetworksResource getOpenStackNetworks() {
        return inject(new BackendOpenStackNetworksResource(id));
    }

    @Override
    public ExternalProviderCertificatesResource getCertificates() {
        return inject(new BackendExternalProviderCertificatesResource(id));
    }
}
