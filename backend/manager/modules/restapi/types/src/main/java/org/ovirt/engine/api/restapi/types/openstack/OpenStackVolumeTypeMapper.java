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

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;

public class OpenStackVolumeTypeMapper {
    @Mapping(from = CinderVolumeType.class, to = OpenStackVolumeType.class)
    public static OpenStackVolumeType map(CinderVolumeType entity, OpenStackVolumeType template) {
        OpenStackVolumeType model = template != null ? template: new OpenStackVolumeType();
        if (entity.getId() != null) {
            model.setId(entity.getId());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getExtraSpecs() != null) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getExtraSpecs()));
        }
        return model;
    }

    @Mapping(from = OpenStackVolumeType.class, to = CinderVolumeType.class)
    public static CinderVolumeType map(OpenStackVolumeType model, CinderVolumeType template) {
        CinderVolumeType entity = template != null ? template: new CinderVolumeType();
        if (model.isSetId()) {
            entity.setId(model.getId());
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetProperties()) {
            entity.setExtraSpecs(CustomPropertiesParser.toMap(model.getProperties()));
        }
        return entity;
    }
}
