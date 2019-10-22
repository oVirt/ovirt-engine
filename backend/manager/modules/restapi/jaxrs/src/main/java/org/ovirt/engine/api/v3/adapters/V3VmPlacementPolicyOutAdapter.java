/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Host;
import org.ovirt.engine.api.v3.types.V3Hosts;
import org.ovirt.engine.api.v3.types.V3VmPlacementPolicy;

public class V3VmPlacementPolicyOutAdapter implements V3Adapter<VmPlacementPolicy, V3VmPlacementPolicy> {
    @Override
    public V3VmPlacementPolicy adapt(VmPlacementPolicy from) {
        V3VmPlacementPolicy to = new V3VmPlacementPolicy();
        if (from.isSetAffinity()) {
            to.setAffinity(from.getAffinity().value());
        }
        if (from.isSetHosts()) {
            to.setHosts(new V3Hosts());
            to.getHosts().getHosts().addAll(adaptOut(from.getHosts().getHosts()));

            // V3 allowed specifying only one host, using the "host" element instead of "hosts":
            List<Host> hosts = from.getHosts().getHosts();
            if (hosts.size() == 1) {
                V3Host host = adaptOut(hosts.get(0));
                to.setHost(host);
            }
        }
        return to;
    }
}
