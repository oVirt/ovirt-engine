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

import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.resource.NetworkFilterResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendNetworkFilterResource
    extends AbstractBackendSubResource<NetworkFilter, org.ovirt.engine.core.common.businessentities.network.NetworkFilter>
    implements NetworkFilterResource {

    public BackendNetworkFilterResource(String id) {
        super(id, NetworkFilter.class, org.ovirt.engine.core.common.businessentities.network.NetworkFilter.class);
    }

    @Override
    public NetworkFilter get() {
        return performGet(VdcQueryType.GetNetworkFilterById, new IdQueryParameters(guid));
    }
}
