/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MemoryPolicy;

public class V3MemoryPolicyInAdapter implements V3Adapter<V3MemoryPolicy, MemoryPolicy> {
    @Override
    public MemoryPolicy adapt(V3MemoryPolicy from) {
        MemoryPolicy to = new MemoryPolicy();
        if (from.isSetBallooning()) {
            to.setBallooning(from.isBallooning());
        }
        if (from.isSetGuaranteed()) {
            to.setGuaranteed(from.getGuaranteed());
        }
        if (from.isSetOverCommit()) {
            to.setOverCommit(adaptIn(from.getOverCommit()));
        }
        if (from.isSetTransparentHugepages()) {
            to.setTransparentHugepages(adaptIn(from.getTransparentHugepages()));
        }
        return to;
    }
}
