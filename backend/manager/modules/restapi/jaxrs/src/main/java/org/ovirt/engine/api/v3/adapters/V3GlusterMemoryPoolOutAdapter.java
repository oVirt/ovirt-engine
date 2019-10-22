/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.GlusterMemoryPool;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterMemoryPool;

public class V3GlusterMemoryPoolOutAdapter implements V3Adapter<GlusterMemoryPool, V3GlusterMemoryPool> {
    @Override
    public V3GlusterMemoryPool adapt(GlusterMemoryPool from) {
        V3GlusterMemoryPool to = new V3GlusterMemoryPool();
        if (from.isSetAllocCount()) {
            to.setAllocCount(from.getAllocCount());
        }
        if (from.isSetColdCount()) {
            to.setColdCount(from.getColdCount());
        }
        if (from.isSetHotCount()) {
            to.setHotCount(from.getHotCount());
        }
        if (from.isSetMaxAlloc()) {
            to.setMaxAlloc(from.getMaxAlloc());
        }
        if (from.isSetMaxStdalloc()) {
            to.setMaxStdalloc(from.getMaxStdalloc());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPaddedSize()) {
            to.setPaddedSize(from.getPaddedSize());
        }
        if (from.isSetPoolMisses()) {
            to.setPoolMisses(from.getPoolMisses());
        }
        return to;
    }
}
