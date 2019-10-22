/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.MemoryOverCommit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MemoryOverCommit;

public class V3MemoryOverCommitInAdapter implements V3Adapter<V3MemoryOverCommit, MemoryOverCommit> {
    @Override
    public MemoryOverCommit adapt(V3MemoryOverCommit from) {
        MemoryOverCommit to = new MemoryOverCommit();
        to.setPercent(from.getPercent());
        return to;
    }
}
