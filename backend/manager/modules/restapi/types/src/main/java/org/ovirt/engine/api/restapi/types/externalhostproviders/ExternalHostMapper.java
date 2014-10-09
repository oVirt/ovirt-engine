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

import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.core.common.businessentities.VDS;

public class ExternalHostMapper {
    @Mapping(from = VDS.class, to = ExternalHost.class)
    public static ExternalHost map(VDS entity, ExternalHost template) {
        ExternalHost model = template != null? template: new ExternalHost();
        String name = entity.getName();
        if (name != null) {
            model.setId(string2hex(name));
            model.setName(name);
        }
        if (entity.getHostName() != null) {
            model.setAddress(entity.getHostName());
        }
        return model;
    }

    @Mapping(from = ExternalHost.class, to = VDS.class)
    public static VDS map(ExternalHost model, VDS template) {
        VDS entity = template != null? template: new VDS();
        if (model.isSetId()) {
            entity.setVdsName(hex2string(model.getId()));
        }
        else if (model.isSetName()) {
            entity.setVdsName(model.getName());
        }
        if (model.isSetAddress()) {
            entity.setHostName(model.getAddress());
        }
        return entity;
    }
}
