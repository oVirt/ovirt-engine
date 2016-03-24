/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3QoS;

public class V3QoSInAdapter implements V3Adapter<V3QoS, Qos> {
    @Override
    public Qos adapt(V3QoS from) {
        Qos to = new Qos();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpuLimit()) {
            to.setCpuLimit(from.getCpuLimit());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
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
            to.setType(QosType.fromValue(from.getType()));
        }
        return to;
    }
}
