/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.SchedulingPolicyUnits;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyUnits;

public class V3SchedulingPolicyUnitsInAdapter implements V3Adapter<V3SchedulingPolicyUnits, SchedulingPolicyUnits> {
    @Override
    public SchedulingPolicyUnits adapt(V3SchedulingPolicyUnits from) {
        SchedulingPolicyUnits to = new SchedulingPolicyUnits();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getSchedulingPolicyUnits().addAll(adaptIn(from.getSchedulingPolicyUnits()));
        return to;
    }
}
