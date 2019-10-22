/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.restapi.types.IpVersionMapper;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;

public class OpenStackSubnetMapper {
    @Mapping(from = ExternalSubnet.class, to = OpenStackSubnet.class)
    public static OpenStackSubnet map(ExternalSubnet entity, OpenStackSubnet template) {
        OpenStackSubnet model = template != null? template: new OpenStackSubnet();
        if (entity.getId() != null) {
            model.setId(entity.getId());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getCidr() != null) {
            model.setCidr(entity.getCidr());
        }
        if (entity.getIpVersion() != null) {
            model.setIpVersion(IpVersionMapper.map(entity.getIpVersion()).value());
        }
        if (entity.getGateway() != null) {
            model.setGateway(entity.getGateway());
        }
        List<String> entityDnsServers = entity.getDnsServers();
        if (entityDnsServers != null && !entityDnsServers.isEmpty()) {
            OpenStackSubnet.DnsServersList modelDnsServers = new OpenStackSubnet.DnsServersList();
            for (String entityDnsServer : entityDnsServers) {
                modelDnsServers.getDnsServers().add(entityDnsServer);
            }
            model.setDnsServers(modelDnsServers);
        }
        return model;
    }

    @Mapping(from = OpenStackSubnet.class, to = ExternalSubnet.class)
    public static ExternalSubnet map(OpenStackSubnet model, ExternalSubnet template) {
        ExternalSubnet entity = template != null? template: new ExternalSubnet();
        if (model.isSetId()) {
            entity.setId(model.getId());
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetCidr()) {
            entity.setCidr(model.getCidr());
        }
        if (model.isSetIpVersion()) {
            entity.setIpVersion(IpVersionMapper.map(IpVersion.fromValue(model.getIpVersion())));
        }
        if (model.isSetGateway()) {
            entity.setGateway(model.getGateway());
        }
        List<String> entityDnsServers = new ArrayList<>(1);
        if (model.isSetDnsServers()) {
            List<String> modelDnsServers = model.getDnsServers().getDnsServers();
            entityDnsServers.addAll(modelDnsServers);
        }
        entity.setDnsServers(entityDnsServers);
        return entity;
    }
}
