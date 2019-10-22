/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.LogSeverity;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Event;

public class V3EventInAdapter implements V3Adapter<V3Event, Event> {
    @Override
    public Event adapt(V3Event from) {
        Event to = new Event();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetCode()) {
            to.setCode(from.getCode());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCorrelationId()) {
            to.setCorrelationId(from.getCorrelationId());
        }
        if (from.isSetCustomData()) {
            to.setCustomData(from.getCustomData());
        }
        if (from.isSetCustomId()) {
            to.setCustomId(from.getCustomId());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetFloodRate()) {
            to.setFloodRate(from.getFloodRate());
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
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetSeverity()) {
            to.setSeverity(LogSeverity.fromValue(from.getSeverity()));
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetTime()) {
            to.setTime(from.getTime());
        }
        if (from.isSetUser()) {
            to.setUser(adaptIn(from.getUser()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
