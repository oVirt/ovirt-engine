/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.QuotaStorageLimits;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimits;

public class V3QuotaStorageLimitsInAdapter implements V3Adapter<V3QuotaStorageLimits, QuotaStorageLimits> {
    @Override
    public QuotaStorageLimits adapt(V3QuotaStorageLimits from) {
        QuotaStorageLimits to = new QuotaStorageLimits();
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
        to.getQuotaStorageLimits().addAll(adaptIn(from.getQuotaStorageLimits()));
        return to;
    }
}
