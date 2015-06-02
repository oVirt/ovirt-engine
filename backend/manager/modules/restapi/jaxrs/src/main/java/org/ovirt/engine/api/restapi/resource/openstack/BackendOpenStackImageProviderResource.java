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

import static org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackImageProvidersResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.resource.openstack.OpenStackImageProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenStackImagesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;

public class BackendOpenStackImageProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackImageProvider>
        implements OpenStackImageProviderResource {
    public BackendOpenStackImageProviderResource(String id) {
        super(id, OpenStackImageProvider.class, SUB_COLLECTIONS);
    }

    @Override
    public OpenStackImagesResource getOpenStackImages() {
        return inject(new BackendOpenStackImagesResource(id));
    }

    @Override
    public Response remove() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(VdcActionType.RemoveProvider, parameters);
    }
}
