/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNode;
import org.ovirt.engine.api.v3.types.V3Statistics;

public class V3NumaNodeOutAdapter implements V3Adapter<NumaNode, V3NumaNode> {
    @Override
    public V3NumaNode adapt(NumaNode from) {
        V3NumaNode to = new V3NumaNode();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptOut(from.getCpu()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        if (from.isSetMemory()) {
            to.setMemory(from.getMemory());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNodeDistance()) {
            to.setNodeDistance(from.getNodeDistance());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        return to;
    }
}
