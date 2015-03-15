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
