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
