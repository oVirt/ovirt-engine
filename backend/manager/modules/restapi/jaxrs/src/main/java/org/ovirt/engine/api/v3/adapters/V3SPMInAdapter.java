/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Spm;
import org.ovirt.engine.api.model.SpmStatus;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SPM;

public class V3SPMInAdapter implements V3Adapter<V3SPM, Spm> {
    @Override
    public Spm adapt(V3SPM from) {
        Spm to = new Spm();
        if (from.isSetPriority()) {
            to.setPriority(from.getPriority());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(SpmStatus.fromValue(from.getStatus().getState()));
        }
        return to;
    }
}
