/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CpuTopology;

public class V3CpuTopologyInAdapter implements V3Adapter<V3CpuTopology, CpuTopology> {
    @Override
    public CpuTopology adapt(V3CpuTopology from) {
        CpuTopology to = new CpuTopology();
        to.setCores(from.getCores());
        to.setSockets(from.getSockets());
        to.setThreads(from.getThreads());
        return to;
    }
}
