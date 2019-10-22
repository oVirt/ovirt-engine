/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VcpuPin;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VCpuPin;

public class V3VCpuPinInAdapter implements V3Adapter<V3VCpuPin, VcpuPin> {
    @Override
    public VcpuPin adapt(V3VCpuPin from) {
        VcpuPin to = new VcpuPin();
        if (from.isSetCpuSet()) {
            to.setCpuSet(from.getCpuSet());
        }
        if (from.isSetVcpu()) {
            to.setVcpu(from.getVcpu());
        }
        return to;
    }
}
