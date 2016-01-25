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
