/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicies;

public class V3SchedulingPoliciesOutAdapter implements V3Adapter<SchedulingPolicies, V3SchedulingPolicies> {
    @Override
    public V3SchedulingPolicies adapt(SchedulingPolicies from) {
        V3SchedulingPolicies to = new V3SchedulingPolicies();
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
        to.getSchedulingPolicy().addAll(adaptOut(from.getSchedulingPolicies()));
        return to;
    }
}
