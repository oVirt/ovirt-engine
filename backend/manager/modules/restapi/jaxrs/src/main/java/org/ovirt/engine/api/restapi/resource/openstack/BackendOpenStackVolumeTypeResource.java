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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.resource.openstack.OpenStackVolumeTypeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public class BackendOpenStackVolumeTypeResource
        extends AbstractBackendActionableResource<OpenStackVolumeType, CinderVolumeType>
        implements OpenStackVolumeTypeResource {
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
                CinderVolumeType.class, VdcQueryType.GetCinderVolumeTypesByStorageDomainId, parameters);
        CollectionUtils.filter(volumeTypes, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((CinderVolumeType) o).getId().equals(id);
            }
        });

        if (volumeTypes.isEmpty()) {
            return notFound();
        }

        return addLinks(populate(map(volumeTypes.get(0)), volumeTypes.get(0)));
    }

    @Override
    protected OpenStackVolumeType doPopulate(OpenStackVolumeType model, CinderVolumeType entity) {
        return model;
    }

    @Override
    protected OpenStackVolumeType addParents(OpenStackVolumeType volumeType) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        volumeType.setOpenstackVolumeProvider(provider);
        return super.addParents(volumeType);
    }
}
