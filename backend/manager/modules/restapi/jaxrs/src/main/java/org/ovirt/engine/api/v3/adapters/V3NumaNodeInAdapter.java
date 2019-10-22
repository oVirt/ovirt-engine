/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNode;

public class V3NumaNodeInAdapter implements V3Adapter<V3NumaNode, NumaNode> {
    @Override
    public NumaNode adapt(V3NumaNode from) {
        NumaNode to = new NumaNode();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptIn(from.getCpu()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
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
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        return to;
    }
}
