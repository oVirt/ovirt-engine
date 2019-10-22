/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
