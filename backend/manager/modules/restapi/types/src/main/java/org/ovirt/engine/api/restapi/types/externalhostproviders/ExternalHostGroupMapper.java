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

import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.restapi.types.Mapping;

public class ExternalHostGroupMapper {
    @Mapping(from = org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class, to = ExternalHostGroup.class)
    public static ExternalHostGroup map(org.ovirt.engine.core.common.businessentities.ExternalHostGroup entity,
            ExternalHostGroup template) {
        ExternalHostGroup model = template != null? template: new ExternalHostGroup();
        String name = entity.getName();
        if (name != null) {
            model.setId(string2hex(name));
            model.setName(name);
        }
        if (entity.getArchitectureName() != null) {
            model.setArchitectureName(entity.getArchitectureName());
        }
        if (entity.getOperatingsystemName() != null) {
            model.setOperatingSystemName(entity.getOperatingsystemName());
        }
        if (entity.getDomainName() != null) {
            model.setDomainName(entity.getDomainName());
        }
        if (entity.getSubnetName() != null) {
            model.setSubnetName(entity.getSubnetName());
        }
        return model;
    }

    @Mapping(from = ExternalHostGroup.class, to = org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class)
    public static org.ovirt.engine.core.common.businessentities.ExternalHostGroup map(ExternalHostGroup model,
            org.ovirt.engine.core.common.businessentities.ExternalHostGroup template) {
        org.ovirt.engine.core.common.businessentities.ExternalHostGroup entity =
                template != null? template: new org.ovirt.engine.core.common.businessentities.ExternalHostGroup();
        if (model.isSetId()) {
            entity.setName(hex2string(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetArchitectureName()) {
            entity.setArchitectureName(model.getArchitectureName());
        }
        if (model.isSetOperatingSystemName()) {
            entity.setOperatingsystemName(model.getOperatingSystemName());
        }
        if (model.isSetDomainName()) {
            entity.setDomainName(model.getDomainName());
        }
        if (model.isSetSubnetName()) {
            entity.setSubnetName(model.getSubnetName());
        }
        return entity;
    }
}
