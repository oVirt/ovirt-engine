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

import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.VcpuPins;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CpuTune;

public class V3CpuTuneInAdapter implements V3Adapter<V3CpuTune, CpuTune> {
    @Override
    public CpuTune adapt(V3CpuTune from) {
        CpuTune to = new CpuTune();
        if (from.isSetVCpuPin()) {
            to.setVcpuPins(new VcpuPins());
            to.getVcpuPins().getVcpuPins().addAll(adaptIn(from.getVCpuPin()));
        }
        return to;
    }
}
