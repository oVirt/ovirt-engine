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
