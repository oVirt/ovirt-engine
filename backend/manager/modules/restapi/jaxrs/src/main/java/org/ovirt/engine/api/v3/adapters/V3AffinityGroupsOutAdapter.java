/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.AffinityGroups;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AffinityGroups;

public class V3AffinityGroupsOutAdapter implements V3Adapter<AffinityGroups, V3AffinityGroups> {
    @Override
    public V3AffinityGroups adapt(AffinityGroups from) {
        V3AffinityGroups to = new V3AffinityGroups();
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
        to.getAffinityGroups().addAll(adaptOut(from.getAffinityGroups()));
        return to;
    }
}
