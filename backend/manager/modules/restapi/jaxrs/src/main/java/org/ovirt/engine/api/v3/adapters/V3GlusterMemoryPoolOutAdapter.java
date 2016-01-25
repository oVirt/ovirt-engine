/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
