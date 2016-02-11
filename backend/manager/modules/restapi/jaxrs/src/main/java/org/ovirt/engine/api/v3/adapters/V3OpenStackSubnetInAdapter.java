/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
