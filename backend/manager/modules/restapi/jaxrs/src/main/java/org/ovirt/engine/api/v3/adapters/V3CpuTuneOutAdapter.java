/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CpuTune;

public class V3CpuTuneOutAdapter implements V3Adapter<CpuTune, V3CpuTune> {
    @Override
    public V3CpuTune adapt(CpuTune from) {
        V3CpuTune to = new V3CpuTune();
        if (from.isSetVcpuPins()) {
            to.getVCpuPin().addAll(adaptOut(from.getVcpuPins().getVcpuPins()));
        }
        return to;
    }
}
