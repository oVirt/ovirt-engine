/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
        } else if (model.isSetName()) {
            entity.setVdsName(model.getName());
        }
        if (model.isSetAddress()) {
            entity.setHostName(model.getAddress());
        }
        return entity;
    }
}
