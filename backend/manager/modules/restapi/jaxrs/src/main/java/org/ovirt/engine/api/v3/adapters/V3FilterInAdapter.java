/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Filter;

public class V3FilterInAdapter implements V3Adapter<V3Filter, Filter> {
    @Override
    public Filter adapt(V3Filter from) {
        Filter to = new Filter();
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
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPosition()) {
            to.setPosition(from.getPosition());
        }
        if (from.isSetSchedulingPolicyUnit()) {
            to.setSchedulingPolicyUnit(adaptIn(from.getSchedulingPolicyUnit()));
        }
        return to;
    }
}
