/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.QuotaStorageLimits;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimits;

public class V3QuotaStorageLimitsOutAdapter implements V3Adapter<QuotaStorageLimits, V3QuotaStorageLimits> {
    @Override
    public V3QuotaStorageLimits adapt(QuotaStorageLimits from) {
        V3QuotaStorageLimits to = new V3QuotaStorageLimits();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getQuotaStorageLimits().addAll(adaptOut(from.getQuotaStorageLimits()));
        return to;
    }
}
