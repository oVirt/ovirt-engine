/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.VirtualNumaNodes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VirtualNumaNodes;

public class V3VirtualNumaNodesOutAdapter implements V3Adapter<VirtualNumaNodes, V3VirtualNumaNodes> {
    @Override
    public V3VirtualNumaNodes adapt(VirtualNumaNodes from) {
        V3VirtualNumaNodes to = new V3VirtualNumaNodes();
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
        to.getVirtualNumaNodes().addAll(adaptOut(from.getVirtualNumaNodes()));
        return to;
    }
}
