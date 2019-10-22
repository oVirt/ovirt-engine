/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.QuotaClusterLimits;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaClusterLimits;

public class V3QuotaClusterLimitsInAdapter implements V3Adapter<V3QuotaClusterLimits, QuotaClusterLimits> {
    @Override
    public QuotaClusterLimits adapt(V3QuotaClusterLimits from) {
        QuotaClusterLimits to = new QuotaClusterLimits();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getQuotaClusterLimits().addAll(adaptIn(from.getQuotaClusterLimits()));
        return to;
    }
}
