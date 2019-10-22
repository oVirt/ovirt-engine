/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimit;

public class V3QuotaStorageLimitOutAdapter implements V3Adapter<QuotaStorageLimit, V3QuotaStorageLimit> {
    @Override
    public V3QuotaStorageLimit adapt(QuotaStorageLimit from) {
        V3QuotaStorageLimit to = new V3QuotaStorageLimit();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setQuota(adaptOut(from.getQuota()));
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetUsage()) {
            to.setUsage(from.getUsage());
        }
        return to;
    }
}
