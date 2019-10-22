/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NumaNodes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNodes;

public class V3NumaNodesInAdapter implements V3Adapter<V3NumaNodes, NumaNodes> {
    @Override
    public NumaNodes adapt(V3NumaNodes from) {
        NumaNodes to = new NumaNodes();
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
        to.getNumaNodes().addAll(adaptIn(from.getNumaNodes()));
        return to;
    }
}
