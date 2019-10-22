/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;

public class OpenStackImageMapper {
    @Mapping(from = RepoImage.class, to = OpenStackImage.class)
    public static OpenStackImage map(RepoImage entity, OpenStackImage template) {
        OpenStackImage model = template != null? template: new OpenStackImage();
        if (entity.getRepoImageId() != null) {
            model.setId(entity.getRepoImageId());
        }
        if (entity.getRepoImageName() != null) {
            model.setName(entity.getRepoImageName());
        }
        return model;
    }

    @Mapping(from = OpenStackImage.class, to = RepoImage.class)
    public static RepoImage map(OpenStackImage model, RepoImage template) {
        RepoImage entity = template != null? template: new RepoImage();
        if (model.isSetId()) {
            entity.setRepoImageId(model.getId());
        }
        if (model.isSetName()) {
            entity.setRepoImageName(model.getName());
        }
        return entity;
    }
}
