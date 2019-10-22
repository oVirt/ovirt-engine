/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalDiscoveredHost;

public class V3ExternalDiscoveredHostInAdapter implements V3Adapter<V3ExternalDiscoveredHost, ExternalDiscoveredHost> {
    @Override
    public ExternalDiscoveredHost adapt(V3ExternalDiscoveredHost from) {
        ExternalDiscoveredHost to = new ExternalDiscoveredHost();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptIn(from.getExternalHostProvider()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIp()) {
            to.setIp(from.getIp());
        }
        if (from.isSetLastReport()) {
            to.setLastReport(from.getLastReport());
        }
        if (from.isSetMac()) {
            to.setMac(from.getMac());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetSubnetName()) {
            to.setSubnetName(from.getSubnetName());
        }
        return to;
    }
}
