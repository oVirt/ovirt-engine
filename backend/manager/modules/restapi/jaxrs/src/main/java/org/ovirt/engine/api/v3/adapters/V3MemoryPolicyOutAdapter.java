/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MemoryPolicy;

public class V3MemoryPolicyOutAdapter implements V3Adapter<MemoryPolicy, V3MemoryPolicy> {
    @Override
    public V3MemoryPolicy adapt(MemoryPolicy from) {
        V3MemoryPolicy to = new V3MemoryPolicy();
        if (from.isSetBallooning()) {
            to.setBallooning(from.isBallooning());
        }
        if (from.isSetGuaranteed()) {
            to.setGuaranteed(from.getGuaranteed());
        }
        if (from.isSetOverCommit()) {
            to.setOverCommit(adaptOut(from.getOverCommit()));
        }
        if (from.isSetTransparentHugepages()) {
            to.setTransparentHugepages(adaptOut(from.getTransparentHugepages()));
        }
        return to;
    }
}
