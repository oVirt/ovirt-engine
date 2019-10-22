/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.NetworkFilter;

public class NetworkFilterMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.NetworkFilter.class, to = NetworkFilter.class)
    public static NetworkFilter map(org.ovirt.engine.core.common.businessentities.network.NetworkFilter entity,
            NetworkFilter template) {
        NetworkFilter networkFilter = (template != null) ? template : new NetworkFilter();
        networkFilter.setVersion(VersionMapper.map(entity.getVersion()));
        networkFilter.setName(entity.getName());
        networkFilter.setId(entity.getId().toString());
        return networkFilter;
    }
}
