/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.resource.NetworkFilterResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendNetworkFilterResource
    extends AbstractBackendSubResource<NetworkFilter, org.ovirt.engine.core.common.businessentities.network.NetworkFilter>
    implements NetworkFilterResource {

    public BackendNetworkFilterResource(String id) {
        super(id, NetworkFilter.class, org.ovirt.engine.core.common.businessentities.network.NetworkFilter.class);
    }

    @Override
    public NetworkFilter get() {
        return performGet(QueryType.GetNetworkFilterById, new IdQueryParameters(guid));
    }
}
