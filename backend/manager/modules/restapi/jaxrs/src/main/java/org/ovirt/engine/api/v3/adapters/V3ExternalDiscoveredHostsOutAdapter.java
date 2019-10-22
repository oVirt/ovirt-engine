/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalDiscoveredHosts;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalDiscoveredHosts;

public class V3ExternalDiscoveredHostsOutAdapter implements V3Adapter<ExternalDiscoveredHosts, V3ExternalDiscoveredHosts> {
    @Override
    public V3ExternalDiscoveredHosts adapt(ExternalDiscoveredHosts from) {
        V3ExternalDiscoveredHosts to = new V3ExternalDiscoveredHosts();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getExternalDiscoveredHosts().addAll(adaptOut(from.getExternalDiscoveredHosts()));
        return to;
    }
}
