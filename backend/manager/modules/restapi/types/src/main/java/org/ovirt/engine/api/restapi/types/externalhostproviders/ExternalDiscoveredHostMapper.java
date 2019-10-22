/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.restapi.types.Mapping;

public class ExternalDiscoveredHostMapper {
    @Mapping(from = org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class,
            to = ExternalDiscoveredHost.class)
    public static ExternalDiscoveredHost map(
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost entity,
            ExternalDiscoveredHost template) {
        ExternalDiscoveredHost model = template != null? template: new ExternalDiscoveredHost();
        String name = entity.getName();
        if (name != null) {
            model.setId(string2hex(name));
            model.setName(name);
        }
        if (entity.getMac() != null) {
            model.setMac(entity.getMac());
        }
        if (entity.getIp() != null) {
            model.setIp(entity.getIp());
        }
        if (entity.getSubnetName() != null) {
            model.setSubnetName(entity.getSubnetName());
        }
        if (entity.getLastReport() != null) {
            model.setLastReport(entity.getLastReport());
        }
        return model;
    }

    @Mapping(from = ExternalDiscoveredHost.class,
        to = org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class)
    public static org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost map(
            ExternalDiscoveredHost model,
            org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost template) {
        org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost entity =
                template != null? template: new org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost();
        if (model.isSetId()) {
            entity.setName(hex2string(model.getId()));
        } else if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetMac()) {
            entity.setMac(model.getMac());
        }
        if (model.isSetIp()) {
            entity.setIp(model.getIp());
        }
        if (model.isSetSubnetName()) {
            entity.setSubnetName(model.getSubnetName());
        }
        if (model.isSetLastReport()) {
            entity.setLastReport(model.getLastReport());
        }
        return entity;
    }
}
