/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Weight;

public class V3WeightOutAdapter implements V3Adapter<Weight, V3Weight> {
    @Override
    public V3Weight adapt(Weight from) {
        V3Weight to = new V3Weight();
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
        if (from.isSetFactor()) {
            to.setFactor(from.getFactor());
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
        if (from.isSetSchedulingPolicy()) {
            to.setSchedulingPolicy(adaptOut(from.getSchedulingPolicy()));
        }
        if (from.isSetSchedulingPolicyUnit()) {
            to.setSchedulingPolicyUnit(adaptOut(from.getSchedulingPolicyUnit()));
        }
        return to;
    }
}
