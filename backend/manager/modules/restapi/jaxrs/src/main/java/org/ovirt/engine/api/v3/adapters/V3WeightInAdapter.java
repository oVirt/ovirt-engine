/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Weight;

public class V3WeightInAdapter implements V3Adapter<V3Weight, Weight> {
    @Override
    public Weight adapt(V3Weight from) {
        Weight to = new Weight();
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
            to.setSchedulingPolicy(adaptIn(from.getSchedulingPolicy()));
        }
        if (from.isSetSchedulingPolicyUnit()) {
            to.setSchedulingPolicyUnit(adaptIn(from.getSchedulingPolicyUnit()));
        }
        return to;
    }
}
