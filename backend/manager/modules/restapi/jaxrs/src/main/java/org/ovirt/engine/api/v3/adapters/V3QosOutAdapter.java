/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QoS;

public class V3QosOutAdapter implements V3Adapter<Qos, V3QoS> {
    @Override
    public V3QoS adapt(Qos from) {
        V3QoS to = new V3QoS();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpuLimit()) {
            to.setCpuLimit(from.getCpuLimit());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptOut(from.getDataCenter()));
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
        if (from.isSetInboundAverage()) {
            to.setInboundAverage(from.getInboundAverage());
        }
        if (from.isSetInboundBurst()) {
            to.setInboundBurst(from.getInboundBurst());
        }
        if (from.isSetInboundPeak()) {
            to.setInboundPeak(from.getInboundPeak());
        }
        if (from.isSetMaxIops()) {
            to.setMaxIops(from.getMaxIops());
        }
        if (from.isSetMaxReadIops()) {
            to.setMaxReadIops(from.getMaxReadIops());
        }
        if (from.isSetMaxReadThroughput()) {
            to.setMaxReadThroughput(from.getMaxReadThroughput());
        }
        if (from.isSetMaxThroughput()) {
            to.setMaxThroughput(from.getMaxThroughput());
        }
        if (from.isSetMaxWriteIops()) {
            to.setMaxWriteIops(from.getMaxWriteIops());
        }
        if (from.isSetMaxWriteThroughput()) {
            to.setMaxWriteThroughput(from.getMaxWriteThroughput());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOutboundAverage()) {
            to.setOutboundAverage(from.getOutboundAverage());
        }
        if (from.isSetOutboundAverageLinkshare()) {
            to.setOutboundAverageLinkshare(from.getOutboundAverageLinkshare());
        }
        if (from.isSetOutboundAverageRealtime()) {
            to.setOutboundAverageRealtime(from.getOutboundAverageRealtime());
        }
        if (from.isSetOutboundAverageUpperlimit()) {
            to.setOutboundAverageUpperlimit(from.getOutboundAverageUpperlimit());
        }
        if (from.isSetOutboundBurst()) {
            to.setOutboundBurst(from.getOutboundBurst());
        }
        if (from.isSetOutboundPeak()) {
            to.setOutboundPeak(from.getOutboundPeak());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
