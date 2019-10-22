/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
