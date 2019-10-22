/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QuotaClusterLimit;

public class V3QuotaClusterLimitOutAdapter implements V3Adapter<QuotaClusterLimit, V3QuotaClusterLimit> {
    @Override
    public V3QuotaClusterLimit adapt(QuotaClusterLimit from) {
        V3QuotaClusterLimit to = new V3QuotaClusterLimit();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
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
        if (from.isSetMemoryLimit()) {
            to.setMemoryLimit(from.getMemoryLimit());
        }
        if (from.isSetMemoryUsage()) {
            to.setMemoryUsage(from.getMemoryUsage());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptOut(from.getQuota()));
        }
        if (from.isSetVcpuLimit()) {
            to.setVcpuLimit(from.getVcpuLimit());
        }
        if (from.isSetVcpuUsage()) {
            to.setVcpuUsage(from.getVcpuUsage());
        }
        return to;
    }
}
