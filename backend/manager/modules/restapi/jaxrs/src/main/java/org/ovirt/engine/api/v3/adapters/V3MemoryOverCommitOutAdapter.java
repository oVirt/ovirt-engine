/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.MemoryOverCommit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MemoryOverCommit;

public class V3MemoryOverCommitOutAdapter implements V3Adapter<MemoryOverCommit, V3MemoryOverCommit> {
    @Override
    public V3MemoryOverCommit adapt(MemoryOverCommit from) {
        V3MemoryOverCommit to = new V3MemoryOverCommit();
        if (from.isSetPercent()) {
            to.setPercent(from.getPercent());
        }
        return to;
    }
}
