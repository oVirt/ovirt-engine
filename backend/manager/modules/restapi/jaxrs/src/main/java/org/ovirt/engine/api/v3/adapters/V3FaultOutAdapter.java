/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Fault;

public class V3FaultOutAdapter implements V3Adapter<Fault, V3Fault> {
    @Override
    public V3Fault adapt(Fault from) {
        V3Fault to = new V3Fault();
        if (from.isSetReason()) {
            to.setReason(from.getReason());
        }
        if (from.isSetDetail()) {
            to.setDetail(from.getDetail());
        }
        return to;
    }
}
