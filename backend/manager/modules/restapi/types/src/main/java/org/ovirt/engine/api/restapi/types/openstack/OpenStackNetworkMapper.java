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

package org.ovirt.engine.api.restapi.types.openstack;

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;

public class OpenStackNetworkMapper {
    @Mapping(from = Network.class, to = OpenStackNetwork.class)
    public static OpenStackNetwork map(Network entity, OpenStackNetwork template) {
        OpenStackNetwork model = template != null? template: new OpenStackNetwork();
        ProviderNetwork providedBy = entity.getProvidedBy();
        if (providedBy != null) {
            if (providedBy.getExternalId() != null) {
                model.setId(providedBy.getExternalId());
            }
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        return model;
    }

    @Mapping(from = OpenStackNetwork.class, to = Network.class)
    public static Network map(OpenStackNetwork model, Network template) {
        Network entity = template != null? template: new Network();
        if (model.isSetId()) {
            ProviderNetwork providedBy = new ProviderNetwork();
            providedBy.setExternalId(model.getId());
            entity.setProvidedBy(providedBy);
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        return entity;
    }
}
