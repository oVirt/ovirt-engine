/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ExternalDiscoveredHosts;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalDiscoveredHosts;

public class V3ExternalDiscoveredHostsInAdapter implements V3Adapter<V3ExternalDiscoveredHosts, ExternalDiscoveredHosts> {
    @Override
    public ExternalDiscoveredHosts adapt(V3ExternalDiscoveredHosts from) {
        ExternalDiscoveredHosts to = new ExternalDiscoveredHosts();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getExternalDiscoveredHosts().addAll(adaptIn(from.getExternalDiscoveredHosts()));
        return to;
    }
}
