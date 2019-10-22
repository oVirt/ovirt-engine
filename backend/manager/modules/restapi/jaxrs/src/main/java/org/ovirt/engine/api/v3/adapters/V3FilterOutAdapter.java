/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Filter;

public class V3FilterOutAdapter implements V3Adapter<Filter, V3Filter> {
    @Override
    public V3Filter adapt(Filter from) {
        V3Filter to = new V3Filter();
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
            to.setSchedulingPolicyUnit(adaptOut(from.getSchedulingPolicyUnit()));
        }
        return to;
    }
}
