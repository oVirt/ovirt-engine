/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NumaNodePins;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.VirtualNumaNode;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VirtualNumaNode;

public class V3VirtualNumaNodeInAdapter implements V3Adapter<V3VirtualNumaNode, VirtualNumaNode> {
    @Override
    public VirtualNumaNode adapt(V3VirtualNumaNode from) {
        VirtualNumaNode to = new VirtualNumaNode();
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
        if (from.isSetNumaNodePins()) {
            to.setNumaNodePins(new NumaNodePins());
            to.getNumaNodePins().getNumaNodePins().addAll(adaptIn(from.getNumaNodePins().getNumaNodePin()));
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
