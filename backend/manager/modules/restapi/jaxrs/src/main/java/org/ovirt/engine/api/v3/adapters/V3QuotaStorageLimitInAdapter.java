/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimit;

public class V3QuotaStorageLimitInAdapter implements V3Adapter<V3QuotaStorageLimit, QuotaStorageLimit> {
    @Override
    public QuotaStorageLimit adapt(V3QuotaStorageLimit from) {
        QuotaStorageLimit to = new QuotaStorageLimit();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLimit()) {
            to.setLimit(from.getLimit());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptIn(from.getQuota()));
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
        }
        if (from.isSetUsage()) {
            to.setUsage(from.getUsage());
        }
        return to;
    }
}
