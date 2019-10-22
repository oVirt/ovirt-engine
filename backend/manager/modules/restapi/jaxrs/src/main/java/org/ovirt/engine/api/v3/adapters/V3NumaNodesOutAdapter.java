/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NumaNodes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNodes;

public class V3NumaNodesOutAdapter implements V3Adapter<NumaNodes, V3NumaNodes> {
    @Override
    public V3NumaNodes adapt(NumaNodes from) {
        V3NumaNodes to = new V3NumaNodes();
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
        to.getNumaNodes().addAll(adaptOut(from.getNumaNodes()));
        return to;
    }
}
