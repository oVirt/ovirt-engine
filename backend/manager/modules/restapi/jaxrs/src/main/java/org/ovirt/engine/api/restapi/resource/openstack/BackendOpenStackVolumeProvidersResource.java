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

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeProviders;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.types.DataCenterMapper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackVolumeProvidersResource
        extends AbstractBackendCollectionResource<OpenStackVolumeProvider, Provider>
        implements OpenstackVolumeProvidersResource {

    public BackendOpenStackVolumeProvidersResource() {
        super(OpenStackVolumeProvider.class, Provider.class);
    }

    @Override
    public OpenStackVolumeProviders list() {
        return mapCollection(getBackendCollection());
    }

    private OpenStackVolumeProviders mapCollection(List<Provider> entities) {
        OpenStackVolumeProviders collection = new OpenStackVolumeProviders();
        for (Provider entity : entities) {
            OpenStackVolumeProvider provider = map(entity);
            collection.getOpenStackVolumeProviders().add(addLinks(populate(provider, entity)));
        }
        return collection;
    }

    private List<Provider> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(
                QueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.OPENSTACK_VOLUME)
            );
        } else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.OPENSTACK_VOLUME.name());
        String query = QueryHelper.getConstraint(httpHeaders, uriInfo, null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(String.format(" AND %1$s", query));
        }
        return buffer.toString();
    }

    @Override
    public Response add(OpenStackVolumeProvider provider) {
        validateParameters(provider, "name");
        if (provider.isSetDataCenter()) {
            StoragePool storagePool = getStoragePool(provider.getDataCenter());
            provider.setDataCenter(DataCenterMapper.map(storagePool, null));
        }
        return performCreate(
                ActionType.AddProvider,
                new ProviderParameters(map(provider)),
                new QueryIdResolver<Guid>(QueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    protected OpenStackVolumeProvider doPopulate(OpenStackVolumeProvider model, Provider entity) {
        StoragePool storagePool = getStoragePoolIdByStorageDomainName(entity.getName());
        if (storagePool != null) {
            model.setDataCenter(DataCenterMapper.map(storagePool, null));
        }
        return model;
    }

    @Override
    public OpenstackVolumeProviderResource getProviderResource(String id) {
        return inject(new BackendOpenStackVolumeProviderResource(id, this));
    }

    public StoragePool getStoragePool(DataCenter dataCenter) {
        StoragePool pool = null;
        if (dataCenter.isSetId()) {
            Guid id = asGuid(dataCenter.getId());
            pool = getEntity(StoragePool.class, QueryType.GetStoragePoolById,
                    new IdQueryParameters(id), "Datacenter: id=" + dataCenter.getId());
        } else if (dataCenter.isSetName()) {
            pool = getEntity(StoragePool.class, QueryType.GetStoragePoolByDatacenterName,
                    new NameQueryParameters(dataCenter.getName()), "Datacenter: name=" + dataCenter.getName());
        }
        if (pool == null) {
            notFound(DataCenter.class);
        }
        return pool;
    }

    private StoragePool getStoragePoolIdByStorageDomainName(String storageDomainName) {
        StorageDomainStatic storageDomain = getEntity(StorageDomainStatic.class, QueryType.GetStorageDomainByName,
                new NameQueryParameters(storageDomainName), "StorageDomain: name=" + storageDomainName);
        List<StoragePool> storagePools = getEntity(List.class, QueryType.GetStoragePoolsByStorageDomainId,
                new IdQueryParameters(storageDomain.getId()), "Datacenters");
        if (!storagePools.isEmpty()) {
            return storagePools.get(0);
        }
        // The storage domain is unattached
        return null;
    }
}
