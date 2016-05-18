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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.Cores;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.CpuMode;
import org.ovirt.engine.api.utils.InvalidEnumValueException;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CPU;

public class V3CPUInAdapter implements V3Adapter<V3CPU, Cpu> {
    @Override
    public Cpu adapt(V3CPU from) {
        Cpu to = new Cpu();
        if (from.isSetArchitecture()) {
            to.setArchitecture(Architecture.fromValue(from.getArchitecture()));
        }
        if (from.isSetCores()) {
            to.setCores(new Cores());
            to.getCores().getCores().addAll(adaptIn(from.getCores().getCore()));
        }
        if (from.isSetCpuTune()) {
            to.setCpuTune(adaptIn(from.getCpuTune()));
        }
        if (from.isSetLevel()) {
            to.setLevel(from.getLevel());
        }
        if (from.isSetMode()) {
            try {
                to.setMode(CpuMode.fromValue(from.getMode()));
            }
            catch (InvalidEnumValueException exception) {
                // In version 3 of the API invalid values were accepted, and they meant "disable passthrough". We need
                // to preserve that, but we also need to pass to version 4 a valid value, as otherwise it won't do any
                // update to the attribute. As both "custom" and "host_model" mean exactly the same we can use any of
                // them.
                to.setMode(CpuMode.CUSTOM);
            }
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetSpeed()) {
            to.setSpeed(from.getSpeed());
        }
        if (from.isSetTopology()) {
            to.setTopology(adaptIn(from.getTopology()));
        }
        if (from.isSetId()) {
            to.setType(from.getId());
        }
        return to;
    }
}
