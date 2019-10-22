/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
