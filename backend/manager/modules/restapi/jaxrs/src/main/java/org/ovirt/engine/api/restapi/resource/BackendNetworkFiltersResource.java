/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.model.NetworkFilters;
import org.ovirt.engine.api.resource.NetworkFilterResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendNetworkFiltersResource
    extends AbstractBackendCollectionResource<NetworkFilter, org.ovirt.engine.core.common.businessentities.network.NetworkFilter>
    implements NetworkFiltersResource {

    public BackendNetworkFiltersResource() {
        super(NetworkFilter.class, org.ovirt.engine.core.common.businessentities.network.NetworkFilter.class);
    }

    @Override
    public NetworkFilterResource getNetworkFilterResource(String id) {
        return inject(new BackendNetworkFilterResource(id));
    }

    @Override
    public NetworkFilters list() {
        return mapCollection(getBackendCollection(QueryType.GetAllNetworkFilters,
                new QueryParametersBase()));
    }

    private NetworkFilters mapCollection(
            List<org.ovirt.engine.core.common.businessentities.network.NetworkFilter> entities) {
        NetworkFilters collection = new NetworkFilters();
        for (org.ovirt.engine.core.common.businessentities.network.NetworkFilter entity : entities) {
            NetworkFilter networkFilter = map(entity);
            collection.getNetworkFilters().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }
}
