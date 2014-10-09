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

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.restapi.types.Mapping;

public class ExternalComputeResourceMapper {
    @Mapping(from = org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class,
            to = ExternalComputeResource.class)
    public static ExternalComputeResource map(
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource entity,
            ExternalComputeResource template) {
        ExternalComputeResource model = template != null ? template : new ExternalComputeResource();
        String name = entity.getName();
        if (name != null) {
            model.setId(string2hex(name));
            model.setName(name);
        }
        if (entity.getProvider() != null) {
            model.setProvider(entity.getProvider());
        }
        if (entity.getUser() != null) {
            model.setUser(entity.getUser());
        }
        if (entity.getUrl() != null) {
            model.setUrl(entity.getUrl());
        }
        return model;
    }

    @Mapping(from = ExternalComputeResource.class,
            to = org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class)
    public static org.ovirt.engine.core.common.businessentities.ExternalComputeResource map(
            ExternalComputeResource model,
            org.ovirt.engine.core.common.businessentities.ExternalComputeResource template) {
        org.ovirt.engine.core.common.businessentities.ExternalComputeResource entity =
                template != null? template: new org.ovirt.engine.core.common.businessentities.ExternalComputeResource();
        if (model.isSetId()) {
            entity.setName(hex2string(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetProvider()) {
            entity.setProvider(model.getProvider());
        }
        if (model.isSetUser()) {
            entity.setUser(model.getUser());
        }
        if (model.isSetUrl()) {
            entity.setUrl(model.getUrl());
        }
        return entity;
    }
}
