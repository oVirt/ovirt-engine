/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Event;

public class V3EventOutAdapter implements V3Adapter<Event, V3Event> {
    @Override
    public V3Event adapt(Event from) {
        V3Event to = new V3Event();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
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
            to.setDataCenter(adaptOut(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetFloodRate()) {
            to.setFloodRate(from.getFloodRate());
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
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetSeverity()) {
            to.setSeverity(from.getSeverity().value());
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetTime()) {
            to.setTime(from.getTime());
        }
        if (from.isSetUser()) {
            to.setUser(adaptOut(from.getUser()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
