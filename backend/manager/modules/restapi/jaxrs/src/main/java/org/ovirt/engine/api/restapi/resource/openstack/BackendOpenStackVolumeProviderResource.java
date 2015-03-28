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

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.resource.openstack.OpenStackVolumeProviderResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.core.common.businessentities.Provider;

import static org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackVolumeProvidersResource.SUB_COLLECTIONS;

public class BackendOpenStackVolumeProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackVolumeProvider>
        implements OpenStackVolumeProviderResource {

    private BackendOpenStackVolumeProvidersResource parent;

    public BackendOpenStackVolumeProviderResource(String id, BackendOpenStackVolumeProvidersResource parent) {
        super(id, OpenStackVolumeProvider.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    protected OpenStackVolumeProvider doPopulate(OpenStackVolumeProvider model, Provider entity) {
        return parent.doPopulate(model, entity);
    }

    BackendOpenStackVolumeProvidersResource getParent() {
        return parent;
    }
}
