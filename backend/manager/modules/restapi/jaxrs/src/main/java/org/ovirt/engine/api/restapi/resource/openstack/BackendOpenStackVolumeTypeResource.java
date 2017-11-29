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

import java.util.List;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackVolumeTypeResource
        extends AbstractBackendActionableResource<OpenStackVolumeType, CinderVolumeType>
        implements OpenstackVolumeTypeResource {
    private String providerId;

    protected BackendOpenStackVolumeTypeResource(String providerId, String id) {
        super(id, OpenStackVolumeType.class, CinderVolumeType.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackVolumeType get() {
        Guid storageDomainId = BackendOpenStackStorageProviderHelper.getStorageDomainId(this, providerId);
        IdQueryParameters parameters = new IdQueryParameters(storageDomainId);
        List<CinderVolumeType> volumeTypes = getBackendCollection(
                CinderVolumeType.class, QueryType.GetCinderVolumeTypesByStorageDomainId, parameters);


        return volumeTypes.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .map(v -> addLinks(populate(map(v), v)))
                .orElseGet(this::notFound);
    }

    @Override
    protected OpenStackVolumeType addParents(OpenStackVolumeType volumeType) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        volumeType.setOpenstackVolumeProvider(provider);
        return super.addParents(volumeType);
    }
}
