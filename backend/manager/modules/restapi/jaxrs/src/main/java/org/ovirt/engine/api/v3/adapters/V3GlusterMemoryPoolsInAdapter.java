/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterMemoryPools;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterMemoryPools;

public class V3GlusterMemoryPoolsInAdapter implements V3Adapter<V3GlusterMemoryPools, GlusterMemoryPools> {
    @Override
    public GlusterMemoryPools adapt(V3GlusterMemoryPools from) {
        GlusterMemoryPools to = new GlusterMemoryPools();
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
        to.getGlusterMemoryPools().addAll(adaptIn(from.getGlusterMemoryPools()));
        return to;
    }
}
