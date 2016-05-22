/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.model.NetworkFilters;
import org.ovirt.engine.api.resource.NetworkFilterResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
        return mapCollection(getBackendCollection(VdcQueryType.GetAllNetworkFilters,
                new VdcQueryParametersBase()));
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
