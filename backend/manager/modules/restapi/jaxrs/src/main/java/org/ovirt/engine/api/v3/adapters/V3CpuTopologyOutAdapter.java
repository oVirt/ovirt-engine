/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CpuTopology;

public class V3CpuTopologyOutAdapter implements V3Adapter<CpuTopology, V3CpuTopology> {
    @Override
    public V3CpuTopology adapt(CpuTopology from) {
        V3CpuTopology to = new V3CpuTopology();
        if (from.isSetCores()) {
            to.setCores(from.getCores());
        }
        if (from.isSetSockets()) {
            to.setSockets(from.getSockets());
        }
        if (from.isSetThreads()) {
            to.setThreads(from.getThreads());
        }
        return to;
    }
}
