/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterBrickMemoryInfo;
import org.ovirt.engine.api.model.GlusterMemoryPools;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBrickMemoryInfo;

public class V3GlusterBrickMemoryInfoInAdapter implements V3Adapter<V3GlusterBrickMemoryInfo, GlusterBrickMemoryInfo> {
    @Override
    public GlusterBrickMemoryInfo adapt(V3GlusterBrickMemoryInfo from) {
        GlusterBrickMemoryInfo to = new GlusterBrickMemoryInfo();
        if (from.isSetMemoryPools()) {
            to.setMemoryPools(new GlusterMemoryPools());
            to.getMemoryPools().getGlusterMemoryPools().addAll(adaptIn(from.getMemoryPools().getGlusterMemoryPools()));
        }
        return to;
    }
}
