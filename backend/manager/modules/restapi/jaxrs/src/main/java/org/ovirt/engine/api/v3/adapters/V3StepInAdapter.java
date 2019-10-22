/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ExternalSystemType;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StepEnum;
import org.ovirt.engine.api.model.StepStatus;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Step;

public class V3StepInAdapter implements V3Adapter<V3Step, Step> {
    @Override
    public Step adapt(V3Step from) {
        Step to = new Step();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
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
            to.setExternalType(ExternalSystemType.fromValue(from.getExternalType()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetJob()) {
            to.setJob(adaptIn(from.getJob()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNumber()) {
            to.setNumber(from.getNumber());
        }
        if (from.isSetParentStep()) {
            to.setParentStep(adaptIn(from.getParentStep()));
        }
        if (from.isSetStartTime()) {
            to.setStartTime(from.getStartTime());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(StepStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetType()) {
            to.setType(StepEnum.fromValue(from.getType()));
        }
        return to;
    }
}
