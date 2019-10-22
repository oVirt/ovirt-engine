/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/
package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.compat.Guid;

public class ClusterFeaturesMapper {

    @Mapping(from = org.ovirt.engine.api.model.ClusterFeature.class, to = AdditionalFeature.class)
    public static AdditionalFeature map(org.ovirt.engine.api.model.ClusterFeature model, AdditionalFeature template) {
        AdditionalFeature entity = template != null ? template : new AdditionalFeature();
        entity.setName(model.getName());
        entity.setId(Guid.createGuidFromString(model.getId()));
        return entity;
    }

    @Mapping(from = AdditionalFeature.class, to = org.ovirt.engine.api.model.ClusterFeature.class)
    public static org.ovirt.engine.api.model.ClusterFeature map(AdditionalFeature entity,
            org.ovirt.engine.api.model.ClusterFeature template) {
        org.ovirt.engine.api.model.ClusterFeature model =
                template != null ? template : new org.ovirt.engine.api.model.ClusterFeature();
        model.setName(entity.getName());
        model.setId(entity.getId().toString());
        return model;
    }

}
