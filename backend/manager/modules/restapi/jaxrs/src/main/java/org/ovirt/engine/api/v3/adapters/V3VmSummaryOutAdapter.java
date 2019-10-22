/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmSummary;

public class V3VmSummaryOutAdapter implements V3Adapter<VmSummary, V3VmSummary> {
    @Override
    public V3VmSummary adapt(VmSummary from) {
        V3VmSummary to = new V3VmSummary();
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetMigrating()) {
            to.setMigrating(from.getMigrating());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        return to;
    }
}
