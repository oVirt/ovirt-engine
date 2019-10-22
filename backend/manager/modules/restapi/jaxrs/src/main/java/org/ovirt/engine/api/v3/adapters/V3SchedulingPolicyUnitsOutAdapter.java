/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.SchedulingPolicyUnits;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyUnits;

public class V3SchedulingPolicyUnitsOutAdapter implements V3Adapter<SchedulingPolicyUnits, V3SchedulingPolicyUnits> {
    @Override
    public V3SchedulingPolicyUnits adapt(SchedulingPolicyUnits from) {
        V3SchedulingPolicyUnits to = new V3SchedulingPolicyUnits();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getSchedulingPolicyUnits().addAll(adaptOut(from.getSchedulingPolicyUnits()));
        return to;
    }
}
