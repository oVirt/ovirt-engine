/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.model.OpenStackVolumeTypes;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackVolumeTypesResource
        extends AbstractBackendCollectionResource<OpenStackVolumeType, CinderVolumeType>
        implements OpenstackVolumeTypesResource {
    private String providerId;

    public BackendOpenStackVolumeTypesResource(String providerId) {
        super(OpenStackVolumeType.class, CinderVolumeType.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackVolumeTypes list() {
        Guid storageDomainId = BackendOpenStackStorageProviderHelper.getStorageDomainId(this, providerId);
        IdQueryParameters parameters = new IdQueryParameters(storageDomainId);
        return mapCollection(getBackendCollection(QueryType.GetCinderVolumeTypesByStorageDomainId, parameters));

    }

    protected OpenStackVolumeTypes mapCollection(List<CinderVolumeType> entities) {
        OpenStackVolumeTypes collection = new OpenStackVolumeTypes();
        for (CinderVolumeType volumeType : entities) {
            collection.getOpenStackVolumeTypes().add(addLinks(populate(map(volumeType), volumeType)));
        }
        return collection;
    }

    @Override
    protected OpenStackVolumeType addParents(OpenStackVolumeType volumeType) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        volumeType.setOpenstackVolumeProvider(provider);
        return super.addParents(volumeType);
    }

    @Override
    public OpenstackVolumeTypeResource getTypeResource(String id) {
        return inject(new BackendOpenStackVolumeTypeResource(providerId, id));
    }
}
