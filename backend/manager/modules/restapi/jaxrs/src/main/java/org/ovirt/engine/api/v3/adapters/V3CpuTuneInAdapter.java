/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
