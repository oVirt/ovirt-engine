/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmPlacementPolicy;

public class V3VmPlacementPolicyInAdapter implements V3Adapter<V3VmPlacementPolicy, VmPlacementPolicy> {
    @Override
    public VmPlacementPolicy adapt(V3VmPlacementPolicy from) {
        VmPlacementPolicy to = new VmPlacementPolicy();
        if (from.isSetAffinity()) {
            to.setAffinity(VmAffinity.fromValue(from.getAffinity()));
        }
        if (from.isSetHosts()) {
            to.setHosts(new Hosts());
            to.getHosts().getHosts().addAll(adaptIn(from.getHosts().getHosts()));
        }

        // V3 allowed specifying only one host, using "host" instead of "hosts":
        if (from.isSetHost() && !from.isSetHosts()) {
            Hosts hosts = new Hosts();
            Host host = adaptIn(from.getHost());
            hosts.getHosts().add(host);
            to.setHosts(hosts);
        }

        return to;
    }
}
