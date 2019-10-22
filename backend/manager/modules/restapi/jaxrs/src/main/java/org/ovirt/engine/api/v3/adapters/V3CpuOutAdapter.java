/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CPU;
import org.ovirt.engine.api.v3.types.V3Cores;

public class V3CpuOutAdapter implements V3Adapter<Cpu, V3CPU> {
    @Override
    public V3CPU adapt(Cpu from) {
        V3CPU to = new V3CPU();
        if (from.isSetArchitecture()) {
            to.setArchitecture(from.getArchitecture().value());
        }
        if (from.isSetCores()) {
            to.setCores(new V3Cores());
            to.getCores().getCore().addAll(adaptOut(from.getCores().getCores()));
        }
        if (from.isSetCpuTune()) {
            to.setCpuTune(adaptOut(from.getCpuTune()));
        }
        if (from.isSetLevel()) {
            to.setLevel(from.getLevel());
        }
        if (from.isSetMode()) {
            to.setMode(from.getMode().value());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetSpeed()) {
            to.setSpeed(from.getSpeed());
        }
        if (from.isSetTopology()) {
            to.setTopology(adaptOut(from.getTopology()));
        }
        if (from.isSetType()) {
            to.setId(from.getType());
        }
        return to;
    }
}
