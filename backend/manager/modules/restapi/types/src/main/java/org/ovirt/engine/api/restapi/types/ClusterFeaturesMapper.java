/*
Copyright (c) 2017 Red Hat, Inc.

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
