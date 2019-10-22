/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.JobStatus;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Job;

public class V3JobInAdapter implements V3Adapter<V3Job, Job> {
    @Override
    public Job adapt(V3Job from) {
        Job to = new Job();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAutoCleared()) {
            to.setAutoCleared(from.isAutoCleared());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetEndTime()) {
            to.setEndTime(from.getEndTime());
        }
        if (from.isSetExternal()) {
            to.setExternal(from.isExternal());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLastUpdated()) {
            to.setLastUpdated(from.getLastUpdated());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOwner()) {
            to.setOwner(adaptIn(from.getOwner()));
        }
        if (from.isSetStartTime()) {
            to.setStartTime(from.getStartTime());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(JobStatus.fromValue(from.getStatus().getState()));
        }
        return to;
    }
}
