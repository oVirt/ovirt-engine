/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3Step;

public class V3StepOutAdapter implements V3Adapter<Step, V3Step> {
    @Override
    public V3Step adapt(Step from) {
        V3Step to = new V3Step();
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
        if (from.isSetEndTime()) {
            to.setEndTime(from.getEndTime());
        }
        if (from.isSetExternal()) {
            to.setExternal(from.isExternal());
        }
        if (from.isSetExternalType()) {
            to.setExternalType(from.getExternalType().value());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetJob()) {
            to.setJob(adaptOut(from.getJob()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNumber()) {
            to.setNumber(from.getNumber());
        }
        if (from.isSetParentStep()) {
            to.setParentStep(adaptOut(from.getParentStep()));
        }
        if (from.isSetStartTime()) {
            to.setStartTime(from.getStartTime());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value().toUpperCase());
            to.setStatus(status);
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
