/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterBrickMemoryInfo;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBrickMemoryInfo;
import org.ovirt.engine.api.v3.types.V3GlusterMemoryPools;

public class V3GlusterBrickMemoryInfoOutAdapter implements V3Adapter<GlusterBrickMemoryInfo, V3GlusterBrickMemoryInfo> {
    @Override
    public V3GlusterBrickMemoryInfo adapt(GlusterBrickMemoryInfo from) {
        V3GlusterBrickMemoryInfo to = new V3GlusterBrickMemoryInfo();
        if (from.isSetMemoryPools()) {
            to.setMemoryPools(new V3GlusterMemoryPools());
            to.getMemoryPools().getGlusterMemoryPools().addAll(adaptOut(from.getMemoryPools().getGlusterMemoryPools()));
        }
        return to;
    }
}
