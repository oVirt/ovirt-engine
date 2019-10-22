/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ksm;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3KSM;

public class V3KsmOutAdapter implements V3Adapter<Ksm, V3KSM> {
    @Override
    public V3KSM adapt(Ksm from) {
        V3KSM to = new V3KSM();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetMergeAcrossNodes()) {
            to.setMergeAcrossNodes(from.isMergeAcrossNodes());
        }
        return to;
    }
}
