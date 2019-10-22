/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ksm;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3KSM;

public class V3KSMInAdapter implements V3Adapter<V3KSM, Ksm> {
    @Override
    public Ksm adapt(V3KSM from) {
        Ksm to = new Ksm();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetMergeAcrossNodes()) {
            to.setMergeAcrossNodes(from.isMergeAcrossNodes());
        }
        return to;
    }
}
