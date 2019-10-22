/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackSubnet;

public class V3OpenStackSubnetInAdapter implements V3Adapter<V3OpenStackSubnet, OpenStackSubnet> {
    @Override
    public OpenStackSubnet adapt(V3OpenStackSubnet from) {
        OpenStackSubnet to = new OpenStackSubnet();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetCidr()) {
            to.setCidr(from.getCidr());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDnsServers()) {
            to.setDnsServers(new OpenStackSubnet.DnsServersList());
            to.getDnsServers().getDnsServers().addAll(from.getDnsServers().getDnsServers());
        }
        if (from.isSetGateway()) {
            to.setGateway(from.getGateway());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIpVersion()) {
            to.setIpVersion(from.getIpVersion());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOpenstackNetwork()) {
            to.setOpenstackNetwork(adaptIn(from.getOpenstackNetwork()));
        }
        return to;
    }
}
