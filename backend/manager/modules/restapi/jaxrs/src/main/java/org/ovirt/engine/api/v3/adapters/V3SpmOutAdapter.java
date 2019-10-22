/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Spm;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SPM;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3SpmOutAdapter implements V3Adapter<Spm, V3SPM> {
    @Override
    public V3SPM adapt(Spm from) {
        V3SPM to = new V3SPM();
        if (from.isSetPriority()) {
            to.setPriority(from.getPriority());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        return to;
    }
}
